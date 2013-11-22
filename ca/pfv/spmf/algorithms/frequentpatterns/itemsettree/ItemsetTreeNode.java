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


import java.util.HashSet;
import java.util.Set;
/**
 * This class represents an itemset-tree node for the itemset-tree data structure.
 * 
 * @author Philippe Fournier-Viger
 */
public class ItemsetTreeNode {
	
	int[] itemset;
	int support;
	ItemsetTreeNode parent;
	Set<ItemsetTreeNode> childs = new HashSet<ItemsetTreeNode>();
	
	public ItemsetTreeNode(int[] itemset, int support){
		this.itemset = itemset;
		this.support = support;
	}

	public String toString(StringBuffer buffer, String space){
		buffer.append(space);
		if(itemset == null){
			buffer.append("{}");
		}else{
			buffer.append("[");
			for(Integer item : itemset){
				buffer.append(item);
				buffer.append(" ");
			}
			buffer.append("]");
		}
		buffer.append("   sup=");
		buffer.append(support);
		buffer.append("\n");
		
		for(ItemsetTreeNode node : childs){
			node.toString(buffer, space + "  ");
		}
		return buffer.toString();
	}
	
	public String toString(){
		return toString(new StringBuffer(), "  ");
	}

	
	
}
