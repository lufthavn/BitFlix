package bencoding;

import java.io.IOException;
import java.io.OutputStream;

public class BInteger implements BElement {

	private long value;
	
	public BInteger(long value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	@Override
	public void encode(OutputStream stream) {
		String valueString = String.valueOf(value);
		try {
			stream.write('i');
			stream.write(valueString.getBytes("ASCII"));
			stream.write('e');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
