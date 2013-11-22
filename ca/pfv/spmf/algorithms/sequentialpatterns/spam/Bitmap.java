package ca.pfv.spmf.algorithms.sequentialpatterns.spam;
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

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a bitmap as used by the SPAM algorithm.
 * 
 * @see AlgoSPAM
* @author Philippe Fournier-Viger
 */
class Bitmap {
	/** A bitmap is implemented using the BitSet class of Java.*/
	private BitSet bitmap = new BitSet();   
	
	/** For calculating the support more efficiently
	 we keep some information: */
	private int lastSID = -1;  // the sid of the last sequence inserted in that bitmap that contains a bit set to 1
	private int support = 0;  // the number of bits that are currently set to 1
	
	/**
	 * Constructor
	 * @param lastBitIndex the desired size of the bitset minus 1
	 */
	Bitmap(int lastBitIndex){
		this.bitmap = new BitSet(lastBitIndex+1); 
	}
	
	/**
	 * Constructor
	 * @param bitmap  a bitset to initialize this Bitmap.
	 */
	private Bitmap(BitSet bitmap){
		this.bitmap = bitmap; 
	}

	/**
	 * Set a bit to 1 in this bitmap
	 * @param sid the sid corresponding to that bit
	 * @param tid the tid corresponding to that bit
	 * @param sequencesSize the list of sequence length to know how many bits are allocated to each sequence
	 */
	public void registerBit(int sid, int tid, List<Integer> sequencesSize) {
		// calculate the position of the bit that we need to set to 1
		int pos = sequencesSize.get(sid) + tid;
		// set the bit to 1
		bitmap.set(pos, true);
		
		// Update the  count of bit set to 1
		if(sid != lastSID){
			support++;
		}
		// remember the last SID with a bit set to 1
		lastSID = sid;
	}
	
	/**
	 * Given the position of a bit, return the corresponding sequence ID.
	 * @param bit  the position of the bit in the bitmap
	 * @param sequencesSize  the list of lengths of sequence by sequence ID.
	 * @return the corresponding sequence ID
	 */
	private int bitToSID(int bit, List<Integer> sequencesSize) {
		// Do a binary search
		int result = Collections.binarySearch(sequencesSize, bit);
		if(result >= 0){
			return result;
		}
		return 0 - result -2;
	}

	/**
	 * Get the support of this bitmap (the number of bits set to 1)
	 * @return the support.
	 */
	public int getSupport() {
		return support;
	}

	/**
	 * Create a new bitmap for the s-step by doing a AND between this
	 * bitmap and the bitmap of an item.
	 * @param bitmapItem  the bitmap of the item used for the S-Step
	 * @param sequencesSize the  sequence lengths
	 * @param lastBitIndex  the last bit index
	 * @return return the new bitmap
	 */
	Bitmap createNewBitmapSStep(Bitmap bitmapItem, List<Integer> sequencesSize, int lastBitIndex) {
		// create a new bitset that will be use for the new bitmap
		BitSet newBitset = new BitSet(lastBitIndex); 
		// create the new bitmap
		Bitmap newBitmap = new Bitmap(newBitset);
		
		// We do an AND with the bitmap of the item and this bitmap
		for (int bitK = bitmap.nextSetBit(0); bitK >= 0; bitK = bitmap.nextSetBit(bitK+1)) {
			
			// find the sid of this bit
			int sid = bitToSID(bitK, sequencesSize);
			// get the last bit for this sid
 			int lastBitOfSID = lastBitOfSID(sid, sequencesSize, lastBitIndex);
			
			boolean match = false;
			for (int bit = bitmapItem.bitmap.nextSetBit(bitK+1); bit >= 0 && bit <= lastBitOfSID; bit = bitmapItem.bitmap.nextSetBit(bit+1)) {
				newBitmap.bitmap.set(bit);
				match = true;
			}
			if(match){
				// update the support
				if(sid != newBitmap.lastSID){
					newBitmap.support++;
				}
				newBitmap.lastSID = sid;
			}
			bitK = lastBitOfSID; // to skip the bit from the same sequence
		}

		// We return the resulting bitmap
		return newBitmap;
	}

	private int lastBitOfSID(int sid, List<Integer> sequencesSize, int lastBitIndex) {
		if(sid+1 >= sequencesSize.size()){
			return lastBitIndex;
		}else{
			return sequencesSize.get(sid+1) -1;
		}
	}


	/**
	 * Create a new bitmap by performing the I-STEP with this
	 * bitmap and the bitmap of an item.
	 * @param bitmapItem the bitmap of the item
	 * @param sequencesSize the sequence lengths
	 * @param lastBitIndex the last bit index
	 * @return the new bitmap
	 */
	Bitmap createNewBitmapIStep(Bitmap bitmapItem, List<Integer> sequencesSize, int lastBitIndex) {
		// We create the new bitmap
		BitSet newBitset = new BitSet(lastBitIndex); // TODO: USE LAST SET BIT
		Bitmap newBitmap = new Bitmap(newBitset);
		
		// We do an AND with the bitmap of the item
		for (int bit = bitmap.nextSetBit(0); bit >= 0; bit = bitmap.nextSetBit(bit+1)) {
			if(bitmapItem.bitmap.get(bit)){ // if both bits are TRUE
				
				// set the bit
				newBitmap.bitmap.set(bit);
				// update the support
				int sid = bitToSID(bit, sequencesSize);
				if(sid != newBitmap.lastSID){
					newBitmap.support++;
				}
				newBitmap.lastSID = sid; // remember the last SID
			}
		}
		// Then do the AND
		newBitset.and(bitmapItem.bitmap);
		
		// We return the resulting bitmap
		return newBitmap;
	}
}
