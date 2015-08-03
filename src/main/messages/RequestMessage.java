package messages;

import java.nio.ByteBuffer;

public class RequestMessage extends Message{
	
	private int index;
	private int begin;
	private int length;
	
	/**
	 * @param index the zero-based piece index  
	 * @param begin the zero-based byte offset within the piece
	 * @param length the requested length
	 */
	public RequestMessage(int index, int begin, int length) {
		this.index = index;
		this.begin = begin;
		this.length = length;
	}

	@Override
	public MessageType getType(){
		return MessageType.REQUEST;
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
				.put((byte) MessageType.REQUEST.getValue())
				.putInt(index)
				.putInt(begin)
				.putInt(length);
	}
	
	
}
