package messages;

public class Request extends Message{
	
	private int index;
	private int begin;
	private int length;
	
	public Request(int index, int begin, int length) {
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
	
	
}
