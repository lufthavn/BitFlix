package files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import files.tasks.Result;
import files.tasks.Task;

public class PieceTaskBuffer implements IPieceTaskBuffer {

	private final BlockingQueue<Task> taskQueue;
	private final List<Result> results;
	
	public PieceTaskBuffer(){
		this.taskQueue = new LinkedBlockingQueue<Task>();
		this.results = Collections.synchronizedList(new ArrayList<Result>());
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#putPieceToWrite(files.Piece)
	 */
	@Override
	public void addTask(Task task){
		try {
			taskQueue.put(task);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#takePieceToWrite()
	 */
	@Override
	public Task takeTask(){
		try {
			return taskQueue.take();
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
	public void addResult(Result result){
		results.add(result);
	}
	
	/* (non-Javadoc)
	 * @see peers.IPieceQueue#getWrittenePieces()
	 */
	@Override
	public List<Result> getCompletedTasks(){
		ArrayList<Result> resultsToReturn = new ArrayList<Result>(results.size());
		synchronized(results){
			for(Result r : results){
				resultsToReturn.add(r);
			}
			results.clear();
		}
		return resultsToReturn;
	}
	

}
