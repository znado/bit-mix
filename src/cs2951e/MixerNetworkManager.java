package cs2951e;

import com.google.common.base.Optional;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

// listen for network requests/activity
public class MixerNetworkManager {
    private ServerSocket serverSocket;
    private boolean running;
    private Random rng;
    private MixerWallet wallet;
    private ECKey sigKey;
    private NetworkParameters networkParams;
    private int mixAmount;

    private HashMap<MixerNetworkAddress, Void> peersList;

    private boolean canMix = true;

    public void setCanMix(boolean canMix) {
        this.canMix = canMix;
    }

    public MixerNetworkManager(NetworkParameters params, MixerWallet wallet, ECKey sigKey, int mixAmount) {
        this.wallet = wallet;
        this.sigKey = sigKey;
        this.networkParams = params;
        this.mixAmount = mixAmount;

        rng = new Random();
        peersList = new HashMap<>();

        // start listening for incoming peer connections
        try {
            serverSocket = new ServerSocket(Config.CLIENT_PORT);
        } catch (IOException e) {
            System.out.println("Server error listening to port.");
            e.printStackTrace();
        }
        // get a list of peers and announce our presence
        Socket joinSocket = new Socket();
        String joinResponse = "undefined";
        try {
            joinSocket.connect(new InetSocketAddress(Config.SERVER_ADDRESS, Config.SERVER_PORT), Config.PEER_TIMEOUT_MS);
            DataOutputStream outToServer = new DataOutputStream(joinSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(joinSocket.getInputStream()));
            // send peer query message and check for response
            outToServer.writeBytes("{'action' : 'join', 'port' : " + Config.CLIENT_PORT + "}\n");
            joinResponse = inFromServer.readLine();
            if(joinResponse != null) {
                JSONObject peersResponseJson = new JSONObject(joinResponse);
                JSONArray peersJson = peersResponseJson.getJSONArray("peers");
                int peerCount = peersJson.length();
                for (int i = 0; i < peerCount; i++) {
                    // don't include self in the list
                    MixerNetworkAddress peerNetworkAddress = new MixerNetworkAddress(peersJson.getJSONObject(i));
                    if(peerNetworkAddress.getPort() != Config.CLIENT_PORT || !peerNetworkAddress.getIpAddress().equals(Config.CLIENT_PUBLIC_ADDRESS)) {
                        peersList.put(peerNetworkAddress, null);
                    }
                }
                System.out.println("join success");
                System.out.println("peers list: " + Arrays.toString(peersList.keySet().toArray()));
            }
        } catch (SocketException e) {
            System.out.println("Network manager error server connection reset.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Network manager error while connecting to server.");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("Network manager error parsing server join response peers list: " + joinResponse);
            e.printStackTrace();
        }
    }


    private Optional<String> addPeer (MixerNetworkAddress peerAddress) {
        try {
            Socket querySocket = new Socket();
            querySocket.connect(new InetSocketAddress(peerAddress.getIpAddress(), peerAddress.getPort()), Config.PEER_TIMEOUT_MS);
            DataOutputStream outToPeer = new DataOutputStream(querySocket.getOutputStream());
            BufferedReader inFromPeer = new BufferedReader(new InputStreamReader(querySocket.getInputStream()));
            // send peer query message and check for response
            outToPeer.writeBytes("{'action' : 'query'}\n");
            return Optional.of(inFromPeer.readLine());
        } catch (SocketTimeoutException e) {
            System.out.println("Querying peer timed out.");
            e.printStackTrace();
            peersList.remove(peerAddress);
            return Optional.absent();
        } catch (ConnectException e) {
            System.out.println("Error connecting to peer: " + peerAddress);
            return Optional.absent();
        } catch (IOException e) {
            System.out.println("Error querying peer.");
            e.printStackTrace();
            return Optional.absent();
        }
    }

