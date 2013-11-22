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

/**
 * This class represents an ITSearchTree and it is used by the bitset versions of Charm and Eclat.
 * 
 * @see ITNode
 * @see AlgoEclat_Bitset_saveToFile
 * @see AlgoCharm_Bitset_saveToFile
 * @author Philippe Fournier-Viger
 */
class ITSearchTree {
	// the root node
	private ITNode root;

	/**
	 * Default constructor.
	 */
	public ITSearchTree() {

	}

	/**
	 * Set the root node of the tree as a given node.
	 * @param root the given node
	 */
	public void setRoot(ITNode root) {
		this.root = root;
	}

	/**
	 * Get the root node of the tree.
	 * @return an ITNode.
	 */
	public ITNode getRoot() {
		return root;
	}

}
