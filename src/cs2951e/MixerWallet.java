package cs2951e;

import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// singleton wrapper for wallet
public class MixerWallet {
    private Wallet wallet;

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
        wallet.autosaveToFile(walletFile, 100, TimeUnit.MILLISECONDS, null);

        try {
            BlockChain blockChain = new BlockChain(networkParams, wallet, new SPVBlockStore(networkParams, new File(Config.SPV_BLOCK_STORE_FILE + "-" + Config.CLIENT_PORT)));
            PeerGroup peerGroup = new PeerGroup(networkParams, blockChain);

            peerGroup.startAsync();

            wallet.addEventListener(new AbstractWalletEventListener() {
                @Override
                public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                    System.out.println("\nReceived tx " + tx.getHashAsString());
                    System.out.println(tx.toString());
                }
            });

            peerGroup.downloadBlockChain();
            peerGroup.stopAsync();

//            peerGroup.addWallet(wallet);
//            peerGroup.start();
            wallet.saveToFile(walletFile);
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
}
