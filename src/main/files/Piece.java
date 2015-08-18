package files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.Hashing;

public class Piece {

	private final byte[] hash;
	private final byte[][] blocks;
	private final int blockSize;
	private final int index;
	private final int pieceLength;
	
	
	
	/**
	 * @param hash the SHA1 hash for this piece
	 * @param maxLength the piece length, specified in the meta info for the torrent.
	 * @param blockSize the size of the blocks this piece consists of
	 */
	public Piece(byte[] hash, int maxLength, int blockSize, int index){
		this.hash = hash;
		this.blockSize = blockSize;
		this.index = index;
		this.pieceLength = maxLength;
		int blockAmount = (int) Math.ceil(maxLength / (double) blockSize);
		blocks = new byte[blockAmount][];
	}
	

	/**
	 * 
	 * @param index
	 * @param b1
	 */
	public void addBlock(Block block) {
		int index = block.getBegin() / blockSize;
		if(blocks[index] == null){
			blocks[index] = block.getBytes();
		}
//		buffer.position(index * blockSize);
//		buffer.put(b1.getBytes(), 0, b1.getBytes().length);
	}
	
	/**
	 * @return the index of this piece in the entire torrent.
	 */
	public int getIndex() {
		return index;
	}

	public byte[] getBytes() {
		byte[] bytes = null;
		for(int i = 0; i < blocks.length; i++){
			bytes = org.apache.commons.lang3.ArrayUtils.addAll(bytes, blocks[i]);
		}
		
		return bytes;
	}


	public boolean checkHash() {
		return Arrays.equals(hash, Hashing.Sha1Hash(getBytes()));
	}
	
	/**
	 * @return the size of the next block to be added to this piece, starting from block at index 0. returns 0 if this piece is complete.
	 */
	public int nextBlockSize(){
		int length = blocks.length;
		for(int i = 0; i < length; i++){
			byte[] block = blocks[i];
			if(block == null){
				return sizeOfBlock(i);
			}
		}
		return 0;
	}
	
	private int sizeOfBlock(int blockIndex){
		if (blockIndex == (blocks.length -1)){
			int r = pieceLength % blockSize;
			if(r == 0){
				return blockSize;
			}
			return r;
		}
		return blockSize;
	}

	/**
	 * @return the index of the first byte of next block to be added to this piece, starting from block at index 0. returns -1 if this piece is complete.
	 */
	public int beginOfNextBlock(){
		for(int i = 0; i < blocks.length; i++){
			byte[] block = blocks[i];
			if(block == null){
				return beginOfBlock(i);
			}
		}
		return -1;
	}
	
	private int beginOfBlock(int blockIndex){
		return blockIndex * blockSize;
	}
	
	/**
	 * @return the info of all the missing blocks in this piece. Returns an empty array if piece is complete.
	 */
	public List<BlockInfo> missingBlocks(){
		List<BlockInfo> indexes = new ArrayList<BlockInfo>();
		
		for(int i = 0; i < blocks.length; i++){
			byte[] block = blocks[i];
			if(block == null){
				int begin = beginOfBlock(i);
				int length = sizeOfBlock(i);
				indexes.add(new BlockInfo(begin, length));
			}
		}
		
		return indexes;
	}

	public int remainingBlocks() {
		int amount = 0;
		for(byte[] b: blocks){
			if(b == null){
				amount++;
			}
		}
		return amount;
	}

	public static class BlockInfo{
		private int begin;
		private int length;

		public BlockInfo(int begin, int length){
			this.begin = begin;
			this.length = length;
		}

		/**
		 * @return the index
		 */
		public int getBegin() {
			return begin;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}
	}
}
