package ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm_bitset;
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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_set_integers_with_tids_bitset.Itemset;

/**
 * This is a bitset-based implementation of the CHARM algorithm that was proposed by MOHAMED
 * Zaki et al (2002).The paper describing CHARM:
 * <br/><br/>
 * 
 * Zaki, M. J., & Hsiao, C. J. (2002). CHARM: An Efficient Algorithm for Closed Itemset Mining. In SDM (Vol. 2, pp. 457-473).
 * <br/><br/>
 * 
 * This version implement TIDs sets as bit vectors. Note however that Zaki
 * have proposed other optimizations (e.g. diffset), not used here.
 * 
 * @see Itemset
 * @see HashTable
 * @see ITSearchTree
 * @author Philippe Fournier-Viger
 */
public class AlgoCharm_Bitset_saveToFile {

	// for statistics
	private long startTimestamp; // start time of the last execution
	private long endTimestamp; // end time of the last execution
	
	// the minsup parameter
	private int minsupRelative;

	// The vertical database
	// Key: item   Value : the bitset representing its tidset
	Map<Integer, BitSet> mapItemTIDS = new HashMap<Integer, BitSet>();

	int tidcount;  // the number of transactions
	BufferedWriter writer = null; // object to write the output file
	private int itemsetCount; // the number of patterns found

	// for optimization with a hashTable
	private HashTable hash;

	/**
	 * Default constructor
	 */
	public AlgoCharm_Bitset_saveToFile() {
	}

	/**
	 * Run the algorithm.
	 * @param input an input file path of a transation database
	 * @param output an output file path for writing the result or if null the result is saved into memory and returned
	 * @param minsup the minimum suppport
	 * @param hashTableSize the hashtable size for the internal array
	 * @throws IOException exception if error while writing the file.
	 */
	public void runAlgorithm(String input, String output, double minsup,
			int hashTableSize) throws IOException {
		// create hash table
		this.hash = new HashTable(hashTableSize);
		// reset the number of itemset found to 0
		itemsetCount = 0;
		
		// record the start timestamp
		startTimestamp = System.currentTimeMillis();
		
		// prepare object to write output file
		writer = new BufferedWriter(new FileWriter(output));

		// (1) count the tid set of each item in the database in one database
		// pass
		
		// This map will contain the tidset of each item
		// Key: item   Value :  tidset
		mapItemTIDS = new HashMap<Integer, BitSet>(); // id item, count
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		tidcount = 0;
		// for each transaction in the input file
		while (((line = reader.readLine()) != null)) { 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line into items
			String[] lineSplited = line.split(" ");
			// for each item
			for (String stringItem : lineSplited) {
				// convert item to integer
				int item = Integer.parseInt(stringItem);
				// get the tidset of the item
				BitSet tids = mapItemTIDS.get(item);
				// add the tid of the current transaction to the tidset of the item
				if (tids == null) {
					tids = new BitSet();
					mapItemTIDS.put(item, tids);
				}
				tids.set(tidcount);
			}
			tidcount++;  // increase the transaction count
		}
		reader.close();  // closed input file

		// convert absolute minsup to relative minsup
		this.minsupRelative = (int) Math.ceil(minsup * tidcount);

		// (2) create ITSearchTree with root node
		ITSearchTree tree = new ITSearchTree();
		// create root note with the empty set
		ITNode root = new ITNode(new HashSet<Integer>());
		tree.setRoot(root);
		// set its support to size of the database
		root.setTidset(null, tidcount);

		// (3) create childs of the root node.
		for (Entry<Integer, BitSet> entry : mapItemTIDS.entrySet()) {
			int entryCardinality = entry.getValue().cardinality();
			//if the item is frequent
			if (entryCardinality >= minsupRelative) {
				// create a new node for that item
				Set<Integer> itemset = new HashSet<Integer>();
				itemset.add(entry.getKey());
				ITNode newNode = new ITNode(itemset);
				// set its tidset as the tidset that we have calculated previously
				newNode.setTidset(entry.getValue(), entryCardinality);
				// set its parent as the root
				newNode.setParent(root);
				// add the new node as child of the root node
				root.getChildNodes().add(newNode);
			}
		}

		// for optimization, sort the child of the root according to the support
		sortChildren(root);

		// while there is at least one child node of the root
		while (root.getChildNodes().size() > 0) {
			// get the first child node
			ITNode child = root.getChildNodes().get(0);
			// extend it
			extend(child);
			// save it
			save(child);
			// delete it
			delete(child);
		}

		// save all closed itemsets
		saveAllClosedItemsets();

		// record the end time for statistics
		endTimestamp = System.currentTimeMillis();
		// close output file
		writer.close();
	}

	/**
	 * Save all closed itemsets to the file
	 * @throws IOException exception if error while writing the file
	 */
	private void saveAllClosedItemsets() throws IOException {
		// for each position in the hash table.
		for (List<Itemset> hashE : hash.table) {
			// if the position is not empty
			if (hashE != null) {
				// for each itemset in that position
				for (Itemset itemsetObject : hashE) {
					// write the itemset
					writer.write(itemsetObject.toString() + " #SUP: "
							+ itemsetObject.cardinality );
					writer.newLine();
					// increase the count
					itemsetCount++;
				}
			}
		}
	}

