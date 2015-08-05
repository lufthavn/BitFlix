package files;

import java.util.Arrays;

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
	public void addBlock(int index, Block b1) {
		if(blocks[index] == null){
			blocks[index] = b1.getBytes();
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
				if (i == (length -1)){
					int r = pieceLength % blockSize;
					if(r == 0){
						return blockSize;
					}
					return r;
				}
				return blockSize;
			}
		}
		return 0;
	}

	/**
	 * @return the index of the first byte of next block to be added to this piece, starting from block at index 0. returns -1 if this piece is complete.
	 */
	public int indexOfNextBlock(){
		for(int i = 0; i < blocks.length; i++){
			byte[] block = blocks[i];
			if(block == null){
				return i * blockSize;
			}
		}
		return -1;
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

	
}
