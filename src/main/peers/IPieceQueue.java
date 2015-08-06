package peers;

import java.util.List;

import files.Piece;

public interface IPieceQueue {

	public void putPieceToWrite(Piece piece);

	public Piece takePieceToWrite();

	public void addWrittenPiece(Piece piece);

	public List<Piece> getWrittenPieces();

}