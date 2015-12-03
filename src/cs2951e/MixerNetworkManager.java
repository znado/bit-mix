package cs2951e;

import com.google.common.base.Optional;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.WrongNetworkException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

// listen for network requests/activity
public class MixerNetworkManager {
    private ServerSocket serverSocket;
    private boolean running;
    private Random rng;
    private NetworkParameters params;
    private MixerWallet wallet;

    private HashMap<MixerNetworkAddress, Void> peersList;

    private boolean canMix = true;

    public synchronized void setCanMix(boolean canMix) {
        this.canMix = canMix;
    }

    public MixerNetworkManager(NetworkParameters params, MixerWallet wallet) {
        this.params = params;
        this.wallet = wallet;

        rng = new Random();
        peersList = new HashMap<>();

        // start listening for incoming peer connections
        try {
            serverSocket = new ServerSocket(Config.CLIENT_PORT);
        } catch (IOException e) {
            System.out.println("Server error listening to port.");
            e.printStackTrace();
        }
        System.out.println("join start");
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
            System.out.println("Network manager error parsing server join response peers list: " + joinResponse);
            if(joinResponse != null) {
                JSONObject peersResponseJson = new JSONObject(joinResponse);
                JSONArray peersJson = peersResponseJson.getJSONArray("peers");
                int peerCount = peersJson.length();
                for (int i = 0; i < peerCount; i++) {
                    peersList.put(new MixerNetworkAddress(peersJson.getJSONObject(i)), null);
                }
                System.out.println("join success");
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
        System.out.println("addPeer start");
        Socket querySocket = new Socket();
        try {
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
        } catch (IOException e) {
            System.out.println("Error querying peer.");
            e.printStackTrace();
            return Optional.absent();
        }
    }

    public ArrayList<MixerNetworkClient> getPeers(int numPeersNeeded) {
        System.out.println("getPeers start");
        ArrayList<MixerNetworkClient> randomAvailablePeers = new ArrayList<>();
        ArrayList<MixerNetworkAddress> allPeers = new ArrayList<>(peersList.keySet());
        Set<Integer> triedIndexes = new LinkedHashSet<>();
        int totalPeers = peersList.size();
        if(totalPeers < numPeersNeeded) {
            return null;
        }
        while (randomAvailablePeers.size() < numPeersNeeded) {
            Integer next = rng.nextInt(totalPeers) + 1;
            MixerNetworkAddress peerNetworkAddress = allPeers.get(next);
            Optional<String> peerQueryResult = addPeer(peerNetworkAddress);
            if(peerQueryResult.isPresent()) {
                try {
                    JSONObject peerResult = new JSONObject(peerQueryResult.get());
                    if(peerResult.getBoolean("available")) {
                        randomAvailablePeers.add(
                                new MixerNetworkClient(
                                        new Address(
                                                params,
                                                Config.BITCOIN_PROTOCOL_VERSION,
                                                peerResult.getString("bitcoinAddress").getBytes()),
                                        peerNetworkAddress));
                    }
                } catch (JSONException e) {
                    System.out.println("Error parsing peer query result into JSON: " + peerQueryResult.get());
                    e.printStackTrace();
                } catch (WrongNetworkException e) {
                    System.out.println("Error parsing peer query address: " + peerQueryResult.get());
                    e.printStackTrace();
                }
            }
            triedIndexes.add(next);
            if(triedIndexes.size() > totalPeers - numPeersNeeded) {
                return null;
            }
        }
        return randomAvailablePeers;
    }

    // RUN ON A BACKGROUND THREAD
    public synchronized void run() {
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
                try {
                    JSONObject clientJson = new JSONObject(clientMessage);
                    switch (clientJson.getString("action")) {
                        case "query":
                            if(canMix) {
                                outToClient.writeBytes("{'available' : true, 'bitcoinAddress' : " + new String(wallet.currentReceiveAddress().getHash160()) + "}\n");
                                canMix = false;
                            } else {
                                outToClient.writeBytes("{'available' : false}\n");
                            }
                            break;
                    }
                } catch (JSONException e) {
                    System.out.println("Server error parsing client JSON message.");
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("Server error accepting client connection.");
                e.printStackTrace();
            }
        }
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
