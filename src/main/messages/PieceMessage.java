package messages;

import java.nio.ByteBuffer;

public class PieceMessage extends Message {

	private int index;
	private int begin;
	private byte[] block;
	
	public PieceMessage(int index, int begin, byte[] block) {
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

	@Override
	public ByteBuffer getBytes() {
		return ByteBuffer.allocate(13 + block.length)
				.putInt(9 + block.length) //length
				.put((byte) MessageType.PIECE.getValue())
				.putInt(index)
				.putInt(begin)
				.put(block);
	}

}
