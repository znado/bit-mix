package cs2951e;

import org.bitcoinj.core.Address;

public class MixerNetworkClient {

    private boolean canMix = false;
    private Address bitcoinReceiveAddress;
    private String publicNetworkAddress;

    public Address getBitcoinReceiveAddress() {
        return bitcoinReceiveAddress;
    }

    public MixerNetworkClient(boolean canMix, Address bitcoinReceiveAddress, String publicNetworkAddress) {
        this.canMix = canMix;
        this.bitcoinReceiveAddress = bitcoinReceiveAddress;
        this.publicNetworkAddress = publicNetworkAddress;
    }

    public void startMixing() {
        this.canMix = false;
    }

    public void updateBitcoinReceiveAddress(Address bitcoinReceiveAddress) {
        this.bitcoinReceiveAddress = bitcoinReceiveAddress;
    }
}
