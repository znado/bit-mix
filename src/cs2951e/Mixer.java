package cs2951e;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.mysql.fabric.xmlrpc.base.Array;
import org.bitcoinj.core.*;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

// the main class for the actual mixer
public class Mixer {

    private MixerNetworkManager networkManager;
    private MixerWallet wallet;
    private NetworkParameters networkParams;
    private int mixAmount;
    private ECKey sigKey;

    public Mixer(NetworkParameters networkParams, MixerWallet wallet, int mixAmount, ECKey sigKey) {
        this.networkParams = networkParams;
        this.wallet = wallet;
        this.sigKey = sigKey;
        this.mixAmount = mixAmount;

        networkManager = new MixerNetworkManager(networkParams, wallet, this.sigKey, mixAmount);
        new Thread(new Runnable() {
            @Override
            public void run() {
                networkManager.run();
                return;
            }
        }).start();
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

        List<Address> mixingDestinations = ImmutableList.of(peersList.get(0).getBitcoinReceiveAddress(),
                peersList.get(1).getBitcoinReceiveAddress(),
                wallet.currentReceiveAddress());

        Transaction mixingTransaction = new Transaction(networkParams);
        // add the outputs
        for(org.bitcoinj.core.Address destination : mixingDestinations) {
            mixingTransaction.addOutput(Coin.valueOf(mixAmount), destination);
        }
        Wallet.SendRequest request = Wallet.SendRequest.forTx(mixingTransaction);
        if(!wallet.completeTx(mixingTransaction, mixingDestinations.get(0))) {
            return;
        }
        TransactionOutput changeOutput = request.tx.getOutput(request.tx.getOutputs().size() - 1);

        long changeCoins = changeOutput.getValue().longValue();

        int prevOutTxCount = request.tx.getOutputs().size();

        // send to each peer so they can add their change outputs
        for(int i=0; i<peersList.size(); i++) {
            MixerNetworkClient peer = peersList.get(i);
            System.out.println("to get change outputs, sending transaction to: " + peer.getPublicNetworkAddress().getPort());
            Optional<Transaction> optionalPeerSig = networkManager.sendToPeerToAddChangeOutputs(peer.getPublicNetworkAddress(), request.tx, mixingDestinations.get(i + 1));
            if(optionalPeerSig.isPresent()) {
                request.tx = optionalPeerSig.get();
                // only if we added another output (a change tx) should we increment our change tx value
                if(prevOutTxCount != request.tx.getOutputs().size()){
                    prevOutTxCount = request.tx.getOutputs().size();
                    changeCoins += request.tx.getOutput(request.tx.getOutputs().size() - 1).getValue().longValue();
                }
                System.out.println("got tx!: " + request.tx);
            } else {
                System.out.println("didnt get tx");
            }
        }
        System.out.println("added all outputs, need to add inputs now");

        // add own inputs
        wallet.signTx(request);

        // send for input signing
        for(int i=0; i<peersList.size(); i++) {
            MixerNetworkClient peer = peersList.get(i);
            System.out.println("to get input, sending transaction to: " + peer.getPublicNetworkAddress().getPort());
            Optional<Transaction> optionalPeerSig = networkManager.sendToPeerToSignInputs(peer.getPublicNetworkAddress(), request.tx);
            if(optionalPeerSig.isPresent()) {
                request.tx = optionalPeerSig.get();
                // only if we added another output (a change tx) should we increment our change tx value
                if(prevOutTxCount != request.tx.getOutputs().size()){
                    prevOutTxCount = request.tx.getOutputs().size();
                    changeCoins += request.tx.getOutput(request.tx.getOutputs().size() - 1).getValue().longValue();
                }
                System.out.println("got tx!: " + request.tx);
            } else {
                System.out.println("didnt get tx");
            }
        }
        System.out.println("SIGNED INPUTS!:");
        System.out.println(request.tx);

        try {
            System.out.println("BEFORE COMMIT: " + wallet);
            wallet.commitTx(request.tx);
            System.out.println("AFTER COMMIT: " + wallet);
            Transaction output = wallet.broadcastTransaction(request.tx).get();
            System.out.println("SENT TX: " + output);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("committed");


        networkManager.setCanMix(true);
        System.out.println("done");
    }

    public void stop() {
        wallet.stop();
        networkManager.stop();
    }
}