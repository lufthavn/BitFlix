package bencoding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class BString implements BElement {

	private String value;
	private byte[] binaryData;
	
	public BString(byte[] data) {
		this.binaryData = data;
		try {
			value = new String(data, "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the binaryData
	 */
	public byte[] getBinaryData() {
		return binaryData;
	}

	@Override
	public void encode(OutputStream stream) {
		String length = String.valueOf(binaryData.length);
		try {
			stream.write(length.getBytes("ASCII"));
			stream.write(':');
			stream.write(binaryData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
