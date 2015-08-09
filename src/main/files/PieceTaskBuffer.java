package files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PieceTaskBuffer implements IPieceTaskBuffer {

	private final BlockingQueue<Piece> writeQueue;
	private final List<Piece> writtenPieces;
	
	public PieceTaskBuffer(){
		this.writeQueue = new LinkedBlockingQueue<Piece>();
		this.writtenPieces = Collections.synchronizedList(new ArrayList<Piece>());
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#putPieceToWrite(files.Piece)
	 */
	@Override
	public void putPieceToWrite(Piece piece){
		try {
			writeQueue.put(piece);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#takePieceToWrite()
	 */
	@Override
	public Piece takePieceToWrite(){
		try {
			return writeQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#addWrittenPiece(files.Piece)
	 */
	@Override
	public void addWrittenPiece(Piece piece){
		writtenPieces.add(piece);
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#getWrittenePieces()
	 */
	@Override
	public List<Piece> getWrittenPieces(){
		ArrayList<Piece> pieces = new ArrayList<Piece>(writtenPieces.size());
		synchronized(writtenPieces){
			for(Piece p : writtenPieces){
				pieces.add(p);
			}
			writtenPieces.clear();
		}
		return pieces;
	}
	

}