    public ArrayList<MixerNetworkClient> findMixingPeers(int numPeersNeeded) {
        ArrayList<MixerNetworkClient> randomAvailablePeers = new ArrayList<>();
        ArrayList<MixerNetworkAddress> allPeers = new ArrayList<>(peersList.keySet());
        Set<Integer> triedIndexes = new LinkedHashSet<>();
        int originalPeerCount = peersList.size();
        if(originalPeerCount < numPeersNeeded) {
            return null;
        }
        while (randomAvailablePeers.size() < numPeersNeeded) {
            Integer next = rng.nextInt(allPeers.size());
            if(triedIndexes.contains(next)) {
                continue;
            }
            MixerNetworkAddress peerNetworkAddress = allPeers.get(next);
            Optional<String> peerQueryResult = addPeer(peerNetworkAddress);
            if (peerQueryResult.isPresent()) {
                try {
                    String peerResultString = peerQueryResult.get();
                    JSONObject peerResult = new JSONObject(peerResultString);
                    if (peerResult.getBoolean("available")) {
                        try {
                            randomAvailablePeers.add(
                                    new MixerNetworkClient(
                                            new Address(
                                                    networkParams,
                                                    peerResult.getString("bitcoinAddress")),
                                            ECKey.fromPublicOnly(Util.hexToBytes(peerResult.getString("pubKey"))),
                                            peerNetworkAddress));
                        } catch (AddressFormatException e) {
                            System.out.println("Invalid peer bitcoin address.");
                            e.printStackTrace();
                        }
                    } else {
                        if (triedIndexes.size() > originalPeerCount - numPeersNeeded) {
                            return null;
                        }
                    }
                } catch (JSONException e) {
                    System.out.println("Error parsing peer query result into JSON: " + peerQueryResult.get());
                    e.printStackTrace();
                }
            } else {
                if (triedIndexes.size() > originalPeerCount - numPeersNeeded) {
                    return null;
                }
            }
            triedIndexes.add(next);
        }
        return randomAvailablePeers;
    }

    public Optional<Transaction> sendToPeerToAddChangeOutputs(MixerNetworkAddress peerAddress, Transaction mixingTransaction, long fakeCoins) {
        try {
            Socket querySocket = new Socket();
            querySocket.connect(new InetSocketAddress(peerAddress.getIpAddress(), peerAddress.getPort()), Config.PEER_TIMEOUT_MS);
            DataOutputStream outToPeer = new DataOutputStream(querySocket.getOutputStream());
            BufferedReader inFromPeer = new BufferedReader(new InputStreamReader(querySocket.getInputStream()));
            outToPeer.writeBytes("{" +
                    "'action' : 'addChangeOutputs'," +
                    "'fakeCoins' : " + fakeCoins + "," +
                    "'mixingTx' : '" + Util.bytesToHex(mixingTransaction.bitcoinSerialize()) + "'" +
                    "}\n");
            JSONObject responseJson = new JSONObject(inFromPeer.readLine());
            if(responseJson.getBoolean("error")) {
                return Optional.absent();
            }
            return Optional.of(new Transaction(networkParams, Util.hexToBytes(responseJson.getString("multisigTx"))));
        } catch (SocketTimeoutException e) {
            System.out.println("Getting output from peer timed out.");
            e.printStackTrace();
            peersList.remove(peerAddress);
        } catch (ConnectException e) {
            System.out.println("Error connecting to peer while getting output: " + peerAddress);
        } catch (IOException e) {
            System.out.println("Error with I/O while getting output from peer.");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("Malformed response during peer output retrieval.");
            e.printStackTrace();
        }
        return Optional.absent();
    }

