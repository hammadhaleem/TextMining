package ca.pfv.spmf.patterns.itemset_set_integers_with_tids;

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


import java.util.HashSet;
import java.util.Set;

import ca.pfv.spmf.patterns.AbstractItemset;

/**
 * This class represents an itemset implemented as a set of integers where
 * the transactions/sequences ids (tids) containing this itemset are represented
 * with a set of integers.
* 
* @see AbstractItemset
* @see Itemsets
 * @author Philippe Fournier-Viger
 */
public class Itemset extends AbstractItemset{
	/**  the items */
	 public Set<Integer> itemset = new HashSet<Integer>(); 
	/** the list of transaction/sequence ids containing this itemset */
	 public Set<Integer> tidset = new HashSet<Integer>(); 
	 /**
	  * Get this itemset as a string.
	  */
	public String toString() {
		StringBuffer r = new StringBuffer();
		for (Integer attribute : itemset) {

			r.append(attribute.toString());
			
			r.append(' ');
		}
		return r.toString();
	}
	
	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions/sequences in the database where the itemset was found
	 * @return the relative support as a double (percentage)
	 */
	public double getRelativeSupport(int nbObject) {
		return ((double) tidset.size()) / ((double) nbObject);
	}

	/**
	 * Get the support of this itemset
	 * @return the support of this itemset
	 */
	public int getAbsoluteSupport() {
		return tidset.size();
	}

	/**
	 * Get the number of items in this itemset
	 * @return the size of this itemset
	 */
	public int size() {
		return itemset.size();
	}

	/** 
	 * Check if this itemset contains a given item
	 * @param item the given item
	 * @return true if contained
	 */
	public boolean contains(Integer item) {
		return itemset.contains(item);
	}
	
	/**
	 * This method returns the set of items in this itemset.
	 * @return A set of Integers.
	 */
	public Set<Integer> getItems(){
		return itemset;
	}

	/**
	 * This class returns a new itemset that is the union of this itemset
	 * and another given itemset.
	 * @param itemset a given itemset.
	 * @return the union.
	 */
	public Itemset union(Itemset itemset) {
		Itemset union = new Itemset();
		union.getItems().addAll(getItems());
		union.getItems().addAll(itemset.getItems());
		return union;
	}

	/**
	 * Add an item to that itemset.
	 * @param item an item (Integer)
	 */
	public void addItem(Integer item) {
		getItems().add(item);
	}

	/**
	 * Set the tidset of this itemset
	 * @param tidset a set of Integers
	 */
	public void setTidset(Set<Integer> tidset) {
		this.tidset = tidset;
	}

	/**
	 * Get the set of transaction IDs.
	 * @return a Set of Integer
	 */
	public Set<Integer>  getTidset() {
		return this.tidset;
	}

}
