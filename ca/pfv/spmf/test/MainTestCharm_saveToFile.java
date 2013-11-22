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
public class MainTestCharm_saveToFile {

	public static void main(String [] arg) throws IOException{

		// the file paths
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found
		
		// minimum support
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Loading the transaction database
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Applying the Charm algorithm
		AlgoCharm algo = new AlgoCharm();
		algo.runAlgorithm(output, database, 100000, minsup, true);
		algo.printStats();
		// NOTE 1: if you  use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		// NOTE 2:  1000000 is the hashtable size used by CHARM for
		// storing itemsets.  Most users don't
		// need to change this parameter.


	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCharm_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
