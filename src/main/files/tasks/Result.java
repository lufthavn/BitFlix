package files.tasks;

public abstract class Result {

	private TaskType type;

	public Result(TaskType type){
		this.type = type;
	}
	
	/**
	 * @return the type
	 */
	public TaskType getType() {
		return type;
	}
}
