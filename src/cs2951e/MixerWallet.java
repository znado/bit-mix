package cs2951e;

import com.google.common.util.concurrent.ListenableFuture;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.CoinSelector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

// singleton wrapper for wallet
public class MixerWallet {
    private Wallet wallet;
    private File walletFile;
    private PeerGroup peerGroup;
    private NetworkParameters networkParams;
    private Thread peerGroupThread;
    private Collection<TransactionOutput> gathered;

    public MixerWallet(NetworkParameters networkParams) {
        this.networkParams = networkParams;
        walletFile = new File(Config.WALLET_SAVE_FILE + "-" + Config.CLIENT_PORT);
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
            peerGroup.addPeerDiscovery(new DnsDiscovery(new String[] {
                    "seed.bitcoin.sipa.be",        // Pieter Wuille
                    "dnsseed.bluematt.me",         // Matt Corallo
                    "dnsseed.bitcoin.dashjr.org",  // Luke Dashjr
                    "seed.bitcoinstats.com",       // Chris Decker
                    "seed.bitnodes.io",            // Addy Yeow
                    "seed.bitcoin.jonasschnelli.ch"// Jonas Schnelli
            }, networkParams));
            peerGroup.addWallet(wallet);
            // connect to P2P and download the blockchain parts we need
            peerGroupThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    peerGroup.start();
                    peerGroup.downloadBlockChain();
                    System.out.println(wallet);
                }
            });
            peerGroupThread.start();


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

    public boolean completeTx(Wallet.SendRequest request, Coin fakeInputCoins) {
        try {
            System.out.println("adding input with " + fakeInputCoins.value + " fake input");
            request.signInputs = false;
            wallet.completeTx(request, fakeInputCoins);
            //System.out.println("GATHERED COIN INPUTS: " + Arrays.toString(gathered.toArray()));
            return true;
        } catch (InsufficientMoneyException e) {
            System.out.println("Insufficient funds for mixing.");
            e.printStackTrace();
        }
        return false;
    }

    public void signTx(Wallet.SendRequest req) {
        wallet.signTransaction(req);
    }

    public ListenableFuture<Transaction> broadcastTransaction(Transaction tx) {
        return peerGroup.broadcastTransaction(tx, 2).future();
    }

    public void commitTx(Transaction tx) {
        wallet.commitTx(tx);
    }

    public void stop() {
        peerGroupThread.stop();
    }
}
