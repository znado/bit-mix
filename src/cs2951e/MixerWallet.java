package cs2951e;

import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.io.IOException;
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


        try {
            BlockChain blockChain = new BlockChain(networkParams, wallet, new SPVBlockStore(networkParams, new File(Config.SPV_BLOCK_STORE_FILE + "-" + Config.CLIENT_PORT)));
            PeerGroup peerGroup = new PeerGroup(networkParams, blockChain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(networkParams));
            peerGroup.addWallet(wallet);
            // connect to P2P and download the blockchain parts we need
            peerGroup.start();
            peerGroup.downloadBlockChain();
            peerGroup.stop();

            wallet.saveToFile(walletFile);
            // make sure to do this after we download the blockchain, otherwise will spam saving the file
            wallet.autosaveToFile(walletFile, 100, TimeUnit.MILLISECONDS, null);
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
