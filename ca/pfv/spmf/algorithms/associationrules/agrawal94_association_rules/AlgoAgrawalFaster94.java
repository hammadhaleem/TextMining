package ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

/**
 * This is an implementation of the "faster algorithm" for generating association rules,
 * described in Agrawal &
 * al. 1994, IBM Research Report RJ9839, June 1994.
 * <br/><br/>
 * 
 * This implementation saves the result to a file
 * or can alternatively keep it into memory if no output 
 * path is provided by the user when the runAlgorithm()
 * method is called.
 * 
 *  @see   Rule
 *  @see   Rules
 *  @author Philippe Fournier-Viger
 **/

public class AlgoAgrawalFaster94 {
	
	// the frequent itemsets that will be used to generate the rules
	private Itemsets patterns;
	
	// variable used to store the result if the user choose to save
	// the result in memory rather than to an output file
	private Rules rules;
	
	// object to write the output file if the user wish to write to a file
	BufferedWriter writer = null;
	
	// for statistics
	long startTimestamp = 0; // last execution start time
	long endTimeStamp = 0;   // last execution end time
	private int ruleCount = 0;  // number of rules generated
	private int databaseSize = 0; // number of transactions in database
	
	// parameters
	private double minconf;
	private double minlift;
	private boolean usingLift = true;
	
	/**
	 * Default constructor
	 */
	public AlgoAgrawalFaster94(){
		
	}

	/**
	 * Run the algorithm
	 * @param patterns  a set of frequent itemsets
	 * @param output an output file path for writing the result or null if the user want this method to return the result
	 * @param databaseSize  the number of transactions in the database
	 * @param minconf  the minconf threshold
	 * @return  the set of association rules if the user wished to save them into memory
	 * @throws IOException exception if error writing to the output file
	 */
	public Rules runAlgorithm(Itemsets patterns, String output, int databaseSize, double minconf) throws IOException {
		// save the parameters
		this.minconf = minconf;
		this.minlift = 0;
		usingLift = false;
		
		// start the algorithm
		return runAlgorithm(patterns, output, databaseSize);
	}

	/**
	 * Run the algorithm
	 * @param patterns  a set of frequent itemsets
	 * @param output an output file path for writing the result or null if the user want this method to return the result
	 * @param databaseSize  the number of transactions in the database
	 * @param minconf  the minconf threshold
	 * @param minlift  the minlift threshold
	 * @return  the set of association rules if the user wished to save them into memory
	 * @throws IOException exception if error writing to the output file
	 */
	public Rules runAlgorithm(Itemsets patterns, String output, int databaseSize, double minconf,
			double minlift) throws IOException {
		// save the parameters
		this.minconf = minconf;
		this.minlift = minlift;
		usingLift = true;
		
		// start the algorithm
		return runAlgorithm(patterns, output, databaseSize);
	}

	private Rules runAlgorithm(Itemsets patterns, String output, int databaseSize)
			throws IOException {
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			rules =  new Rules("ASSOCIATION RULES");
	    }else{ 
	    	// if the user want to save the result to a file
	    	rules = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}

		
		this.databaseSize = databaseSize;
		
		// record the time when the algorithm starts
		startTimestamp = System.currentTimeMillis();
		// initialize variable to count the number of rules found
		ruleCount = 0;
		// save itemsets in a member variable
		this.patterns = patterns;

