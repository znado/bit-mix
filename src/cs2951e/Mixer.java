package cs2951e;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// the main class for the actual mixer
public class Mixer {

    private String bitcoinSourceAddress;
    private double mixAmount;
    private MixerNetworkManager networkManager;
    private MixerWallet wallet;

    public Mixer(NetworkParameters networkParams, MixerWallet wallet, double mixAmount) {
        this.wallet = wallet;
        this.mixAmount = mixAmount;

        networkManager = new MixerNetworkManager(networkParams, wallet);
        new Thread(new Runnable() {
            @Override
            public void run() {
                networkManager.run();
                return;
            }
        }).start();
    }

    public void mix() {
        System.out.println("mixing start");
        // find two ready peers
        ArrayList<MixerNetworkClient> peers = networkManager.findMixingPeers(Config.MIX_PEER_COUNT);
        if(peers != null) {
            System.out.println("got mixing peers: " + Arrays.toString(peers.toArray()));
        } else {
            System.out.println("got mixing peers: null");
        }
        // if peers == null then don't have enough peers to mix with
        if(peers != null) {
            networkManager.setCanMix(false);
        }

        // get address our mixed bitcoins will arrive at
        Address mixerReceivingAddress = wallet.currentReceiveAddress();
        // shuffle outputs of who is paying to who (where we are 0 and the peers in the arraylist are 1,2...)
        Address mixerDestinationAddress = new Shuffler().shuffle();

        // make transaction with all three as input users and three output scripts that pay out according to the above permutation


        networkManager.setCanMix(true);
        System.out.println("done");
    }

    public void stop() {
        networkManager.stop();
    }
}
