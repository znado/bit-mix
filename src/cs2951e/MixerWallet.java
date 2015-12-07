package cs2951e;

import com.google.common.util.concurrent.ListenableFuture;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

// singleton wrapper for wallet
public class MixerWallet {
    private Wallet wallet;
    private PeerGroup peerGroup;

    public MixerWallet(NetworkParameters networkParams) {
        File walletFile = new File(Config.WALLET_SAVE_FILE + "-" + Config.CLIENT_PORT);
        if(!walletFile.exists()) {
            wallet = new Wallet(networkParams);
            try {
                wallet.saveToFile(walletFile);
                System.out.println("saved new to file");
            } catch (IOException e) {
                System.out.println("ERROR: could not save to wallet file.");
                e.printStackTrace();
            }
        } else {
            try {
                wallet = Wallet.loadFromFile(walletFile);
            } catch (UnreadableWalletException e) {
                System.out.println("ERROR: could not read wallet file.");
            }
        }


        try {
            BlockChain blockChain = new BlockChain(networkParams, wallet, new SPVBlockStore(networkParams, new File(Config.SPV_BLOCK_STORE_FILE + "-" + Config.CLIENT_PORT)));
            this.peerGroup = new PeerGroup(networkParams, blockChain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(networkParams));
            peerGroup.addWallet(wallet);
            // connect to P2P and download the blockchain parts we need
            peerGroup.start();
            peerGroup.downloadBlockChain();
            peerGroup.stop();

            wallet.saveToFile(walletFile);
            // make sure to do this after we download the blockchain, otherwise will spam saving the file
            wallet.autosaveToFile(walletFile, 100, TimeUnit.MILLISECONDS, null);

            // print when we receive a transaction
            wallet.addEventListener(new AbstractWalletEventListener() {
                @Override
                public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
                    System.out.println("RECEIVED FUNDS:");
                    System.out.println(transaction);
                    System.out.println(coin);
                    System.out.println(wallet);
                }
            });
        } catch (BlockStoreException e) {
            System.out.println("Error creating blockstore from file");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("ERROR: could not save to wallet file.");
            e.printStackTrace();
        }
    }

    public Address currentReceiveAddress() {
        return wallet.currentReceiveAddress();
    }

    @Override
    public String toString() {
        return wallet.toString();
    }

    public void completeTx(Wallet.SendRequest request) {
        try {
            wallet.completeTx(request);
        } catch (InsufficientMoneyException e) {
            System.out.println("Insufficient funds for mixing.");
            e.printStackTrace();
        }
    }

    public ListenableFuture<Transaction> broadcastTransaction(Transaction tx) {
        return peerGroup.broadcastTransaction(tx).future();
    }

    public void commitTx(Transaction tx) {
        wallet.commitTx(tx);
    }
}
