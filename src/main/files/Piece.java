package files;

import java.nio.ByteBuffer;
import java.util.Arrays;

import util.Hashing;

public class Piece {

	private final byte[] hash;
	private final ByteBuffer buffer;
	private final int blockSize;
	
	
	/**
	 * @param hash the SHA1 hash for this piece
	 * @param maxLength the piece length, specified in the meta info for the torrent.
	 * @param blockSize the size of the blocks this piece consists of
	 */
	public Piece(byte[] hash, int maxLength, int blockSize){
		this.hash = hash;
		this.blockSize = blockSize;
		buffer = ByteBuffer.allocate(maxLength);
	}
	

	public void addBlock(int index, Block b1) {
		buffer.position(index * blockSize);
		buffer.put(b1.getBytes(), 0, b1.getBytes().length);
	}
	
	/**
	 * @return the index of this piece in the entire torrent.
	 */
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte[] getBytes() {
		return buffer.array();
	}


	public boolean checkHash() {
		return Arrays.equals(hash, Hashing.Sha1Hash(getBytes()));
	}

	
}
