package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.clostream.AlgoCloSteam;
import ca.pfv.spmf.patterns.itemset_list_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_list_integers_with_count.Itemset;

/**
 * Example of how to use the CloStream algorith, from the source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestCloStream {  

	public static void main(String [] arg){
		
		// Creating an instance of the CloStream algorithm
		AlgoCloSteam cloStream = new AlgoCloSteam();
		
		// Now we add 5 transactions
		long startTime = System.currentTimeMillis();
		Itemset transaction0 = new Itemset();
		transaction0.addItem(1);
		transaction0.addItem(3);
		transaction0.addItem(4);
		cloStream.processNewTransaction(transaction0);
		
		Itemset transaction1 = new Itemset();
		transaction1.addItem(2);
		transaction1.addItem(3);
		transaction1.addItem(5);
		cloStream.processNewTransaction(transaction1);
		
		Itemset transaction2 = new Itemset();
		transaction2.addItem(1);
		transaction2.addItem(2);
		transaction2.addItem(3);
		transaction2.addItem(5);
		cloStream.processNewTransaction(transaction2);
		
		Itemset transaction3 = new Itemset();
		transaction3.addItem(2);
		transaction3.addItem(5);
		cloStream.processNewTransaction(transaction3);

		Itemset transaction4 = new Itemset();
		transaction4.addItem(1);
		transaction4.addItem(2);
		transaction4.addItem(3);
		transaction4.addItem(5);
		cloStream.processNewTransaction(transaction4);
		
		// We print the patterns found
		List<Itemset> list = cloStream.getClosedItemsets();
		System.out.println("Closed itemsets count : " + list.size());
		for(Itemset itemset : list){
			System.out.println("  " + itemset.toString() + " absolute support : " + itemset.getAbsoluteSupport());
		}

		long endTime = System.currentTimeMillis();
		System.out.println("total Time : " + (endTime - startTime) + "ms");
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCloStream.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
