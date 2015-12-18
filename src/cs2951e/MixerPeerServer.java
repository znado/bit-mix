package cs2951e;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// holds a list of clients in the network
public class MixerPeerServer {

    private ServerSocket serverSocket;

    private HashMap<MixerNetworkAddress, Void> peerList;


    public MixerPeerServer() {
        peerList = new HashMap<>();

        try {
            serverSocket = new ServerSocket(Config.SERVER_PORT);
        } catch (IOException e) {
            System.out.println("Server error listening to port.");
            e.printStackTrace();
        }
    }

    // RUN ON A BACKGROUND THREAD
    public void run() {
        InetSocketAddress clientSocketAddress;
        MixerNetworkAddress clientAddress;
        while(!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("got server connection");
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                clientSocketAddress = (InetSocketAddress)clientSocket.getRemoteSocketAddress();
                clientAddress = new MixerNetworkAddress(clientSocketAddress.getAddress().getHostAddress(), clientSocketAddress.getPort());
                String request = inFromClient.readLine();
                System.out.println("got server req: " + request);
                String response = generateResponse(clientAddress, request);
                outToClient.writeBytes(response + "\n");
            } catch (SocketException e) {
                return; // the server was closed
            } catch (IOException e) {
                System.out.println("Server error accepting client connection.");
                e.printStackTrace();
            }
        }
    }

    private String generateResponse(MixerNetworkAddress clientAddress, String request) {
        try {
            JSONObject clientJson = new JSONObject(request);
            try {
                String clientAction = clientJson.getString("action");
                System.out.println("client action = " + clientAction);
                switch (clientAction) {
                    case "join":
                        int port = clientJson.getInt("port");
                        clientAddress.setPort(port);
                        peerList.put(clientAddress, null);
                        return "{'error' : false, 'peers' : " + new JSONArray(peerList.keySet()).toString() + "}";
                    case "disconnect":
                        peerList.remove(clientAddress);
                        return Util.generateSuccess();
                }
            } catch (JSONException e) {
                String err = "Server error: missing key in client JSON message: " + request;
                System.out.println(err);
                e.printStackTrace();
                return Util.generateError(err);
            }
        } catch (JSONException e) {
            String err = "Server error parsing client JSON message.";
            System.out.println(err);
            e.printStackTrace();
            return Util.generateError(err);
        }
        return Util.generateError("An unknown error occurred.");
    }




    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server error shutting down.");
            e.printStackTrace();
        }
    }
}
