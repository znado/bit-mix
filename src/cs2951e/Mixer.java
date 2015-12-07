package cs2951e;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.mysql.fabric.xmlrpc.base.Array;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
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
    private int mixAmount;

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

        // test simple send
//        Wallet.SendRequest request = Wallet.SendRequest.to(peersList.get(0).getBitcoinReceiveAddress(), Coin.valueOf(mixAmount));
//        wallet.completeTx(request);
//        wallet.commitTx(request.tx);
//        wallet.save();
//        ListenableFuture<Transaction> future = wallet.broadcastTransaction(request.tx);
//        try {
//            future.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        // multi sig multi spend transaction

        // the input keys array
        ArrayList<ECKey> keys = new ArrayList<>(peersList.size());
        keys.add(sigKey);
        for(int i=0; i<peersList.size(); i++) {
            MixerNetworkClient peer = peersList.get(i);
            keys.add(peer.getPubkey());
        }

        // add our multisig output to the builder transaction
        Transaction multiSigOutputTransaction = new Transaction(networkParams);
        Script script = ScriptBuilder.createMultiSigOutputScript(peersList.size(), keys);
        Coin amount = Coin.valueOf(mixAmount);
        multiSigOutputTransaction.addOutput(amount, script);


        // send to each peer so they can add their inputs
        for(int i=0; i<peersList.size(); i++) {
            MixerNetworkClient peer = peersList.get(i);
            System.out.println("to get intput, sending transaction to: " + peer.getPublicNetworkAddress().getPort());
            Optional<Transaction> optionalPeerSig = networkManager.sendToPeerToAddInput(peer.getPublicNetworkAddress(), multiSigOutputTransaction);
            if(optionalPeerSig.isPresent()) {
                multiSigOutputTransaction = optionalPeerSig.get();
                System.out.println("got sig!: " + peerSig);
            } else {
                System.out.println("didnt get sig :(");
            }
        }

        TransactionOutput multisigOutput = multiSigOutputTransaction.getOutput(0);
        Script multisigScript = multisigOutput.getScriptPubKey();

        Transaction mixingTransaction = new Transaction(networkParams);
        for(Address destination : mixingDestinations) {
            mixingTransaction.addOutput(Coin.valueOf(mixAmount), destination);
        }
        TransactionInput mixingInput = mixingTransaction.addInput(multisigOutput);
        ArrayList<TransactionSignature> sigs = new ArrayList<>(peersList.size());
        Sha256Hash sighash = mixingTransaction.hashForSignature(0, multisigScript, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature signature = sigKey.sign(sighash);
        sigs.add(new TransactionSignature(signature, Transaction.SigHash.ALL, false));

        // send to each peer so they can sign the outputs
        for(int i=0; i<peersList.size(); i++) {
            MixerNetworkClient peer = peersList.get(i);
            System.out.println("to get sig, sending transaction to: " + peer.getPublicNetworkAddress().getPort());
            Optional<ECKey.ECDSASignature> optionalPeerSig = networkManager.sendToPeerToGetSig(peer.getPublicNetworkAddress(), multiSigOutputTransaction, mixingTransaction);
            if(optionalPeerSig.isPresent()) {
                ECKey.ECDSASignature peerSig = optionalPeerSig.get();
                System.out.println("got sig!: " + peerSig);
                sigs.add(new TransactionSignature(peerSig, Transaction.SigHash.ALL, false));
            } else {
                System.out.println("didnt get sig :(");
            }
        }
        System.out.println(mixingTransaction);


        // actually add the signature inputs program as an input
        Script inputScript = ScriptBuilder.createMultiSigInputScript(sigs);
        mixingInput.setScriptSig(inputScript);
        mixingInput.verify(multisigOutput);

        Wallet.SendRequest request = Wallet.SendRequest.forTx(mixingTransaction);
        wallet.completeTx(request);
        System.out.println("completed");
        try {
            wallet.broadcastTransaction(request.tx).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        wallet.commitTx(request.tx);
        System.out.println("committed");


        networkManager.setCanMix(true);
        System.out.println("done");
    }

    public void stop() {
        networkManager.stop();
    }
}