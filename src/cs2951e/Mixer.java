package cs2951e;

import com.google.common.collect.ImmutableList;
import com.mysql.fabric.xmlrpc.base.Array;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.UnreadableWalletException;
import org.spongycastle.math.ec.ECPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

// the main class for the actual mixer
public class Mixer {

    private String bitcoinSourceAddress;
    private int mixAmount;
    private MixerNetworkManager networkManager;
    private MixerWallet wallet;
    private NetworkParameters networkParams;
    private ECKey sigKey;

    public Mixer(NetworkParameters networkParams, MixerWallet wallet, int mixAmount) {
        this.networkParams = networkParams;
        this.wallet = wallet;
        this.sigKey = new ECKey();
        this.mixAmount = mixAmount;

        networkManager = new MixerNetworkManager(networkParams, wallet, this.sigKey);
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
        ArrayList<MixerNetworkClient> peersList = networkManager.findMixingPeers(Config.MIX_PEER_COUNT);
        if(peersList != null) {
            System.out.println("got mixing peers: " + Arrays.toString(peersList.toArray()));
            networkManager.setCanMix(false);
        } else {
            System.out.println("got mixing peers: null, not mixing");
            return;
        }

        // get address our mixed bitcoins will arrive at
        Address mixerReceivingAddress = wallet.currentReceiveAddress();
        Address peerMixerDestinationAddress = new Shuffler().shuffle();
        ECKey serverKey = null;
        for(MixerNetworkClient peer : peersList) {
            if(peer.getBitcoinReceiveAddress().equals(peerMixerDestinationAddress)) {
                serverKey = peer.getPubkey();
                break;
            }
        }

        // make transaction with all three as input users and three output scripts that pay out according to the above permutation
        Transaction inputContract = new Transaction(networkParams);
        ArrayList<ECKey> keys = new ArrayList<>();
        for(MixerNetworkClient peer : peersList) {
            keys.add(peer.getPubkey());
        }
        Script script = ScriptBuilder.createMultiSigOutputScript(keys.size(), keys);
        Coin inputAmount = Coin.valueOf(keys.size()*mixAmount);
        Coin singleAmount = Coin.valueOf(mixAmount);
        inputContract.addOutput(inputAmount, script);


        Transaction mixTransaction = new Transaction(networkParams);
        mixTransaction.addInput(inputContract.getOutput(0));
        for(ECKey key : keys) {
            mixTransaction.addOutput(singleAmount, key);
        }
        //Wallet.SendRequest req = Wallet.SendRequest.forTx(contract);
        //wallet.completeTx(req);
        //wallet.broadcastTransaction(req.tx);



        networkManager.setCanMix(true);
        System.out.println("done");
    }

    public void stop() {
        networkManager.stop();
    }
}