    public Optional<Transaction> sendToPeerToSignInputs(MixerNetworkAddress peerAddress, Transaction tx) {
        try {
            Socket querySocket = new Socket();
            querySocket.connect(new InetSocketAddress(peerAddress.getIpAddress(), peerAddress.getPort()), Config.PEER_TIMEOUT_MS);
            DataOutputStream outToPeer = new DataOutputStream(querySocket.getOutputStream());
            BufferedReader inFromPeer = new BufferedReader(new InputStreamReader(querySocket.getInputStream()));
            outToPeer.writeBytes("{" +
                    "'action' : 'signInputs'," +
                    "'tx' : '" + Util.bytesToHex(tx.bitcoinSerialize()) + "'" +
                    "}\n");
            JSONObject responseJson = new JSONObject(inFromPeer.readLine());
            if(responseJson.getBoolean("error")) {
                return Optional.absent();
            }
            return Optional.of(new Transaction(networkParams, Util.hexToBytes(responseJson.getString("tx"))));
        } catch (SocketTimeoutException e) {
            System.out.println("Getting output from peer timed out.");
            e.printStackTrace();
            peersList.remove(peerAddress);
        } catch (ConnectException e) {
            System.out.println("Error connecting to peer while getting output: " + peerAddress);
        } catch (IOException e) {
            System.out.println("Error with I/O while getting output from peer.");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("Malformed response during peer output retrieval.");
            e.printStackTrace();
        }
        return Optional.absent();
    }

    // RUN ON A BACKGROUND THREAD
    public void run() {
        if(serverSocket == null) {
            System.out.println("Null server socket.");
            return;
        }
        String clientMessage;
        InetSocketAddress clientAddress;
        running = true;
        while(running) {
            try {
                Socket clientSocket = serverSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                clientMessage = inFromClient.readLine();
                System.out.println("got peer request " + clientMessage);
                String response = generateResponse(clientMessage);
                System.out.println("response " + response);
                outToClient.writeBytes(response + "\n");
            } catch (SocketException e) {
                System.out.println("P2P shutdown.");
            } catch (IOException e) {
                System.out.println("P2P server error accepting client connection.");
                e.printStackTrace();
            }
        }
    }


    private String generateResponse(String request) {
        try {
            JSONObject requestJson = new JSONObject(request);
            String response;
            switch (requestJson.getString("action")) {
                case "query":
                    if(canMix) {
                        canMix = false;
                        return "{" +
                                "'available' : true," +
                                "'bitcoinAddress' : '" + wallet.currentReceiveAddress() + "'," +
                                "'pubKey' : '" + sigKey.getPublicKeyAsHex() + "'" +
                                "}";
                    } else {
                        return "{'available' : false}";
                    }
                case "addChangeOutputs":
                    Transaction mixingTransaction = new Transaction(networkParams, Util.hexToBytes(requestJson.getString("mixingTx")));
                    int fakeCoins = requestJson.getInt("fakeCoins");
                    System.out.println("RECEIVED TO ADD INPUT TO: " + mixingTransaction + " with " + fakeCoins + " fake coin val");
                    Wallet.SendRequest walletRequest = Wallet.SendRequest.forTx(mixingTransaction);
                    walletRequest.shuffleOutputs = false;
                    if(!wallet.completeTx(walletRequest, Coin.valueOf(fakeCoins))) {
                        return generateResponse("Insufficient peer coins.");
                    }
                    TransactionOutput changeOutput = walletRequest.tx.getOutput(walletRequest.tx.getOutputs().size() - 1);
                    System.out.println("tx changeOutput: " + changeOutput);
                    response = "{'error' : false, 'multisigTx' : '" + Util.bytesToHex(walletRequest.tx.bitcoinSerialize()) + "'}";
                    return response;
                case "signInputs":
                    Transaction mixingTx = new Transaction(networkParams, Util.hexToBytes(requestJson.getString("tx")));
                    Wallet.SendRequest req = Wallet.SendRequest.forTx(mixingTx);
                    req.shuffleOutputs = false;
                    req.signInputs = false;
                    wallet.signTx(req);
                    response = "{'error' : false, 'tx' : '" + Util.bytesToHex(req.tx.bitcoinSerialize()) + "'}";
                    return response;
            }
        } catch (JSONException e) {
            String err = "P2P server error parsing client JSON message.";
            System.out.println(err);
            e.printStackTrace();
            return Util.generateError(err);
        }
        return Util.generateError("An unknown error occurred.");
    }


    public void stop() {
        running = false;
        if(serverSocket == null) {
            return;
        }
        try {

            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server error shutting down.");
            e.printStackTrace();
        }
    }
}
