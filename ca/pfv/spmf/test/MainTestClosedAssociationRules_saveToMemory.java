package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.closedrules.AlgoClosedRules;
import ca.pfv.spmf.algorithms.associationrules.closedrules.ClosedRules;
import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTIDClose.AlgoAprioriTIDClose;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;

/**
 * Example of how to mine closed association rules from the source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestClosedAssociationRules_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// input and output file paths
		String input = fileToPath("contextZart.txt");
		String output = null;
		// Note: we here set the output file path to null because we want
		// to save the result to memory instead of saving it to a file.
		
		// the threshold
		double minsupp = 0.60;
		double  minconf = 0.60;
		
		// Loading the transaction database
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		database.printDatabase();
		
		// STEP 1: Applying the AlgoAprioriTIDClose algorithm to find frequent closed itemsets
		AlgoAprioriTIDClose aclose = new AlgoAprioriTIDClose();
		Itemsets patterns = aclose.runAlgorithm(database, minsupp, null);
		aclose.printStats();
		
		// STEP 2: Generate all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		AlgoClosedRules algoClosedRules = new AlgoClosedRules();
		ClosedRules rules = algoClosedRules.runAlgorithm(patterns, minconf, output);
		algoClosedRules.printStatistics();
		rules.printRules(database.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestClosedAssociationRules_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
