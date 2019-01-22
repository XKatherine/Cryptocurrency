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
		if(tx == null)
			return false;
		ArrayList<UTXO> utxos = new ArrayList<UTXO>();
		// Requirement #1
		// Requirement #2
		double inSum = 0;
		for(Transaction.Input i : tx.getInputs()){
			UTXO u = new UTXO(i.prevTxHash, i.outputIndex);
			if(!utxoPool.contains(u))
				return false;
			Transaction.Output o = utxoPool.getTxOutput(u);
			int index = tx.getInputs().indexOf(i);
			if(o == null)
				return false;
			if(!Crypto.verifySignature(o.address, tx.getRawDataToSign(index), i.signature))
				return false;
			inSum += o.value;
			utxos.add(u);
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
		Transaction[] ret = new Transaction[0];
		if(possibleTxs == null || possibleTxs.length == 0)
			return ret;
		return handleTxs(possibleTxs, 0, this);
    }

	public Transaction[] handleTxs(Transaction[] possibleTxs, int cnt, TxHandler th){
		Transaction[] ret = new Transaction[0];
		if(cnt >= possibleTxs.length)
			return ret;
		Transaction tx = possibleTxs[cnt];
		if(!isValidTx(tx))
			return handleTxs(possibleTxs, cnt+1, th);
		//Not select this transaction
		Transaction[] ret1 = handleTxs(possibleTxs, cnt+1, th);

		//Select this transaction
		UTXOPool cpy = utxoPool;
		for(Transaction.Input i : tx.getInputs())
			cpy.removeUTXO(new UTXO(i.prevTxHash, i.outputIndex));
		TxHandler newTh = new TxHandler(cpy);
		Transaction[] retu = handleTxs(possibleTxs, cnt+1, newTh);
		Transaction[] ret2 = null;
		if(retu == null || retu.length == 0)
			ret2 = new Transaction[]{tx};
		else{
			ret2 = new Transaction[retu.length+1];
			int k = 0;
			for(Transaction t : retu)
				ret2[k++] = t;
			ret2[k] = tx;
		}
		
		int len2 = ret2.length;
		int len1 = (ret1 == null || ret1.length == 0) ? 0 : ret1.length;
		if(len2 > len1)
			return ret2;
		return ret2;
	}
}
