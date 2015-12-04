package cs2951e;

public class Config {
    public static String CLIENT_PUBLIC_ADDRESS = "127.0.0.1";
    public static int CLIENT_PORT = 13308;
    public static String SERVER_ADDRESS = "127.0.0.1";
    public static int SERVER_PORT = 3308;
    public static int MIX_PEER_COUNT = 2;
    public static String WALLET_SAVE_FILE = "/home/zack/bitmix-wallet";
    public static int PEER_REFRESH_INTERVAL_MS = 600000; // 10 minutes

    public static int BITCOIN_PROTOCOL_VERSION = 5;
    public static int PEER_TIMEOUT_MS = 20000;
    public static int SERVER_TIMEOUT_MS = 2000;

    public static String SPV_BLOCK_STORE_FILE = "/home/zack/bitmix-chain";
}
