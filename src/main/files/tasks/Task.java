package files.tasks;

public abstract class Task {
	private TaskType type;
	
	public Task(TaskType type){
		this.type = type;
	}
	
	public TaskType getType(){
		return type;
	}
}
