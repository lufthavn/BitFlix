package files.tasks;

import files.Piece;

public class WriteResult extends Result {

	private Piece piece;

	public WriteResult(Piece piece) {
		super(TaskType.WRITE);
		this.piece = piece;
	}

	/**
	 * @return the piece
	 */
	public Piece getPiece() {
		return piece;
	}

}
