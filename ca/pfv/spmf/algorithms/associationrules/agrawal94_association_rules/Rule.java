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
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This class represent an association rule used by the Agrawal algorithm.
 *  * It is based on:  Agrawal &
 * al. 1994, IBM Research Report RJ9839, June 1994.
 * 
 * @author Philippe Fournier-Viger
 * @see   AlgoAgrawalFaster94
 * @see   Rules
 */
public class Rule {
	private Itemset itemset1; // antecedent
	private Itemset itemset2; // consequent
	private int transactionCount; // relative support
	private double confidence; // confidence of the rule
	private double lift; // lift of the rule

	/**
	 * Constructor
	 * 
	 * @param itemset1
	 *            the antecedent of the rule (an itemset)
	 * @param itemset2
	 *            the consequent of the rule (an itemset)
	 * @param transactionCount
	 *            the absolute support of the rule (integer)
	 * @param confidence
	 *            the confidence of the rule
	 * @param lift   the lift of the rule
	 */
	public Rule(Itemset itemset1, Itemset itemset2,
			int transactionCount, double confidence, double lift) {
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount = transactionCount;
		this.confidence = confidence;
		this.lift = lift;
	}

	/**
	 * Get the relative support of the rule (percentage)
	 * 
	 * @param databaseSize
	 *            the number of transactions in the database where this rule was
	 *            found.
	 * @return the support (double)
	 */
	public double getRelativeSupport(int databaseSize) {
		return ((double) transactionCount) / ((double) databaseSize);
	}

	/**
	 * Get the absolute support of this rule (integer).
	 * 
	 * @return the absolute support.
	 */
	public int getAbsoluteSupport() {
		return transactionCount;
	}

	/**
	 * Get the confidence of this rule.
	 * 
	 * @return the confidence
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * Get the lift of this rule.
	 * 
	 * @return the lift.
	 */
	public double getLift() {
		return lift;
	}

	/**
	 * Print this rule to System.out.
	 */
	public void print() {
		System.out.println(toString());
	}

	/**
	 * Return a String representation of this rule
	 * 
	 * @return a String
	 */
	public String toString() {
		return itemset1.toString() + " ==> " + itemset2.toString();
	}

	/**
	 * Get the left itemset of this rule (antecedent).
	 * 
	 * @return an itemset.
	 */
	public Itemset getItemset1() {
		return itemset1;
	}

	/**
	 * Get the right itemset of this rule (consequent).
	 * 
	 * @return an itemset.
	 */
	public Itemset getItemset2() {
		return itemset2;
	}
}
