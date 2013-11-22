package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm.AlgoCharm;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm.AlgoCharmMFI;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.*;

/**
 * Example of how to use the CHARM-MFI algorith, from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2009)
 */
public class MainTestCharmMFI_saveToMemory {

	public static void main(String [] arg) throws IOException{
		
		// the file paths
		String input = fileToPath("contextPasquier99.txt");  // the database

		// minimum support
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Loading the binary context
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		database.printDatabase();
		
		// Applying the Charm algorithm
		AlgoCharm algo = new AlgoCharm();
		algo.runAlgorithm(null, database, 100000, minsup, false);
		// if you change use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		// Run CHARM MFI
		AlgoCharmMFI algo2 = new AlgoCharmMFI();
		algo2.runAlgorithm(null, algo.getClosedItemsets());
		algo2.printStats(database.size());
		
		// Code to browse the itemsets in memory
		Itemsets itemsets = algo2.getItemsets();
		for(List<Itemset> level : itemsets.getLevels()) {
			 for(Itemset itemset : level) {
				 for(Integer item : itemset.itemset) {
					 System.out.print(item );
				 }
				 System.out.println( "  support " + itemset.getAbsoluteSupport());
			 }
		}
		
		
//		.printItemsets(database.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCharmMFI_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
