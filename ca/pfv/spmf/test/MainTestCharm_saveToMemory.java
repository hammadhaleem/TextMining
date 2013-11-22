package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm.AlgoCharm;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.Itemsets;

/**
 * Example of how to use the CHARM algorithm from the source code.
 * @author Philippe Fournier-Viger (Copyright 2009)
 */
public class MainTestCharm_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// Loading the transaction database
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		database.printDatabase();
		
		// Applying the Charm algorithm
		AlgoCharm algo = new AlgoCharm();
		Itemsets closedItemsets = algo.runAlgorithm(null, database, 100000, 0.4, true);
		// NOTE 0: We use "null" as output file path, because in this
		// example, we want to save the result to memory instead of
		// saving to a file
		
		// NOTE 1: if you  use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		// NOTE 2:  1000000 is the hashtable size used by CHARM for
		// storing itemsets.  Most users don't use this parameter.
		
		// print the statistics
		algo.printStats();
		// print the frequent itemsets found
		closedItemsets.printItemsets(database.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCharm_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
