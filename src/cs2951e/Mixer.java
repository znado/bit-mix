package cs2951e;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

// the main class for the actual mixer
public class Mixer {

    private String bitcoinSourceAddress;
    private double mixAmount;
    private PeerDiscoveryService peerDiscoveryService;
    private MixerNetworkClient localClient;
    private Wallet wallet;
    private NetworkParameters networkParams;

    public Mixer(String bitcoinSourceAddress, double mixAmount) {
        this.bitcoinSourceAddress = bitcoinSourceAddress;
        this.mixAmount = mixAmount;
        peerDiscoveryService = new PeerDiscoveryService();

        networkParams = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);

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

        // add the key we want to mix from
//        wallet.

        localClient = new MixerNetworkClient(true, wallet.currentReceiveAddress(), );
    }

    public void mix() {
        // find two ready peers
        ArrayList<MixerNetworkClient> peers = new ArrayList<>();
        for(int i=0; i<Config.MIX_PEER_COUNT; i++) {
            peers.add(peerDiscoveryService.findAblePeer());
        }


        // get address our mixed bitcoins will arrive at
        Address mixerReceivingAddress = wallet.currentReceiveAddress();
        // shuffle outputs of who is paying to who (where we are 0 and the peers in the arraylist are 1,2...)
        Address mixerDestinationAddress = new Shuffler().shuffle();

        // make transaction with all three as input users and three output scripts that pay out according to the above permutation

    }
}
