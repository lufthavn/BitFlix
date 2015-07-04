package files;

public class Block {

	private final byte[] bytes;

	public Block(byte[] bytes){
		this.bytes = bytes;
	}

	/**
	 * @return the bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

}
