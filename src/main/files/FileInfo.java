/**
 * 
 */
package files;

/**
 * This represents the info of a single file, that's part of a torrent.
 * @author Tobias
 *
 */
public class FileInfo {
	private String path;
	private long size;
	
	public FileInfo(String path, long size) {
		this.setPath(path);
		this.size = size;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
