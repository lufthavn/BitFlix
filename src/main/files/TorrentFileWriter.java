package files;

import java.io.FileNotFoundException;
import java.io.IOException;
import files.tasks.ReadResult;
import files.tasks.ReadTask;
import files.tasks.Task;
import files.tasks.WriteResult;
import files.tasks.WriteTask;

public class TorrentFileWriter implements Runnable {

	private boolean isRunning;
	
	private final IPieceTaskBuffer buffer;
	private final PieceWriter writer;
	
	private long totalLength;
	private long totalWritten;
	
	public TorrentFileWriter(String baseDir, TorrentFile file, IPieceTaskBuffer queue) throws FileNotFoundException{
		this.buffer = queue;
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
				Task task = buffer.takeTask();
				switch(task.getType()){
				case READ:
					read((ReadTask)task);
					break;
				case WRITE:
					write((WriteTask) task);
					break;
				default:
					break;
				
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void read(ReadTask task) throws IOException {
		byte[] data = writer.read(task.getPieceIndex(), task.getBegin(), task.getLength());
		ReadResult result = new ReadResult(task.getPieceIndex(), task.getBegin(), data, task.getPeer());
		buffer.addResult(result);
	}

	private void write(WriteTask task) throws IOException {
		
		Piece p = task.getPiece();
		writer.writePiece(p);
		totalWritten += p.getBytes().length;
		
		WriteResult result = new WriteResult(p);
		buffer.addResult(result);;
		if(totalWritten == totalLength){
			System.out.println("all done with writing file");
			isRunning = false;
		}else if(totalWritten > totalLength){
			System.err.println("More bytes than the length of the torrent were written. Somehow.");
			isRunning = false;
		}
		
	}

	public void stop(){
		isRunning = false;
	}

}
