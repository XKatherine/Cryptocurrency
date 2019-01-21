import java.util.Collections;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

	 /**
     * The current collection of UTXOs, with each one mapped to its corresponding transaction output
     */
	private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
		this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
		if(tx==null)
			return false;
		byte[] hash = tx.getHash();
		ArrayList<UTXO> utxos = new ArrayList<UTXO>();
		for(int i = 0; i < tx.numOutputs(); i++)
			utxos.add(new UTXO(hash, i));
		// Requirement #1
		for(UTXO u : utxos)
			if(!utxoPool.contains(u))
				return false;
		// Requirement #2
		double inSum = 0;
		for(int i = 0; i < tx.numInputs(); i++){
			UTXO u = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
			Transaction.Output o = utxoPool.getTxOutput(u);
			if(o == null)
				return false;
			if(!Crypto.verifySignature(o.address, tx.getRawDataToSign(i), tx.getInput(i).signature))
				return false;
			utxos.add(u);
			inSum += o.value;
		}
		// Requirement #3
		Collections.sort(utxos);
		for(int i = 1; i<utxos.size(); i++)
			if(utxos.get(i).equals(utxos.get(i-1)))
				return false;
		// Requirement #4
		double outSum = 0;
		for(Transaction.Output output : tx.getOutputs()){
			if(output.value<0)
				return false;
			outSum += output.value;
		}
		// Requirement #5
		if(inSum < outSum)
			return false;
		return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
		ArrayList<Transaction> txs = new ArrayList<Transaction>();
		for(Transaction tx : possibleTxs)
			if(isValidTx(tx)){
				for(Transaction.Input i : tx.getInputs())
					utxoPool.removeUTXO(new UTXO(i.prevTxHash, i.outputIndex));
				txs.add(tx);
			}
		Transaction[] retTxs = new Transaction[txs.size()];
		retTxs = txs.toArray(retTxs);
		return retTxs;
    }
}
