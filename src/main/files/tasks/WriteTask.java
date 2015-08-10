package files.tasks;

import files.Piece;

public class WriteTask extends Task {

	private Piece piece;

	public WriteTask(Piece piece) {
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
