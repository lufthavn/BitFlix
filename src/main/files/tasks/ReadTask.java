package files.tasks;

public class ReadTask extends Task {

	private int pieceIndex;
	private int begin;

	public ReadTask(int pieceIndex, int begin) {
		super(TaskType.READ);
		this.pieceIndex = pieceIndex;
		this.begin = begin;
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

}
