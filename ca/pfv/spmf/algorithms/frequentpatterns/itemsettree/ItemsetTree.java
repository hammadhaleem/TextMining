package ca.pfv.spmf.algorithms.frequentpatterns.itemsettree;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the Itemset-tree data structure for targeted association
 * rule and itemset mining.<br/><br/>
 * 
 * This implementation is based on the description in:<br/><br/>
 * 
 *     Kubat, M., Hafez, A., Raghavan, V. V., Lekkala, J. R., Chen, W. K. (2003) 
 *     Itemset Trees for Targeted Association Querying. Proc. of ICDE 2003.
 *  
 * @see Itemset
 * @see ItemsetTreeNode
 * @see HashTableIT
 * @see AssociationRuleIT
 * @author Philippe Fournier-Viger
 */

public class ItemsetTree {
	
	// root of the itemset tree
	ItemsetTreeNode root = null;

	// statistics about tree construction
	long startTimestamp;  // start time
	long endTimestamp;   // end time

	/**
	 * Default constructor
	 */
	public ItemsetTree() {
		
	}

	/**
	 * Build the itemset-tree based on an input file containing transactions
	 * @param input an input file
	 * @throws IOException exception if error while reading the file
	 */
	public void buildTree(String input)
			throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// reset memory usage statistics
		MemoryLogger.getInstance().reset();
		
		// create an empty root for the tree
		root = new ItemsetTreeNode(null, 0);

