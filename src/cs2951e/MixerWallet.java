package cs2951e;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.util.concurrent.TimeUnit;

// singleton wrapper for wallet
public class MixerWallet {
    private Wallet wallet;

    public MixerWallet(NetworkParameters networkParams) {
        File walletFile = new File(Config.WALLET_SAVE_FILE);
        if(!walletFile.exists()) {
            wallet = new Wallet(networkParams);
        } else {
            try {
                wallet = Wallet.loadFromFile(walletFile);
            } catch (UnreadableWalletException e) {
                System.out.println("ERROR: could not read wallet file.");
            }
        }
        wallet.autosaveToFile(walletFile, 100, TimeUnit.MILLISECONDS, null);
    }

    public Address currentReceiveAddress() {
        return wallet.currentReceiveAddress();
    }
}