		// For each frequent itemset of size >=2
		for (int k = 2; k < patterns.getLevels().size(); k++) {
			for (Itemset lk : patterns.getLevels().get(k)) {
				// create H1
				Set<Itemset> H1 = new HashSet<Itemset>();
				for (Itemset itemsetSize1 : patterns.getLevels().get(1)) {
					if (lk.contains(itemsetSize1.getItems()[0])) {
						H1.add(itemsetSize1);
					}
				}

				// / ================ I ADDED THIS BECAUSE THE ALGORITHM AS
				// DESCRIBED BY AGRAWAL94
				// / ================ DID NOT GENERATE ALL THE ASSOCIATION RULES
				Set<Itemset> H1_for_recursion = new HashSet<Itemset>();
				// for each itemset in H1
				for (Itemset hm_P_1 : H1) {
					// make a copy of  itemset_Lk_minus_hm_P_1 but remove 
					// items from  hm_P_1
					Itemset itemset_Lk_minus_hm_P_1 = (Itemset)lk
							.cloneItemSetMinusAnItemset(hm_P_1);
					
					// This is the definition of confidence:
					// double conf = supp(lk) / supp (lk - hm+1)
					// To calculate the confidence, we need 
					// the support of :  itemset_Lk_minus_hm_P_1
					calculateSupport(itemset_Lk_minus_hm_P_1); // THIS COULD BE
																// OPTIMIZED ?
																// OR DONE
																// ANOTHER WAY ?
					// calculate the confidence
					double conf = ((double) lk.getAbsoluteSupport())
							/ ((double) itemset_Lk_minus_hm_P_1
									.getAbsoluteSupport());

					// if the confidence is lower than minconf
					if(conf < minconf){
						continue;
					}
					
					double lift =0;
					// if we are using the minlift threshold
					if(usingLift){
						// if we want to calculate the lift, we need the support of hm_P_1
						calculateSupport(hm_P_1);  // if we want to calculate the lift, we need to add this.
						// calculate the lift
						double term1 = ((double)lk.getAbsoluteSupport()) /databaseSize;
						double term2 = ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport()) /databaseSize;
						double term3 = ((double)hm_P_1.getAbsoluteSupport() / databaseSize);
						lift = term1 / (term2 * term3);
//						System.out.println("term 1 " + term1);
//						System.out.println("term 2 " + term2);
//						System.out.println("term 3 " + term3);
//						System.out.println("Lift-- " + lift);
						
						// if the lift is not enough
						if(lift < minlift){
							continue;
						}
					}
					
					// create a rule
					Rule rule = new Rule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf, lift);
					// save the rule in results
					saveRule(rule);
					// recursive call
					H1_for_recursion.add(hm_P_1);
					
				}
				// ================ END OF WHAT I HAVE ADDED

