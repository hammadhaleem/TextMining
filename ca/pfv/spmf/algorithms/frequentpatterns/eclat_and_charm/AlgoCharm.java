package ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm;
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.Itemsets;

/**
 * This is an implementation of the CHARM algorithm that was proposed by MOHAMED
 * ZAKI. The paper describing charm:
 * <br/><br/>
 * 
 * Zaki, M. J., & Hsiao, C. J. (2002, April). CHARM: An Efficient Algorithm for Closed Itemset Mining. In SDM (Vol. 2, pp. 457-473).
 * 
 * This implementation may not be fully optimized. In particular, Zaki proposed
 * various extensions that I have not implemented (for example diffsets).<br/><br/>
 * 
 * This version saves the result to a file or keep it into memory if no output
 * path is provided by the user to the runAlgorithm() method.<br/><br/>
 * 
 * @see TriangularMatrix
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @see HashTable
 * @see ITSearchTree
 * @author Philippe Fournier-Viger
 */
public class AlgoCharm {

	// parameters
	private int minsupRelative; // relative minimum support
	private TransactionDatabase database; // the transaction database

	// for statistics
	private long startTimestamp; // start time of the last execution
	private long endTimestamp; // end time of the last execution

	// The patterns that are found
	// (if the user want to keep them into memory)
	protected Itemsets frequentItemsets;
	BufferedWriter writer = null; // object to write the output file
	private int itemsetCount; // the number of patterns found

	// For optimization with a triangular matrix for counting
	// itemsets of size 2.
	private TriangularMatrix matrix; // the triangular matrix
	private boolean useTriangularMatrixOptimization; // if the user want to use
														// this optimization

	// for optimization with a hashTable
	private HashTable hash;

	/**
	 * Default constructor
	 */
	public AlgoCharm() {

	}

	/**
	 * Run the Charm algorithm.
	 * 
	 * @param output the filepath for saving the result
	 * @param database a transaction database taken as input
	 * @param minsuppAbsolute the ABSOLUTE minimum support (double)
	 * @param hashTableSize  the size of the hashtable to be used by charm
	 * @param useTriangularMatrixOptimization if the triangular matrix optimization should be used.
	 * @return the frequent closed itemsets found by charm
	 * @throws IOException if an error occurs while writting to file.
	 */
	public Itemsets runAlgorithm(String output, TransactionDatabase database,
			int hashTableSize, double minsuppAbsolute,
			boolean useTriangularMatrixOptimization) throws IOException {

		this.database = database;
		
		// create hash table to store candidate itemsets
		this.hash = new HashTable(hashTableSize);
		
		// convert from an absolute minimum support to relative minimum support
		// by multiplying with the database size
		this.minsupRelative = (int) Math
				.ceil(minsuppAbsolute * database.size());
		
		// save the user preference for triangular matrix optimization
		this.useTriangularMatrixOptimization = useTriangularMatrixOptimization;

		// start the algorithm!
		return run(output);
	}

	/**
	 * Run the algorithm
	 * @param output the output file path for saving the result.
	 * @param minsupRelative the RELATIVE minimum support
	 * @param useTriangularMatrixOptimization if true the triangular matrix optimization will be used 
	 * @return the list of frequent closed itemsets
	 * @throws IOException if an error occurs while writing the result to file.
	 */
	public Itemsets runAlgorithmWithRelativeMinsup(String output,
			boolean useTriangularMatrixOptimization, int minsupRelative)
			throws IOException {
		// save the parameters
		this.minsupRelative = minsupRelative;
		this.useTriangularMatrixOptimization = useTriangularMatrixOptimization;

		// run the algorithm!
		return run(output);
	}

