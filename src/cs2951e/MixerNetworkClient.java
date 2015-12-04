package cs2951e;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;

// simple bitcoin address and network pair
public class MixerNetworkClient {

    private Address bitcoinReceiveAddress;
    private MixerNetworkAddress publicNetworkAddress;

    public Address getBitcoinReceiveAddress() {
        return bitcoinReceiveAddress;
    }

    public MixerNetworkClient(Address bitcoinReceiveAddress, MixerNetworkAddress publicNetworkAddress) {
        this.bitcoinReceiveAddress = bitcoinReceiveAddress;
        this.publicNetworkAddress = publicNetworkAddress;
    }

    public void updateBitcoinReceiveAddress(Address bitcoinReceiveAddress) {
        this.bitcoinReceiveAddress = bitcoinReceiveAddress;
    }

    @Override
    public String toString() {
        return "MixerNetworkClient{" +
                "bitcoinReceiveAddress=" + Base58.encode(bitcoinReceiveAddress.getHash160()) +
                ", publicNetworkAddress=" + publicNetworkAddress +
                '}';
    }
}