				// call apGenRules
				apGenrules(k, 1, lk, H1_for_recursion);
			}
		}

		// close the file if we saved the result to a file
		if(writer != null){
			writer.close();
		}
		// record the end time of the algorithm execution
		endTimeStamp = System.currentTimeMillis();
		
		return rules;
	}

	/**
	 * Save a rule to the output file or in memory depending
	 * if the user has provided an output file path or not
	 * @param rule the rule to be saved
	 * @throws IOException exception if error writing the output file
	 */
	private void saveRule(Rule rule) throws IOException {
		ruleCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer buffer = new StringBuffer();
			// write itemset 1
			for (int i = 0; i < rule.getItemset1().size(); i++) {
				buffer.append(rule.getItemset1().get(i));
				if (i != rule.getItemset1().size() - 1) {
					buffer.append(" ");
				}
			}
			// write separator
			buffer.append(" ==> ");
			// write itemset 2
			for (int i = 0; i < rule.getItemset2().size(); i++) {
				buffer.append(rule.getItemset2().get(i));
				if (i != rule.getItemset2().size() - 1) {
					buffer.append(" ");
				}
			}
			// write separator
			buffer.append(" #SUP: ");
			// write support
			buffer.append(rule.getAbsoluteSupport());
			// write separator
			buffer.append(" #CONF: ");
			// write confidence
			buffer.append(doubleToString(rule.getConfidence()));
			if(usingLift){
				buffer.append(" #LIFT: ");
				buffer.append(doubleToString(rule.getLift()));
			}
			
			writer.write(buffer.toString());
			writer.newLine();
			writer.flush();
		}// otherwise the result is kept into memory
		else{
			rules.addRule(rule);
		}
	}

	/**
	 * The ApGenRules as described in p.14 of the paper by Agrawal.
	 * (see the Agrawal paper for more details).
	 * @param lk  a itemset that is used to generate rules
	 * @throws IOException exception if error while writing output file
	 */
	private void apGenrules(int k, int m, Itemset lk, Set<Itemset> Hm)
			throws IOException {
		// System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if (k > m + 1) {
			Set<Itemset> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<Itemset> Hm_plus_1_for_recursion = new HashSet<Itemset>();
			// for each itemset in Hm+1
			for (Itemset hm_P_1 : Hm_plus_1) {
				Itemset itemset_Lk_minus_hm_P_1 = (Itemset) lk
						.cloneItemSetMinusAnItemset(hm_P_1);

				// calculate the support of  Lk/(Hm+1) because
				// we need it to calculate the confidence
				calculateSupport(itemset_Lk_minus_hm_P_1); // THIS COULD BE DONE
															// ANOTHER WAY ?
															// IT COULD PERHAPS
															// BE IMPROVED....
				
				// calculate the confidence
				double conf = ((double) lk.getAbsoluteSupport())
						/ ((double) itemset_Lk_minus_hm_P_1
								.getAbsoluteSupport());

				// if the confidence is not enough than we don't need to consider
				// the itemset Hm+1 anymore and go to the next one
				if(conf < minconf){
					continue;
				}
				
				double lift =0;
				// if the user used
				if(usingLift){
					// if we want to calculate the lift, we need the support of Hm+1
					calculateSupport(hm_P_1);  
					// calculate the lift
					double term1 = ((double)lk.getAbsoluteSupport()) /databaseSize;
					double term2 = ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport()) /databaseSize;
					
					 lift = term1 / (term2 * ((double)hm_P_1.getAbsoluteSupport() / databaseSize));
//					System.out.println("term 1 " + term1);
//					System.out.println("term 2 " + term2);
//					System.out.println("Lift " + lift);
					
					// if the lift is not enough
					if(lift < minlift){
						continue;
					}
				}
				
				// The rule has passed the confidence and lift threshold requiremeents,
				// so we save it
				Rule rule = new Rule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf, lift);
				saveRule(rule);
				// recursive call to find other rules with Hm+1
				Hm_plus_1_for_recursion.add(hm_P_1);
			}
			// recursive call to apGenRules
			apGenrules(k, m + 1, lk, Hm_plus_1_for_recursion);
		}
	}

	/**
	 * Calculate the support of an itemset by looking at the frequent patterns
	 * of the same size.
	 * 
	 * @param itemset_Lk_minus_hm_P_1
	 *            The itemset.
	 */
	private void calculateSupport(Itemset itemset_Lk_minus_hm_P_1) {
		// loop over all the patterns of the same size.
		for (Itemset itemset : patterns.getLevels().get(
				itemset_Lk_minus_hm_P_1.size())) {
			// If the pattern is found
			if (itemset.isEqualTo(itemset_Lk_minus_hm_P_1)) {
				// set its support to the same value.
				itemset_Lk_minus_hm_P_1.setAbsoluteSupport(itemset
						.getAbsoluteSupport());
				return;
			}
		}
	}

	/**
	 * Generating candidate itemsets of size k from frequent itemsets of size
	 * k-1. This is called "apriori-gen" in the paper by agrawal. This method is
	 * also used by the Apriori algorithm for generating candidates.
	 * 
	 * @param levelK_1  a set of itemsets of size k-1
	 * @return a set of candidates
	 */
	protected Set<Itemset> generateCandidateSizeK(Set<Itemset> levelK_1) {
		Set<Itemset> candidates = new HashSet<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for (Itemset itemset1 : levelK_1) {
			for (Itemset itemset2 : levelK_1) {
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if (missing != null) {
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);

					// The candidate is tested to see if its subsets of size k-1
					// are included in
					// level k-1 (they are frequent).
					if (allSubsetsOfSizeK_1AreFrequent(candidate, levelK_1)) {
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}

	/**
	 * This method checks if all the subsets of size "k" of the itemset
	 * "candidate" are frequent. It is similar to what is used in the
	 * Apriori algorithm for generating frequent itemsets.
	 * 
	 * @param candidate
	 *            An itemset of size "k".
	 * @param levelK_1
	 *            The frequent itemsets of size "k-1".
	 * @return true is all susets are frequent
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate,
			Set<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if (candidate.size() == 1) {
			return true;
		}
		// for each item of candidate, we will consider that this item is removed
		for (Integer item : candidate.getItems()) {
			// create the subset without this item
			Itemset subset = (Itemset)candidate.cloneItemSetMinusOneItem(item);
			
			// we will search itemsets of size k-1 to see if this itemset appears
			boolean found = false;
			// for each  itemset of size k-1
			for (Itemset itemset : levelK_1) {
				// if the itemset is equals to "subset", we found it and stop the loop
				if (itemset.isEqualTo(subset)) {
					found = true;
					break;
				}
			}
			// if not found return false
			if (found == false) {
				return false;
			}
		}
		// otherwise, all the subsets were found, so we return true
		return true;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  ASSOCIATION RULE GENERATION - STATS =============");
		System.out.println(" Number of association rules generated : "
				+ ruleCount);
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
	
	/**
	 * Convert a double value to a string with only five decimal
	 * @param value  a double value
	 * @return a string
	 */
	String doubleToString(double value) {
		// convert it to a string with two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(value);
	}
	
	/**
	 * Save some text to the output file
	 * @param text the text as a string
	 * @throws IOException exception if error writing the file
	 */
	private void saveTextToFile(String text) throws IOException{
		writer.write(text);
	}
	
	
	/**
	 * Save statisticts about the last algorithm execution to a file.
	 * @throws IOException exception if error writing the file
	 */
	public void saveStatsToFile() throws IOException {
		saveTextToFile("=============  ASSOCIATION RULE GENERATION - STATS =============\n");
		saveTextToFile(" Number of itemsets generated : "
				+ patterns.getItemsetsCount() + "\n");
		saveTextToFile(" Number of association rules generated : "
				+ ruleCount + "\n");
		saveTextToFile(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms\n");
		saveTextToFile("===================================================\n");
	}
}
