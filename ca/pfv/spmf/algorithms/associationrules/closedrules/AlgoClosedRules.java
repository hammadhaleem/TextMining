package ca.pfv.spmf.algorithms.associationrules.closedrules;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
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
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;

/**
 * This is an algorithm for generating closed association rules
 * from the set of closed itemsets as proposed by Szathmary (2006).
 * <br/><br/>
 * 
 * This algorithm is a modification of the classical algorithm for 
 * generating association rules described in 
 * Agrawal & al. 1994, IBM Research Report RJ9839, June 1994. 
 * It has been slightly modified for being applied to mine all "closed association rules"
 * as defined in Szathmary's thesis (2006).
 * <br/><br/>
 * 
 * This algorithm can save the result to a file or keep it into memory
 * if the user provides a null output file path to the runAlgorithm() method.
 *
 * @author Philippe Fournier-Viger
 */

public class AlgoClosedRules {
	
	// parameters
	private Itemsets closedItemsets;   // closed itemsets
	private double minconf;    // minimum confidence threshold
	
	// closed association rules generated
	private ClosedRules rules;
	
	// for statistics
	long startTimestamp = 0; // last execution start time
	long endTimeStamp = 0;   // last execution end time
	private int ruleCount; // the number of rules found
	
	// object to write the output file if the user wish to write to a file
	BufferedWriter writer = null;
	
	
	public AlgoClosedRules(){
		
	}

	public ClosedRules runAlgorithm(Itemsets closedItemsets, double minconf, String outputFile) throws IOException {
		this.closedItemsets = closedItemsets;
		
		// if the user want to keep the result into memory
		if(outputFile == null){
			writer = null;
			rules = new ClosedRules("Closed association rules");
	    }else{ 
	    	// if the user want to save the result to a file
	    	rules = null;
			writer = new BufferedWriter(new FileWriter(outputFile)); 
		}

		startTimestamp = System.currentTimeMillis();
		
		this.minconf = minconf;
		
		//For each frequent itemset of size >=2
		for(int k=2; k< closedItemsets.getLevels().size(); k++){
			for(Itemset lk : closedItemsets.getLevels().get(k)){
				// create H1
				Set<Itemset> H1 = new HashSet<Itemset>();
				for(Integer item : lk.getItems()){  // THIS PART WAS CHANGED
					Itemset itemset = new Itemset(item);
					H1.add(itemset);
				}
				
				Set<Itemset> H1_for_recursion  = new HashSet<Itemset>();
				for(Itemset hm_P_1 : H1){
					Itemset itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

					int supLkMinus_hm_P_1 = calculateSupport(itemset_Lk_minus_hm_P_1);   // THIS COULD BE DONE ANOTHER WAY ?
					int supLk = calculateSupport(lk);                                            // IT COULD PERHAPS BE IMPROVED....
					double conf = ((double)supLk) / ((double)supLkMinus_hm_P_1);
					
					if(conf >= minconf){
						ClosedRule rule = new ClosedRule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
						save(rule);
						H1_for_recursion.add(hm_P_1);// for recursion
					}
				}

				// call apGenRules
				apGenrules(k, 1, lk, H1_for_recursion);
			}
		}
		
		endTimeStamp = System.currentTimeMillis();
		
		// if the user chose to save to a file, we close the file.
		if(writer != null){
			writer.close();
		}
		
		
		return rules;
	}

	private void apGenrules(int k, int m, Itemset lk, Set<Itemset> Hm) throws IOException {
//		System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if(k > m+1){
			Set<Itemset> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<Itemset> Hm_plus_1_for_recursion = new HashSet<Itemset>();
			for(Itemset hm_P_1 : Hm_plus_1){
				Itemset itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

//				calculateSupport(hm_P_1);   
				int supLkMinus_hm_P_1 = calculateSupport(itemset_Lk_minus_hm_P_1);   // THIS COULD BE DONE ANOTHER WAY ?
				int supLk = calculateSupport(lk);                                            // IT COULD PERHAPS BE IMPROVED....
				double conf = ((double)supLk) / ((double)supLkMinus_hm_P_1);
				
				if(conf >= minconf){
					ClosedRule rule = new ClosedRule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
					save(rule);
					Hm_plus_1_for_recursion.add(hm_P_1);
				}
			}
			apGenrules(k, m+1, lk, Hm_plus_1_for_recursion);
		}
	}

	private int calculateSupport(Itemset itemsetToTest) {  // THIS WAS CHANGED
		for(List<Itemset> list : closedItemsets.getLevels()){
			if(list.size() == 0  || list.get(0).size() < itemsetToTest.size()){
				continue; // it is not useful to consider itemsets that are smaller  
				          // than itemsetToTest.size
			}
			for(Itemset itemset : list){
				if(itemset.containsAll(itemsetToTest)){
					return itemset.getAbsoluteSupport();
				}
			}
		} 
		return 0;
	}

	protected Set<Itemset> generateCandidateSizeK(Set<Itemset> levelK_1) {
		Set<Itemset> candidates = new HashSet<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for(Itemset itemset1 : levelK_1){
			for(Itemset itemset2 : levelK_1){
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					
//					System.out.println(" " + itemset1.toString() + " + " + itemset2.toString() + " = " + candidate.toString());
	
					// The candidate is tested to see if its subsets of size k-1 are included in
					// level k-1 (they are frequent).
					if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}
	
	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate, Set<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		for(Integer item : candidate.getItems()){
			Itemset subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for(Itemset itemset : levelK_1){
				if(itemset.isEqualTo(subset)){
					found = true;
					break;
				}
			}
			if(found == false){
				return false;
			}
		}
		return true;
	}
	
	
	private void save(ClosedRule rule) throws IOException {
		// increase the number of rule found
		ruleCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer buffer = new StringBuffer();
			// write itemset 1
			if(rule.getItemset1().size() == 0){
				buffer.append("__");
			}
			else{
				for (int i = 0; i < rule.getItemset1().size(); i++) {
					buffer.append(rule.getItemset1().get(i));
					if (i != rule.getItemset1().size() - 1) {
						buffer.append(" ");
					}
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
			buffer.append(rule.getConfidence());
			
			writer.write(buffer.toString());
			writer.newLine();
			writer.flush();
		}// otherwise the result is kept into memory
		else{
			rules.addRule(rule);
		}
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
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStatistics() {
		System.out
				.println("============= CLOSED ASSOCIATION RULE GENERATION - STATS =============");
		System.out.println(" Number of association rules generated : "
				+ ruleCount);
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
	
}
