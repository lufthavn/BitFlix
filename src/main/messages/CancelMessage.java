package messages;

import java.nio.ByteBuffer;

public class CancelMessage extends Message {
	
	private int index;
	private int begin;
	private int length;
	
	public CancelMessage(int index, int begin, int length) {
		this.index = index;
		this.begin = begin;
		this.length = length;
	}

	@Override
	public MessageType getType(){
		return MessageType.CANCEL;
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
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	@Override
	public ByteBuffer getBytes() {
		return ByteBuffer.allocate(17)
				.putInt(13) //length
				.put((byte) MessageType.CANCEL.getValue())
				.putInt(index)
				.putInt(begin)
				.putInt(length);
	}
	
	
}