		// Scan the database to read the transactions
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		 // for each line (transaction) until the end of file
		while (((line = reader.readLine()) != null)) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			// create a structure for storing the transaction
			int[] itemset = new int[lineSplited.length];
			 // for each item in the transaction
			for (int i=0; i< lineSplited.length; i++) {
				// convert the item to integer and add it to the structure
				itemset[i] = Integer.parseInt(lineSplited[i]);
			}
//			printTree();
			// call the method "construct" to add the transaction to the tree
			construct(root, itemset);
		}
		// close the input file
		reader.close();
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		// close the file
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Add a transaction to the itemset tree.
	 * @param transaction the transaction to be added (array of ints)
	 */
	public void addTransaction(int[] transaction){
		// call the "construct" algorithm to add it
		construct(root, transaction);
	}


	/**
	 * Given the root of a sub-tree, add an itemset at the proper position in that tree
	 * @param r  the root of the sub-tree
	 * @param s  the itemset to be inserted
	 */
	private void construct(ItemsetTreeNode r, int[] s) {
		// get the itemset in the root node
		int[] sr = r.itemset;
		
		// if the itemset in root node is the same as the one to be inserted,
		// we just increase the support, and return.
		if(same(s, sr)){
			r.support++;
			return;
		}
		
		// if the node to be inserted is an ancestor of the itemset of the root node
		if(ancestorOf(s, sr)){
			// create a new node for the itemset to be inserted with the support of
			// the root node + 1
			ItemsetTreeNode newNode = new ItemsetTreeNode(s, r.support +1);
			// set the childs and parent pointers.
			newNode.childs.add(r);
			r.parent.childs.remove(r);
			r.parent.childs.add(newNode);
			r.parent = newNode;
			return;  // return
		}
		
		// Otherwise, calculate the largest common ancestor
		// of the itemset to be inserted and the root of the sutree
		int[] l = getLargestCommonAncestor(s, sr);
		if(l != null){ // if there is one largest common ancestor
			// create a new node with that ancestor and the support of
			// the root +1.
			ItemsetTreeNode newNode = new ItemsetTreeNode(l, r.support +1);
			// set the node childs and parent pointers
			newNode.childs.add(r);
			r.parent.childs.remove(r);
			r.parent.childs.add(newNode);
			r.parent = newNode;
			// append second children which is the itemset to be added with a 
			// support of 1
			ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
			// update pointers for the new node
			newNode.childs.add(newNode2);
			newNode2.parent = newNode;
			return;
		}
		
		// else  get the length of the root itemset
		int indexLastItemOfR = (sr == null)? 0 : sr.length;
		// increase the support of the root
		r.support++;
		// for each child of the root
		for(ItemsetTreeNode ci : r.childs){
			
			// if one children of the root is the itemset to be inserted s,
			// then increase its support and stop
			if(same(s, ci.itemset)){ // case 2
				ci.support++;
				return;
			}
			
			// if   the itemset to be inserted is an ancestor of the child ci
			if(ancestorOf(s, ci.itemset)){ // case 3
				// create a new node between ci and r in the tree
				// and update child /parents pointers
				ItemsetTreeNode newNode = new ItemsetTreeNode(s, ci.support+ 1);
				newNode.childs.add(ci);
				newNode.parent = r;
				r.childs.remove(ci);
				r.childs.add(newNode);
				ci.parent = newNode;
				return;
			}
			
			// if the child ci is an ancestor of s
			if(ancestorOf(ci.itemset, s)){ // case 4
				// then make a recursive call to construct to handle this case.
				construct(ci, s);
				return;
			}

			// case 5
			// if ci and s have a common ancestor that is larger than r:
			if(ci.itemset[indexLastItemOfR] == s[indexLastItemOfR]){
				// find the largest common ancestor
				int[] ancestor = getLargestCommonAncestor(s, ci.itemset);
				// create a new node for the ancestor itemset just found with the support
				// of ci + 1
				ItemsetTreeNode newNode = new ItemsetTreeNode(ancestor, ci.support+ 1);
				// set r as aprent
				newNode.parent = r;
				r.childs.add(newNode);
				// add ci as a childre of the new node
				newNode.childs.add(ci);
				ci.parent = newNode;
				r.childs.remove(ci);
				// create another new node for s with a support of 1, which
				// will be the child of the first new node
				ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
				newNode2.parent = newNode;
				newNode.childs.add(newNode2);
				// end
				return;
			}
			
		}
		
		// Otherwise, case 1:
		// A new node is created for s with a support of 1 and is added
		// below the node r.
		ItemsetTreeNode newNode = new ItemsetTreeNode(s, 1);
		newNode.parent = r;
		r.childs.add(newNode);
		
	}

	/**
	 * Method to calculate the largest common ancestor of two given itemsets
	 * (as defined in the paper).
	 * @param itemset1  the first itemset
	 * @param itemset2  the second itemset
	 * @return a new itemset which is the largest common ancestor or null if it is the empty set
	 */
	private int[] getLargestCommonAncestor(int[] itemset1, int[] itemset2) {
		// if one of the itemsets is null,
		// return null.
		if(itemset2 == null || itemset1 == null){
			return null;
		}
	
		// find the minimum length of the itemsets
		int minI = itemset1.length < itemset2.length ? itemset1.length : itemset2.length;
		
		int count = 0;  // to count the size of the common ancestor
		
		// for each position in the itemsets from 0 to the maximum length -1
		// Note that we use maxI-1 because we don't want that
		// the maximum ancestor to be equal to itemset1 or itemset2
		for(int i=0; i < minI; i++){   
			// if the two items are different, we stop because
			// of the lexical ordering
			if(itemset1[i] != itemset2[i]){
				break;
			}else{
				// otherwise we inscrease the counter indicating the number of common
				// items in the prefix
				count++;
			}
		}
		// if there is a common ancestor of size >0
		// (we don,t want the empty set!)
		if(count >0 && count < minI){
			// create the itemset by copying the first "count" elements of
			// itemset1 and return it
			int[] common = new int[count];
			System.arraycopy(itemset1, 0, common, 0, count);
			return common;
		}
		else{
			// otherwise, return null because the common ancestor is the empty set
			return null;
		}
	}

	/**
	 * Check if a first itemset is the ancestor of the second itemset
	 * @param itemset1  the first itemset
	 * @param itemset2 the second itemset
	 * @return true, if yes, otherwise, false.
	 */
	private boolean ancestorOf(int[] itemset1, int[] itemset2) {
		// if the second itemset is null (empty set), return false
		if(itemset2 == null){
			return false;
		}
		// if the first itemset is null (empty set), return true
		if(itemset1 == null){
			return true;
		}
		// if the length of itemset 1 is greater than the one of
		// itemset2, it cannot be the ancestor, so return false
		if(itemset1.length >= itemset2.length){
			return false;
		}
		// otherwise, loop on items from itemset1
		// and check if they are the same as itemset 2
		for(int i=0; i< itemset1.length; i++){
			// if one item is different, itemset1 is not the ancestor
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		// otherwise itemset1 is an ancestor of itemset2
		return true;
	}

	/**
	 * Method to check if two itemsets are equals
	 * @param itemset1 the first itemset
	 * @param itemset2 the second itemset
	 * @return true if they are the same or false otherwise
	 */
	private boolean same(int[] itemset1, int[] itemset2) {
		// if one is null, then returns false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// if they don't have the same size, then they cannot
		// be equal
		if(itemset1.length != itemset2.length){
			return false;
		}
		// otherwise, loop on items from itemset1
		// and check if they are the same as itemset 2
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				// if one is different then they are not the same
				return false;
			}
		}
		// otherwise they are the same
		return true;
	}

	/**
	 * Print statistics about the time and maximum memory usage for the construction
	 * of the itemset tree. 
	 */
	public void printStatistics() {

		System.out.println("========== ITEMSET TREE CONSTRUCTION - STATS ============");
		System.out.println(" Tree construction time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("=====================================");
	}

	/**
	 * Print the tree to System.out.
	 */
	public void printTree() {
		System.out.println(root.toString(new StringBuffer(),""));
	}
	
	/**
	 * Return a string representation of the tree.
	 */
	public String toString() {
		return root.toString(new StringBuffer(), "");
	}

	/**
	 * Get the support of a given itemset s.
	 * @param s the itemset
	 * @return the support as an integer.
	 */
	public int getSupportOfItemset(int[] s) {
		return count(s, root);  // call the method count.
	}

	/**
	 * This method calculate the support of an itemset by using a subtree
	 * defined by its root.
	 * 
	 * Note: this is implemented based on the algorithm "count" of Table 2 in the paper by Kubat et al.
	// Note that there was a few problem in the algorithm in the paper.
	// I had to change > by < in :  ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
	// also the count was not correct so i had to change the way it counted the support a little bit
	// by using += instead of return.
	 * 
	 * @param s  the itemset
	 * @param root  the root of the subtree
	 * @return  the support as an integer
	 */
	private int count(int[] s, ItemsetTreeNode root) {
		// the variable count will be used to count the support
		int count =0;
		// for each child of the root
		for(ItemsetTreeNode ci : root.childs){
			// if the first item of the itemset that we are looking for
			// is smaller than the first item of the child, we need to look
			// further in that tree.
			if(ci.itemset[0]  <= s[0]){
				// if s is included in ci, add the support of ci to the current count.
				if(includedIn(s, ci.itemset)){
					count += ci.support;
				}else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){  
					// otherwise, if the last item of ci is smaller than
					// the last item of s, then  make a recursive call to explore
					// the subtree where ci is the root
					count += count(s, ci);
				}
			}
		}
		// return the total count
		return count;
	}


	/**
	 * Check if an itemset is contained in another
	 * @param itemset1 the first itemset
	 * @param itemset2 the second itemset
	 * @return true if yes, otherwise false
	 */
	private boolean includedIn(int[] itemset1, int[] itemset2) {
		int count = 0; // the current position of itemset1 that we want to find in itemset2
		
		// for each item in itemset2
		for(int i=0; i< itemset2.length; i++){
			// if we found the item
			if(itemset2[i] == itemset1[count]){
				// we will look for the next item of itemset1
				count++;
				// if we have found all items already, return true
				if(count == itemset1.length){
					return true;
				}
			}
		}
		// it is not included, so return false!
		return false;
	}

	/**
	 * Get the frequent itemsets subsuming a given itemset for a given minimum support value.
	 * @param is  the itemset
	 * @param minsup the minimum support threshold (integer)
	 * @return an hashtable containing the frequent itemsets
	 */
	public HashTableIT getFrequentItemsetSubsuming(int[] is, int minsup) {
		// call the recursive method 
		HashTableIT hashTable = getFrequentItemsetSubsuming(is);
		// after finding the itemsets we do a loop to remove those with a support lower than minsup,
		// This does not seems efficient but that is how the authors of the paper do it.
		
		// for each position in the internal array of the hash table
		for(List<Itemset> list : hashTable.table){
			// if that position is not empty
			if(list != null){
				// loop over the itemsets stored at that position
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					// if the itemset is infrequent, remove it
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minsup){
						it.remove();
					}
				}
			}
		}
		// then we return the hash table
		return hashTable;
	}
	
	
	/**
	 * This method pass through the itemset tree to get all itemsets
	 * that are subsuming a given itemset "s" and their support. Note that
	 * this method may also return infrequent itemsets that can be filtered by 
	 * additional processing after.
	 * @param s the itemset
	 * @return  an hashtable countaining itemsets and their support.
	 */
	public HashTableIT getFrequentItemsetSubsuming(int [] s){
		// create a hash table to contain the itemsets to be more efficient
		// we set the default size of the internal array to 1000
		HashTableIT hash = new HashTableIT(1000);
		
		// create an hashset to store the items of the itemset
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		// call the method selective mining for finding the sets subsuming s
		selectiveMining(s, seti, root, hash);
		return hash;
	}

	/**
	 * This method finds itemsets subsuming a given itemset. It is a recursive method that
	 * scan a subtree of the itemset-tree. It stores itemsets found in an hashtable together
	 * with their support. 
	 * @param s  the itemset s
	 * @param seti  the items from the itemset s stored in a HashSet<Integer> for more efficiency for inclusion checking
	 * @param t  the root of the subtree
	 * @param hash  the hashtable for storing the result
	 */
	private void selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNode t, HashTableIT hash) {
		// for all child nodes of the given root of the subtree
		for(ItemsetTreeNode ci : t.childs){
			// if the first item of s is smaller or equal to the
			// first item of the child
			if(ci.itemset[0]  <= s[0]){
				// Check if s is included in ci
				if(includedIn(s, ci.itemset)){
					// if ci has not child, put s in the hashtable with 
					// the support of ci, and then
					// call recursive add.
					// Note: This part is not explained correctly in the paper, 
					// i had to figure it out by myself and fix it.
					if(ci.childs.size() ==0){
						hash.put(s, ci.support);
						recursiveAdd(s, seti, ci.itemset, ci.support, hash, 0);
					}else{
						// otherwise recursively explore subtree with ci as root.
						selectiveMining(s, seti, ci, hash);
					}
					
				}
				else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
					// else if the last item of ci is smaller than the last
					// item of s, we also need to recursively explore subtree 
					// with ci as root.
					selectiveMining(s, seti, ci, hash);
				}
			}
		}
	}

	/**
	 * Perform a recursive add (as based on the procedure presented in the paper by Kubat et al.)
	 * @param s  an itemset s
	 * @param seti   the items from the itemset s in a HashSet of integers
	 * @param ci     an itemset tree node ci
	 * @param cisupport  the support of the itemset associated to ci
	 * @param hash   an hashtable used to store itemset and their support
	 * @param pos   the current position in the itemset ci
	 */
	private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableIT hash, int pos) {
		// if we have reached the end of ci, then stop
		if(pos >= ci.length){
			return;
		}
		// if the itemset i contain the item as position pos in ci
		if(!seti.contains(ci[pos])){
			// create a new itemset "newS"  by concatening the
			// item as position pos inn ci with the itemset s.
			
			// Note that the resulting itemset must be lexicographically ordered
			// so we copy the item one by one and check where the item at
			// position pos should be inserted.
			int[] newS = new int[s.length+1]; // create the new itemset
			int j=0;  // current position
			boolean added = false;  //indicate if we have added the item at pos already
			// for each item in s
			for(Integer item : s){
				// if added already or the current item is smaller than the one at pos
				if(added || item < ci[pos]){
					// we add the item from s
					newS[j++] = item;
				}else{
					// otherwise, we insert the item at position pos
					newS[j++] = ci[pos];
					newS[j++] = item;
					added = true;  // we set that variable to true to not insert it twice!
				}
			}
			// if the item at position pos was not yet added, that means
			// that he should be inserted in the last position because he his
			// greater than all other items
			if(j < s.length+1){
				newS[j++] = ci[pos];
			}
			// add the new itemset to the hashtable with the support of ci
			hash.put(newS, cisupport);
			
			// make a recursive call with the next position in ci with the new itemset
			recursiveAdd(newS, seti, ci, cisupport, hash, pos+1);
		}
		// make a recursive call with the next position in ci with itemset "S"
		recursiveAdd(s, seti, ci, cisupport, hash, pos+1);
	}

	/**
	 * Generate all association rules with a given itemset as antecedent.
	 * @param s  the itemset to be used as antecedent
	 * @param minsup  the minsup threshold to be used
	 * @param minconf the minconf threshold to be used
	 * @return a list of association rules
	 */
	public List<AssociationRuleIT> generateRules(int[] s, int minsup, double minconf) {
		// create a list of association rules for storing the result
		List<AssociationRuleIT> rules = new ArrayList<AssociationRuleIT>();
		
		// put the items from the itemset in a hashset
		// for quick item inclusion checking
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}

		// calculate the support of the itemset
		// (it will be used for calculating the confidence)
		int suppS = getSupportOfItemset(s);
		
		// get all frequent itemsets
		 HashTableIT frequentItemsets = getFrequentItemsetSubsuming(s, minsup);
		 // for each position in the hash table
		 for(List<Itemset> list : frequentItemsets.table){
			 // if the position is not empty
			if(list != null){
				// iterate over all itemsets in the same bucket in the hash table
				for(Itemset c : list){
					 // if we have found an itemset having the same size as S,
					// we continue because we want to find an itemset C to generate
					// rules by doing  C - S and that would result in the empty set.
					if(c.size() == s.length){ 
						continue;
					}
					//Try to generate a rule by 
					// creating a new itemset l for the consequent as  C - S.
					int[] l = new int[c.itemset.length - s.length];
					int pos =0;
					// we copy to l the items from c that are not in S.
					for(Integer item : c.itemset){
						if(!seti.contains(item)){
							l[pos++] = item;
						}
					}
					// calculate confidence of S -->  C - S
					int suppC = getSupportOfItemset(c.itemset);
					
					// Note: the formula for calculating the confidence is wrong in the paper.
					// It is not g(l) / g(c) but it should be g(c) / g(s).
					double conf = (double)suppC / suppS;  
					// if the confidence is no less than minconf
					if(conf >= minconf){
						// create a new rule  S --> L
						AssociationRuleIT rule = new AssociationRuleIT();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						// add it to the list of rules found.
						rules.add(rule);
					}
					
				}
			}
		}
		 // return the result
		return rules;
	}
}
