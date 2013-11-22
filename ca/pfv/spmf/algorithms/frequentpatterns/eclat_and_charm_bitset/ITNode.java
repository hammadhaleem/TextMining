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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_set_integers_with_tids_bitset.Itemset;

/**
 * This class represents an ITNode from the ITSearch Tree used by bitset versions of Charm and Eclat.
 * 
 * @see ITSearchTree
 * @see AlgoEclat_Bitset_saveToFile
 * @see AlgoCharm_Bitset_saveToFile
 * @author Philippe Fournier-Viger
 */
class ITNode {
	// the itemset stored in that node
	Itemset itemsetObject = new Itemset();
	//the parent node
	private ITNode parent = null;
	// the child nodes
	private List<ITNode> childNodes = new ArrayList<ITNode>();
	
	/**
	 * Get the number of tids in that node.
	 * @return the tidset cardinality.
	 */
	public int size(){
		return itemsetObject.cardinality;
	}
	

	/**
	 * Get the relative support of that node (percentage).
	 * @param nbObject the number of transaction in the database
	 * @return the support
	 */
	public double getRelativeSupport(int nbObject) {
		return ((double) itemsetObject.cardinality) / ((double) nbObject);
	}
	
	/**
	 * Constructor of the node.
	 * @param itemset the itemset for that node.
	 */
	public ITNode(Set<Integer> itemset){
		this.itemsetObject.itemset = itemset;
	}

	/**
	 * Get the itemset of that node.
	 * @return an Itemset.
	 */
	public Set<Integer> getItemset() {
		return itemsetObject.itemset;
	}

	/**
	 * Get the tidset of that node
	 * @return the tidset as a Set of Integers.
	 */
	public BitSet getTidset() {
		return itemsetObject.tidset;
	}

	/**
	 * Set the tidset of that node.
	 * @param tidset 
	 */
	public void setTidset(BitSet tidset, int cardinality) {
		this.itemsetObject.tidset = tidset;
		this.itemsetObject.cardinality = cardinality;
	}

	/**
	 * Get the child nodes of this node
	 * @return a list of ITNodes.
	 */
	public List<ITNode> getChildNodes() {
		return childNodes;
	}

	/**
	 * Get the parent of this node
	 * @return a node or null if no parent.
	 */
	public ITNode getParent() {
		return parent;
	}

	/**
	 * Set the parent of this node to a given node.
	 * @param parent the given node.
	 */
	public void setParent(ITNode parent) {
		this.parent = parent;
	}
	
	/**
	 * Method used by Charm to replace all itemsets in the subtree defined
	 * by this node as the itemsets union a replacement itemset.
	 * @param replacement the replacement itemset
	 */
	void replaceInChildren(Set<Integer> replacement) {
		// for each child node
		for(ITNode node : getChildNodes()){
			// get the itemset of the child node
			Set<Integer> itemset  = node.getItemset();
			// could be optimized... not very efficient..
			// in particular, instead of using a list in itemset, we could use a
			// set.
			
			// for each item in the replacement
			for(Integer item : replacement){
				// if it is not in the itemset already
				if(!itemset.contains(item)){
					// add it
					itemset.add(item);
				}
			}
			// recursive call for the children of the current node
			node.replaceInChildren(replacement);
		}
	}

	/**
	 * Set the itemset of that node.
	 * @param itemset the itemset.
	 */
	public void setItemset(Set<Integer> itemset) {
		this.itemsetObject.itemset = itemset;
	}
}
