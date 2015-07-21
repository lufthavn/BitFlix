package messages;

public class Piece extends Message {

	private int index;
	private int begin;
	private byte[] block;
	
	public Piece(int index, int begin, byte[] block) {
		this.index = index;
		this.begin = begin;
		this.block = block;
	}

	@Override
	public MessageType getType(){
		return MessageType.PIECE;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the begin
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * @return the block
	 */
	public byte[] getBlock() {
		return block;
	}

}
