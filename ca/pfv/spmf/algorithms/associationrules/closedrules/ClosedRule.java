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
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 * This class represent an association rule used by Szathmary algorithm
 * for closed association rule mining. An association rule is 
 * defined by two itemsets and it has a support and a confidence.
 * 
 * The notion of closed association rules was introduced in the 
 * thesis of L. Szathmary, 2006.
 * 
 * @see AlgoClosedRules
 * @author Philippe Fournier-Viger
 */

public class ClosedRule {
	private Itemset itemset1; // antecedent
	private Itemset itemset2; // consequent
	private int transactionCount; // absolute support
	private double confidence;
	
	/**
	 * Constructor
	 * @param itemset1 an itemset that is the left side of the rule
	 * @param itemset2 an itemset that is the right side of the rule
	 * @param transactionCount the support of the rule (integer)
	 * @param confidence the confidence of the rule
	 */
	public ClosedRule(Itemset itemset1, Itemset itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount =  transactionCount;
		this.confidence = confidence;
	}
	
	/**
	 * Get the relative support of this rule (percentage)
	 * @return the relative support
	 */
	public double getRelativeSupport(int objectCount) {
		return ((double)transactionCount) / ((double) objectCount);
	}
	
	/**
	 * Get the absolute support of this rule (integer)
	 * @return the support
	 */
	public int getAbsoluteSupport(){
		return transactionCount;
	}

	/**
	 * Get the confidence of the rule.
	 * @return the confidence (double)
	 */
	public double getConfidence() {
		return confidence;
	}
	
	/**
	 * Print this rule to System.out
	 */
	public void print(){
		System.out.println(toString());
	}
	
	/**
	 * Return a string representation of this rule
	 */
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}

	/**
	 * Get the left side of the rule.
	 * @return an itemset
	 */
	public Itemset getItemset1() {
		return itemset1;
	}

	/**
	 * Get the right side of the rule.
	 * @return an itemset
	 */
	public Itemset getItemset2() {
		return itemset2;
	}


}
