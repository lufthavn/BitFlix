package files;

import java.util.List;

public interface IPieceTaskBuffer {

	public void putPieceToWrite(Piece piece);

	public Piece takePieceToWrite();

	public void addWrittenPiece(Piece piece);

	public List<Piece> getWrittenPieces();

}