	/**
	 * This is the "extend" method as described in the paper.
	 * @param currNode the current node.
	 * @throws IOException exception if error while writing to file.
	 */
	private void extend(ITNode currNode) throws IOException {
		// loop over the brothers of that node
		int i = 0;
		while (i < currNode.getParent().getChildNodes().size()) {
			// get the brother i
			ITNode brother = currNode.getParent().getChildNodes().get(i);
			if (brother != currNode) {

				// Property 1
				// If the tidset of the current node is the same as the one
				// of its brother
				if (currNode.getTidset().equals(brother.getTidset())) {
					// we can replace the current node itemset in the current node
					// and the subtree by the union of the brother itemset
					// and the current node itemset
					replaceInSubtree(currNode, brother.getItemset());
					// then we delete the brother
					delete(brother);
				}
				// Property 2
				// If the brother tidset contains the tidset of the current node
				else if (containsAll(brother, currNode)) {
					// Same as previous if condition except that we
					// do not delete the brother.
					replaceInSubtree(currNode, brother.getItemset());
					i++;
				}
				// Property 3
				// If the tidset of the current node contains the tidset of the
				// brother
				else if (containsAll(currNode, brother)) {
					// Generate a candidate by performing
					// the union of the itemsets of the current node and its brother
					ITNode candidate = getCandidate(currNode, brother);
					delete(brother);
					// if a candidate was obtained
					if (candidate != null) {
						// add the candidate as child node of the current node
						currNode.getChildNodes().add(candidate);
						candidate.setParent(currNode);
					}
				}
				// Property 4
				// if the tidset of the current node is not equal to the tidset
				// of its brother
				else if (!currNode.getTidset().equals(brother.getTidset())) {
					// Generate a candidate by performing
					// the union of the itemsets of the current node and its brother
					ITNode candidate = getCandidate(currNode, brother);
					// if a candidate was obtained
					if (candidate != null) {
						// add the candidate as child node of the current node
						currNode.getChildNodes().add(candidate);
						candidate.setParent(currNode);
					}
					i++; // go to next node
				} else {
					i++; // go to next node
				}
			} else {
				i++; // go to next node
			}
		}

		// for optimization, sort the child of the root according to the support
		sortChildren(currNode);

		// while the current node has child node
		while (currNode.getChildNodes().size() > 0) {
			// get the first child
			ITNode child = currNode.getChildNodes().get(0);
			extend(child);  // extend it (charm is a depth-first search algorithm)
			save(child); // save the node
			delete(child); // then delete it
		}
	}

	/**
	 * Check if the tidset of a node contains the tidset of another node
	 * @param node1 the first tidset
	 * @param node2 the second tidset
	 * @return true if it the first tidset contains the second one, otherwise false.
	 */
	private boolean containsAll(ITNode node1, ITNode node2) {
		// clone the second bitset
		BitSet newbitset = (BitSet) node2.getTidset().clone();
		// do the logical AND with the first bitset
		newbitset.and(node1.getTidset());
		// if the size of the second bitset remains the same, it means
		// that it is contained in the first bitset
		return newbitset.cardinality() == node2.size();
	}

	/**
	 * Replace the itemset of a current node by another itemset in 
	 * a subtree (including the current node).
	 * @param currNode the current node.
	 * @param itemset  the itemset
	 */
	private void replaceInSubtree(ITNode currNode, Set<Integer> itemset) {
		// make the union
		Set<Integer> union = new HashSet<Integer>(itemset);
		union.addAll(currNode.getItemset());
		// replace for this node
		currNode.setItemset(union);
		// recursively perform replacement for childs and their childs, etc.
		currNode.replaceInChildren(union);
	}

	/**
	 * Generate a candidate by performing the union of the current node and a brother of that node.
	 * @param currNode the current node
	 * @param brother  the itemset of the brother node
	 * @return  a candidate or null if the resulting candidate do not have enough support.
	 */
	private ITNode getCandidate(ITNode currNode, ITNode brother) {

		// create list of common tids of the itemset of the current node
		// and the brother node
		BitSet commonTids = (BitSet) currNode.getTidset().clone();
		commonTids.and(brother.getTidset());
		// get the cardinality of the bitset
		int cardinality = commonTids.cardinality();

		// if the common tids cardinality is enough for the minimum support
		if (cardinality >= minsupRelative) {
			// perform the union of the itemsets
			Set<Integer> union = new HashSet<Integer>(brother.getItemset());
			union.addAll(currNode.getItemset());
			// create a new node with the union
			ITNode node = new ITNode(union);
			// set the tidset as the intersection of the tids of both itemset
			node.setTidset(commonTids, cardinality);
			// return the node
			return node;
		}
		// otherwise return null because the candidate did not have enough support
		return null;
	}

	/**
	 * Delete a child from its parent node.
	 * @param child the child node
	 */
	private void delete(ITNode child) {
		child.getParent().getChildNodes().remove(child);
	}

	/**
	 * Save a node (as described in the paper).
	 * @param node the node
	 */
	private void save(ITNode node) throws IOException {
		// if it is not contained in the hash table
		if (!hash.containsSupersetOf(node.itemsetObject)) {
			// add it to the hash table
			hash.put(node.itemsetObject);
		}
	}

	/**
	 *  Sort the children of a node according to the order of support.
	 * @param node the node.
	 */
	private void sortChildren(ITNode node) {
		// sort children of the node according to the support.
		Collections.sort(node.getChildNodes(), new Comparator<ITNode>() {
			// Returns a negative integer, zero, or a positive integer as
			// the first argument is less than, equal to, or greater than the
			// second.
			public int compare(ITNode o1, ITNode o2) {
				return o1.getTidset().size() - o2.getTidset().size();
			}
		});
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  CHARM - STATS =============");
		long temps = endTimestamp - startTimestamp;
		System.out.println(" Transactions count from database : " + tidcount);
		System.out.println(" Frequent closed itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
