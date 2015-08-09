package files.tasks;

public class ReadResult extends Result {

	private int pieceIndex;
	private int begin;
	private byte[] block;

	public ReadResult(int pieceIndex, int begin, byte[] block) {
		super(TaskType.READ);
		this.pieceIndex = pieceIndex;
		this.begin = begin;
		this.block = block;
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
	 * @return the block
	 */
	public byte[] getBlock() {
		return block;
	}

}
