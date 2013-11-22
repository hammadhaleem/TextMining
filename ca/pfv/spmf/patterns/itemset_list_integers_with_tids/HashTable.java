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
import java.util.List;


/**
 * This class represent an hash table for storing itemsets, where an itemset
 * is a list of integers with a tid set represented as a list of integers.
 * It is not currently used in SPMF.
* 
 * @author Philippe Fournier-Viger
 */
public class HashTable {
	
	/** the size of the hash table **/
	private int size;
	/** the internal array for storing the itemsets **/
	private List<Itemset>[] table;
	
	/**
	 * Construtor
	 * @param size the initial size of the internal array for the hash table
	 */
	public HashTable(int size){
		this.size = size;
		table = new ArrayList[size];
	}
	
	/**
	 * Method to check if there exists a superset of a given itemset
	 * in this hashtable 
	 * @param itemset the itemset
	 * @return true if there exists one.
	 */
	public boolean containsSupersetOf(Itemset itemset) {
		// calculate hashcode of the given itemset
		int hashcode = hashCode(itemset);
		if(table[hashcode] ==  null){
			return false;
		}
		// loop through all itemsets to check if there is a superset
		for(Object object : table[hashcode]){
			Itemset itemset2 = (Itemset)object;
			// if there is, return true
			if(itemset2.getItems().contains(itemset.getItems())){
				return true;
			}
		}
		// otherwise return false
		return false;
	}
	
	/**
	 * Add an itemset to the hash table.
	 * @param itemset the itemset to be added.
	 */
	public void put(Itemset itemset) {
		// calculate the hashcode
		int hashcode = hashCode(itemset);
		if(table[hashcode] ==  null){
			table[hashcode] = new ArrayList<Itemset>();
		}
		// add the itemset at the position given by the hashcode
		table[hashcode].add(itemset);
	}
	
	/**
	 * Calculate the hashcode of an itemset.
	 * @param itemset the itemset
	 * @return return the hashcode
	 */
	public int hashCode(Itemset itemset){
		// The hashcode is the sum of the transaction/sequence id
		// containing this itemset % the size of the internal array.
		int hashcode =0;
		for(Integer tid : itemset.getTransactionsIds()){
			hashcode += tid;
		}
		return (hashcode % size);
	}

}
