package cs2951e;

import org.bitcoinj.core.NetworkParameters;

import java.util.Scanner;

// the command line class
public class Main {

    private static MixerPeerServer server;

    public static void main(String[] args) {
        System.out.println("  ____ _____ _______ __  __ _______   __\n" +
                           " |  _ \\_   _|__   __|  \\/  |_   _\\ \\ / /\n" +
                           " | |_) || |    | |  | \\  / | | |  \\ V / \n" +
                           " |  _ < | |    | |  | |\\/| | | |   > <  \n" +
                           " | |_) || |_   | |  | |  | |_| |_ / . \\ \n" +
                           " |____/_____|  |_|  |_|  |_|_____/_/ \\_\\\n");

        Scanner terminalInput = new Scanner(System.in);
        System.out.println("Run as server [y/N]?:");
        String runAsServerAnswer = terminalInput.nextLine();
        if(runAsServerAnswer.equals("y") || runAsServerAnswer.equals("Y")) {
            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    server = new MixerPeerServer();
                    server.run();
                    return;
                }
            });
            serverThread.start();
            System.out.println("Server started.  Type 'stop' to shutdown.");
            String stopServerString = terminalInput.nextLine();
            while(!stopServerString.equals("stop")) {
                stopServerString = terminalInput.nextLine();
            }
            server.stop();
            serverThread.interrupt();
            return;
        }

        System.out.println("Client port: [" + Config.CLIENT_PORT + "]");
        String portString = terminalInput.nextLine();
        if(!portString.equals("")) {
            Config.CLIENT_PORT = Integer.parseInt(portString);
        }
        //System.out.println("Enter source wallet credentials:");
        //System.out.println("Source key address: ");
        String sourceKeyAddress = "17kEiACFE1PRt5BXUADA5RMevFuk28jKDT";//terminalInput.nextLine();
        //System.out.println("Amount of BTC to mix (in BTC): [0.0001]\r\n");
        double mixAmount = 0.0001;//Double.parseDouble(terminalInput.nextLine());

        NetworkParameters networkParams = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
        MixerWallet wallet = new MixerWallet(networkParams);
        Mixer mixer = new Mixer(networkParams, wallet, mixAmount);

        System.out.println("Type mix to start mixing, else exit.");
        String startMixString = terminalInput.nextLine();
        if(!startMixString.equals("mix")) {
            return;
        }

        mixer.mix();



        mixer.stop();
    }
}