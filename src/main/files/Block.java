package files;

public class Block {

	private int pieceIndex;
	private int begin;
	private final byte[] bytes;

	public Block(int pieceIndex, int begin, byte[] bytes){
		this.pieceIndex = pieceIndex;
		this.begin = begin;
		this.bytes = bytes;
	}
	
	/**
	 * @return the pieceIndex
	 */
	public int getPieceIndex() {
		return pieceIndex;
	}

	/**
	 * @return the begin
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * @return the bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

}
