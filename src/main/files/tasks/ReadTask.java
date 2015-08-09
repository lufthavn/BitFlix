package files.tasks;

public class ReadTask extends Task {

	private int pieceIndex;
	private int begin;
	private int length;
	
	public ReadTask(int pieceIndex, int begin, int length) {
		super(TaskType.READ);
		this.pieceIndex = pieceIndex;
		this.begin = begin;
		this.length = length;
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
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

}
