package cs2951e;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.spongycastle.math.ec.ECPoint;

import java.util.Arrays;

// simple bitcoin address, pub key, and network pair
public class MixerNetworkClient {

    private Address bitcoinReceiveAddress;
    private ECKey pubkey;
    private MixerNetworkAddress publicNetworkAddress;

    public Address getBitcoinReceiveAddress() {
        return bitcoinReceiveAddress;
    }

    public MixerNetworkClient(Address bitcoinReceiveAddress, ECKey pukey, MixerNetworkAddress publicNetworkAddress) {
        this.bitcoinReceiveAddress = bitcoinReceiveAddress;
        this.pubkey = pukey;
        this.publicNetworkAddress = publicNetworkAddress;
    }

    public void updateBitcoinReceiveAddress(Address bitcoinReceiveAddress) {
        this.bitcoinReceiveAddress = bitcoinReceiveAddress;
    }

    public ECKey getPubkey() {
        return pubkey;
    }

    public MixerNetworkAddress getPublicNetworkAddress() {
        return publicNetworkAddress;
    }

    @Override
    public String toString() {
        return "MixerNetworkClient{" +
                "bitcoinReceiveAddress=" + bitcoinReceiveAddress +// ", addr bytes = " + Arrays.toString(bitcoinReceiveAddress.getHash160()) +
                ", pubKey" + pubkey +
                ", publicNetworkAddress=" + publicNetworkAddress +
                '}';
    }
}
