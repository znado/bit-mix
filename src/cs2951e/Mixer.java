package cs2951e;

import com.google.common.base.Optional;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

// the main class for the actual mixer
public class Mixer {

    private MixerNetworkManager networkManager;
    private MixerWallet wallet;
    private NetworkParameters networkParams;
    private ECKey sigKey;

    public Mixer(NetworkParameters networkParams, MixerWallet wallet, int mixAmount) {
        this.networkParams = networkParams;
        this.wallet = wallet;
        this.sigKey = new ECKey();

        networkManager = new MixerNetworkManager(networkParams, wallet, this.sigKey, mixAmount);
        new Thread(new Runnable() {
            @Override
            public void run() {
                networkManager.run();
                return;
            }
        }).start();

        // TODO TESTING ONLY NEED TO REMOVE
        networkManager.setShuffleAddress(wallet.currentReceiveAddress());
    }

    public void mix() {
        // find two ready peers
        ArrayList<MixerNetworkClient> peersList = networkManager.findMixingPeers(Config.MIX_PEER_COUNT);
        if(peersList != null) {
            System.out.println("got mixing peers: " + Arrays.toString(peersList.toArray()));
            networkManager.setCanMix(false);
        } else {
            System.out.println("got mixing peers: null, not mixing");
            return;
        }

        Address shuffleDestinationAddress = peersList.get(0).getBitcoinReceiveAddress();
        networkManager.setShuffleAddress(shuffleDestinationAddress);

        Transaction testTransaction = new Transaction(networkParams);
        testTransaction = networkManager.addShuffleOutput(testTransaction);


        // send to each peer so they can add their output
        for(MixerNetworkClient peer : peersList) {
            System.out.println("sending transaction to: " + peer.getPublicNetworkAddress().getPort());
            Optional<Transaction> modifiedTransaction = networkManager.sendToPeerToGetOutput(peer.getPublicNetworkAddress(), testTransaction);
            if(modifiedTransaction.isPresent()) {
                testTransaction = modifiedTransaction.get();
                System.out.println("got modified transaction!");
                System.out.println(testTransaction);
            } else {
                System.out.println("didnt get modified transaction!");
            }
        }
        System.out.println(testTransaction);

//        Wallet.SendRequest request = Wallet.SendRequest.forTx(testTransaction);
//        wallet.completeTx(request);
//        System.out.println("completed");
//        wallet.commitTx(request.tx);
//        System.out.println("committed");
//        try {
//            Transaction sentTransaction = wallet.broadcastTransaction(request.tx).get();
//            System.out.println("sent: " + sentTransaction);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }



        // get address our mixed bitcoins will arrive at
        /*Address peerMixerDestinationAddress = new Shuffler().shuffle();
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
        for(ECKey key : keys) {
            mixTransaction.addOutput(singleAmount, key);
        }
        //Wallet.SendRequest req = Wallet.SendRequest.forTx(contract);
        //wallet.completeTx(req);
        //wallet.broadcastTransaction(req.tx);
        */



        networkManager.setCanMix(true);
        System.out.println("done");
    }

    public void stop() {
        networkManager.stop();
    }
}