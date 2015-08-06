package files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import peers.IPieceQueue;

public class TorrentFileWriter implements Runnable {

	private boolean isRunning;
	
	private final IPieceQueue pieceQueue;
	private final PieceWriter writer;
	
	private long totalLength;
	private long totalWritten;
	
	public TorrentFileWriter(String baseDir, TorrentFile file, IPieceQueue queue) throws FileNotFoundException{
		this.pieceQueue = queue;
		this.writer = new PieceWriter(baseDir, file);
		this.isRunning = false;
		this.totalLength = file.getLength();
		this.totalWritten = 0;
	}
	
	@Override
	public void run() {
		this.isRunning = true;
		try {
			writer.reserve();
			while(isRunning){
				Piece p = pieceQueue.takePieceToWrite();
				writer.writePiece(p);
				totalWritten += p.getBytes().length;
				pieceQueue.addWrittenPiece(p);
				if(totalWritten == totalLength){
					System.out.println("all done with writing file");
					isRunning = false;
				}else if(totalWritten > totalLength){
					System.err.println("More bytes than the length of the torrent were written. Somehow.");
					isRunning = false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop(){
		isRunning = false;
	}

}
