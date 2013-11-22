package ca.pfv.spmf.patterns.itemset_list_integers_with_tids;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.patterns.AbstractItemset;
import ca.pfv.spmf.patterns.AbstractMutableOrderedItemset;

/**
 * This class represents an itemset as a list of integers with a 
 * transaction id set represented by a list of integer. 
* 
* @see Itemsets
* @see AbstractMutableOrderedItemset
 * @author Philippe Fournier-Viger
 */
public class Itemset extends AbstractMutableOrderedItemset{
	/** The list of items contained in this itemset, ordered by 
	 lexical order */
	private final List<Integer> items = new ArrayList<Integer>(); 
	
	/** The set of transactions/sequences id containing this itemset */
	public Set<Integer> transactionsIds = new HashSet<Integer>();

	/**
	 * Constructor
	 */
	public Itemset() {
	}

	/**
	 * Get the support of this itemset (as an integer)
	 */
	public int getAbsoluteSupport() {
		return transactionsIds.size();
	}

	/**
	 * Add an item to this itemset.
	 * @param value the item.
	 */
	public void addItem(Integer value) {
		items.add(value);
	}

	/**
	 * Get the items in this itemset as a list.
	 * @return the items.
	 */
	public List<Integer> getItems() {
		return items;
	}
	
	/**
	 * Get the item at a given position.
	 * @param index the position
	 * @return the item
	 */
	public Integer get(int index) {
		return items.get(index);
	}

	/**
	 * Set the list of transaction/sequence ids containing this itemset
	 * @param listTransactionIds  the list of transaction/sequence ids
	 */
	public void setTIDs(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}

	/**
	 * Get the size of this itemset.
	 */
	public int size() {
		return items.size();
	}

	/**
	 * Get the list of sequence/transaction ids containing this itemset.
	 * @return a set of transaction/sequence ids.
	 */
	public Set<Integer> getTransactionsIds() {
		return transactionsIds;
	}
	
	/**
	 * This method return a new empty itemset (it just calls the constructor).
	 */
	public AbstractMutableOrderedItemset createNewEmptyItemset() {
		return new Itemset();
	}

}
