package cs2951e;

import java.util.Scanner;

// the command line class
public class Main {
    public static void main(String[] args) {
        System.out.println("  ____ _____ _______ __  __ _______   __\n" +
                           " |  _ \\_   _|__   __|  \\/  |_   _\\ \\ / /\n" +
                           " | |_) || |    | |  | \\  / | | |  \\ V / \n" +
                           " |  _ < | |    | |  | |\\/| | | |   > <  \n" +
                           " | |_) || |_   | |  | |  | |_| |_ / . \\ \n" +
                           " |____/_____|  |_|  |_|  |_|_____/_/ \\_\\\n");
        System.out.println("Enter source wallet credentials.");
        System.out.print("Source key address: [17kEiACFE1PRt5BXUADA5RMevFuk28jKDT]");
        Scanner terminalInput = new Scanner(System.in);
        String sourceKeyAddress = "17kEiACFE1PRt5BXUADA5RMevFuk28jKDT";//terminalInput.nextLine();
        System.out.print("Amount of BTC to mix (in BTC): [0.0001]\r\n");
        double mixAmount = 0.0001;//Double.parseDouble(terminalInput.nextLine());

        Mixer mixer = new Mixer(sourceKeyAddress, mixAmount);
        mixer.mix();




    }
}