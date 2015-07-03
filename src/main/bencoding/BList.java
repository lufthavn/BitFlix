package bencoding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public class BList extends LinkedList<BElement> implements BElement  {

	@Override
	public void encode(OutputStream stream) {
		try {
			stream.write('l');
			for(BElement element : this)
			{
				element.encode(stream);
			}
			stream.write('e');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
