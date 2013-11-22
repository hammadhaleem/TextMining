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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent the set of closed association rules found
 * by the Szathmary algorithm
 * for closed association rule mining.
 * It is based on the thesis of L. Szathmary, 2006.
 * 
 * @see AlgoClosedRules
 * @author Philippe Fournier-Viger
 */

public class ClosedRules {
	
	// the set of rules
	public final List<ClosedRule> rules = new ArrayList<ClosedRule>();  // rules
	
	// the name of this set of rules
	private final String name;
	
	/**
	 * The constructor.
	 * @param name the name of this set of rules
	 */
	public ClosedRules(String name){
		this.name = name;
	}
	
	/**
	 * Print the rules to System.out
	 * @param databaseSize  the number of transactions in the database where the rules were found.
	 */
	public void printRules(int databaseSize){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(ClosedRule rule : rules){
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  " + rule.getRelativeSupport(databaseSize) +
					" (" + rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	/**
	 * Return a string representation of the rules.
	 * @param databaseSize  the number of transactions in the database where the rules were found.
	 */
	public String toString(int databaseSize){
		StringBuffer buffer = new StringBuffer(" ------- ");
		buffer.append(name);
		buffer.append(" -------\n");
		int i=0;
		for(ClosedRule rule : rules){
//			System.out.println("  L" + j + " ");
			buffer.append("   rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
			buffer.append("support :  ");
			buffer.append(rule.getRelativeSupport(databaseSize));

			buffer.append(" (");
			buffer.append(rule.getAbsoluteSupport());
			buffer.append("/");
			buffer.append(databaseSize);
			buffer.append(") ");
			buffer.append("confidence :  " );
			buffer.append(rule.getConfidence());
			buffer.append("\n");
			i++;
		}
		return buffer.toString();
	}
	
	/**
	 * Add a rule.
	 * @param rule a rule
	 */
	public void addRule(ClosedRule rule){
		rules.add(rule);
	}
	
	/**
	 * Get the number of rules.
	 * @return the number of rules (integer)
	 */
	public int getRulesCount(){
		return rules.size();
	}

	/**
	 * Get the list of rules.
	 * @return a list of rules.
	 */
	public List<ClosedRule> getRules() {
		return rules;
	}
}
