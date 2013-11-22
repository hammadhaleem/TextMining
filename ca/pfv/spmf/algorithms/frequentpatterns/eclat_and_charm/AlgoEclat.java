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
 * This is an implementation of the ECLAT algorithm as proposed by ZAKI (2000).
 * <br/><br/>
 * 
 * See this article for details about ECLAT:
 * <br/><br/>
 * 
 * Zaki, M. J. (2000). Scalable algorithms for association mining. Knowledge and Data Engineering, IEEE Transactions on, 12(3), 372-390.
 * <br/><br/>
 * 
 * This implementation may not be fully optimized. In particular, Zaki proposed
 * various extensions that I have not implemented (e.g. diffsets).
 * <br/><br/>
 * 
 * This  version  saves the result to a file
 * or keep it into memory if no output path is provided
 * by the user to the runAlgorithm method().
 * 
 * @see TriangularMatrix
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @see ITSearchTree
 * @author Philippe Fournier-Viger
 */
public class AlgoEclat {

	// parameters
	private int minsupRelative;  // relative minimum support
	private TransactionDatabase database; // the transaction database

	// for statistics
	private long startTimestamp; // start time of the last execution
	private long endTime; // end  time of the last execution
	
	// results
	// The  patterns that are found 
	// (if the user want to keep them into memory)
	protected Itemsets frequentItemsets;
	BufferedWriter writer = null; // object to write the output file
	private int itemsetCount; // the number of patterns found
	
	// For optimization with a triangular matrix for counting 
	// itemsets of size 2. 
	private TriangularMatrix matrix; // the triangular matrix
	private boolean useTriangularMatrixOptimization; // if the user want to use this optimization

	/**
	 * Default constructor
	 */
	public AlgoEclat() {
		
	}


	/**
	 * Run the algorithm.
	 * @param database a transaction database
	 * @param output an output file path for writing the result or if null the result is saved into memory and returned
	 * @param minsupp the minimum support
	 * @param useTriangularMatrixOptimization if true the triangular matrix optimization will be applied.
	 * @return the result
	 * @throws IOException exception if error while writing the file.
	 */
	public Itemsets runAlgorithm(String output, TransactionDatabase database, double minsupp,
			boolean useTriangularMatrixOptimization) throws IOException {
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			frequentItemsets =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
	    	frequentItemsets = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}

		// reset the number of itemset found to 0
		itemsetCount =0;

		this.database = database;
		
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// convert from an absolute minsup to a relative minsup by multiplying
		// by the database size
		this.minsupRelative = (int) Math.ceil(minsupp * database.size());
		
		// save the user preference about using the triangular matrix
		// optimization or not
		this.useTriangularMatrixOptimization = useTriangularMatrixOptimization;

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

		// (2) create ITSearchTree with root node
		ITSearchTree tree = new ITSearchTree();
		// add the empty set
		ITNode root = new ITNode(new Itemset());
		// the empty set as all tids as its tidset
		root.setTidset(allTIDS);
		tree.setRoot(root);

		// (3) create childs of the root node.
		for (Entry<Integer, Set<Integer>> entry : mapItemCount.entrySet()) {
			// we only add nodes for items that are frequents
			if (entry.getValue().size() >= minsupRelative) {
				// create a new node for that item
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
		save(root);

		// for optimization
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
		if(writer != null){
			writer.close();
		}
		
		// record the end time for statistics
		endTime = System.currentTimeMillis();

		return frequentItemsets; // Return all frequent itemsets found!
	}

	/**
	 * This is the "extend" method as described in the paper to extend
	 * a given node.
	 * @param currNode the current node.
	 * @throws IOException exception if error while writing to file.
	 */
	private void extend(ITNode currNode) throws IOException {
		// loop over the brothers
		for (ITNode brother : currNode.getParent().getChildNodes()) {
			// if the brother is not the current node
			if (brother != currNode) {
				// try to generate a candidate by doing the union
				// of the itemset of the current node and the brother
				ITNode candidate = getCandidate(currNode, brother);
				// if a candidate was generated (with enough support)
				if (candidate != null) {
					// add the candidate as a child of the current node
					currNode.getChildNodes().add(candidate);
					candidate.setParent(currNode);
				}
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
			// Get the support of the first item of each itemset by using the triangular matrix
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
		// increase the itemset count
		itemsetCount++;
		// if the result should be saved to memory
		if(writer == null){
			// add it to the set of frequent itemsets
			Itemset itemset = node.getItemset();
			itemset.setTidset(node.getTidset());
			frequentItemsets.addItemset(itemset, itemset.size());
		}else{
			// if the result should be saved to a file
			// write it to the output file
			writer.write(node.getItemset().toString() + " #SUP: " + node.getTidset().size());
			writer.newLine();
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
		System.out.println("=============  ECLAT - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Frequent itemsets count : "
				+ itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}

	/**
	 * Get the set of frequent itemsets.
	 * @return the frequent itemsets (Itemsets).
	 */
	public Itemsets getItemsets() {
		return frequentItemsets;
	}
}
