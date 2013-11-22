package ca.pfv.spmf.patterns.itemset_list_integers_with_count;

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
import java.util.List;

import ca.pfv.spmf.patterns.AbstractMutableOrderedItemset;

/**
 * This class represents an itemset (a set of items) where the
 * itemset is a set of integers sorted by lexical order where no item can appear
 * twice and the number of transactions/sequences containing this itemset is
 * represented by an integer.
* 
* @see AbstractMutableOrderedItemset
 * @author Philippe Fournier-Viger
 */
public class Itemset extends AbstractMutableOrderedItemset {
	/** The list of items contained in this itemset */
	protected List<Integer> items = new ArrayList<Integer>();
	
	/** The support of this itemset */
	protected int transactioncount = 0;

	/**
	 * Constructor
	 */
	public Itemset() {
		super();
	}

	/**
	 * Get the absolute support of this itemset (an integer)
	 * 
	 * @return the support
	 */
	public int getAbsoluteSupport() {
		return transactioncount;
	}

	/**
	 * Increase the absolute support of this itemset by 1
	 */
	public void increaseTransactionCount() {
		transactioncount++;
	}

	/**
	 * Add an item to this itemset
	 * 
	 * @param item
	 *            the item to be added
	 */
	public void addItem(Integer item) {
		items.add(item);
	}

	/**
	 * Get the list of items contained in this itemset.
	 */
	public List<Integer> getItems() {
		return items;
	}

	/**
	 * Get the item at position "index"
	 */
	public Integer get(int index) {
		return items.get(index);
	}

	/**
	 * Set the support of this itemset
	 * @param transactioncount the support
	 */
	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}

	/**
	 * Return the size of this itemset
	 */
	public int size() {
		return items.size();
	}

	/**
	 * Make a copy of this itemset
	 * @return the copy
	 */
	public Itemset cloneItemset() {
		Itemset itemset = new Itemset();
		for (Integer item : items) {
			itemset.addItem(item);
		}
		return itemset;
	}
	
	/**
	 * This method return a new empty itemset (it just calls the constructor).
	 */
	public AbstractMutableOrderedItemset createNewEmptyItemset() {
		return new Itemset();
	}

}