package cs2951e;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

// a transaction that has three input sigs that each pay to an output
// where the output is chosen via the garbled circuits shuffle
public class MixerTransaction extends Transaction {
    public MixerTransaction(NetworkParameters params) {
        super(params);


    }


}