	/**
	 * Run the algorithm.
	 * @param output an output file path for writing the result or if null the result is saved into memory and returned
	 * @return the result
	 * @throws IOException exception if error while writing the file.
	 */
	private Itemsets run(String output) throws IOException {

		// if the user want to keep the result into memory
		if (output == null) {
			writer = null;
			frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
		} else { // if the user want to save the result to a file
			frequentItemsets = null;
			writer = new BufferedWriter(new FileWriter(output));
		}

		// reset the number of itemset found to 0
		itemsetCount = 0;
		
		// record the start timestamp
		startTimestamp = System.currentTimeMillis();

		// A set that will contains all transactions IDs
		Set<Integer> allTIDS = new HashSet<Integer>();

		// (1) First database pass : calculate tidsets of each item.
		int maxItemId = 0;
		// This map will contain the tidset of each item
		// Key: item   Value :  tidset
		final Map<Integer, Set<Integer>> mapItemCount = new HashMap<Integer, Set<Integer>>();
		// for each transaction
		for (int i = 0; i < database.size(); i++) {
			// add the transaction id to the set of all transaction ids
			allTIDS.add(i); 
			// for each item in that transaction
			for (Integer item : database.getTransactions().get(i)) {
				// add the transaction ID to the tidset of that item
				Set<Integer> set = mapItemCount.get(item);
				if (set == null) {
					set = new HashSet<Integer>();
					mapItemCount.put(item, set);
					// if the current item is larger than all items until
					// now, remember that!
					if (item > maxItemId) {
						maxItemId = item;
					}
				}
				set.add(i); // add tid to the tidset of the item
			}
		}

		// if the user chose to use the triangular matrix optimization
		// for containing the support of itemsets of size 2.
		if (useTriangularMatrixOptimization) {
			// (1.b) create the triangular matrix for counting the support of
			// itemsets of size 2
			// for optimization purposes.
			matrix = new TriangularMatrix(maxItemId + 1);
			// for each transaction, take each itemset of size 2,
			// and update the triangular matrix.
			for (List<Integer> itemset : database.getTransactions()) {
				Object[] array = itemset.toArray();
				// for each item i in the transaction
				for (int i = 0; i < itemset.size(); i++) {
					Integer itemI = (Integer) array[i];
					// compare with each other item j in the same transaction
					for (int j = i + 1; j < itemset.size(); j++) {
						Integer itemJ = (Integer) array[j];
						// update the matrix count by 1 for the pair i, j
						matrix.incrementCount(itemI, itemJ);
					}
				}
			}
		}

		// (2) create ITSearchTree with the empty set as root node
		ITSearchTree tree = new ITSearchTree();
		// add the empty set
		ITNode root = new ITNode(new Itemset());
		// the empty set as all tids as its tidset
		root.setTidset(allTIDS);
		tree.setRoot(root);

		// (3) create a child node of the root node for each frequent item.
		
		// For each item
		for (Entry<Integer, Set<Integer>> entry : mapItemCount.entrySet()) {
			//if the item is frequent
			if (entry.getValue().size() >= minsupRelative) {
				// create a  new node for that item
				Itemset itemset = new Itemset();
				itemset.addItem(entry.getKey());
				ITNode newNode = new ITNode(itemset);
				// set its tidset as the tidset that we have calculated previously
				newNode.setTidset(entry.getValue());
				// set its parent as the root
				newNode.setParent(root);
				// add the new node as child of the root node
				root.getChildNodes().add(newNode);
			}
		}

		// save root node
		// save(root);

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

		// close the output file if the result was saved to a file
		if (writer != null) {
			writer.close();
		}

		// record the end time for statistics
		endTimestamp = System.currentTimeMillis();

		// Return all frequent closed itemsets found.
		return frequentItemsets; 
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
			// if the brother is not the current node
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
				else if (brother.getTidset().containsAll(currNode.getTidset())) {
					// Same as previous if condition except that we
					// do not delete the brother.
					replaceInSubtree(currNode, brother.getItemset());
					i++;
				}
				// Property 3
				// If the tidset of the current node contains the tidset of the
				// brother
				else if (currNode.getTidset().containsAll(brother.getTidset())) {
					// Generate a candidate by performing
					// the union of the itemsets of the current node and its brother
					ITNode candidate = getCandidate(currNode, brother);
					// delete the brother
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
				i++;  // go to next node
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
			delete(child); // then delte it
		}
	}

	/**
	 * Replace the itemset of a current node by another itemset in 
	 * a subtree (including the current node).
	 * @param currNode the current node.
	 * @param itemset  the itemset
	 */
	private void replaceInSubtree(ITNode currNode, Itemset itemset) {
		// make the union
		Itemset union = new Itemset();
		union.getItems().addAll(currNode.getItemset().getItems());
		union.getItems().addAll(itemset.getItems());
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
		// optimization: if these node are itemsets of size 1, we just check the
		// triangular matrix to know their support. If they are not frequent,
		// then we don't need to calculate the list of common tids.
		if (useTriangularMatrixOptimization
				&& currNode.getItemset().size() == 1) {
			// Get the support of the first item of each itemset
			int support = matrix.getSupportForItems((Integer) currNode
					.getItemset().getItems().toArray()[0], (Integer) brother
					.getItemset().getItems().toArray()[0]);
			// if not frequent
			if (support < minsupRelative) {
				// return null because the candidate would not be a frequent closed itemset
				return null;
			}
		}

		// create list of common tids of the itemset of the current node
		// and the brother node
		Set<Integer> commonTids = new HashSet<Integer>();
		// for each tid in the tidset of the current node
		for (Integer tid : currNode.getTidset()) {
			// if it is in the tidset of the brother node
			if (brother.getTidset().contains(tid)) {
				// add it to the set of common tids
				commonTids.add(tid);
			}
		}

		// (2) check if the two itemsets have enough common tids
		// if not, we don't need to generate a rule for them.
		
		// if the common tids cardinality is enough for the minimum support
		if (commonTids.size() >= minsupRelative) {
			// perform the union of the itemsets
			Itemset union = currNode.getItemset().union(brother.getItemset());
			// create a new node with the union
			ITNode node = new ITNode(union);
			// set the tidset as the intersection of the tids of both itemset
			node.setTidset(commonTids);
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
	 * @throws IOException
	 */
	private void save(ITNode node) throws IOException {
		// get the itemset of that node and set its tidset
		Itemset itemset = node.getItemset();
		itemset.setTidset(node.getTidset());

		// if it has no superset already in the hash table
		// it is a frequent closed itemset
		if (!hash.containsSupersetOf(itemset)) {
			// increase the itemset count
			itemsetCount++;
			// if the result should be saved to memory
			if (writer == null) {
				// save it to memory
				frequentItemsets.addItemset(itemset, itemset.size());
			} else {
				// otherwise if the result should be saved to a file,
				// then write it to the output file
				writer.write(node.getItemset().toString() + " #SUP: "
						+ node.getTidset().size());
				writer.newLine();
			}
			// add the itemset to the hashtable
			hash.put(itemset);
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
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Frequent closed itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}

	/**
	 * Get the set of frequent closed itemsets found by Charm.
	 * @return the set of frequent closed itemsets.
	 */
	public Itemsets getClosedItemsets() {
		return frequentItemsets;
	}
}
