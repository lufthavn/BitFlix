package files;

import java.util.List;

import files.tasks.Result;
import files.tasks.Task;

public interface IPieceTaskBuffer {

	public void addTask(Task task);

	public Task takeTask();

	public void addResult(Result result);

	public List<Result> getCompletedTasks();

}