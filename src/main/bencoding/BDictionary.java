package bencoding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;

public class BDictionary extends LinkedHashMap<String, BElement> implements BElement {

	@Override
	public void encode(OutputStream stream) {
		
		try {
			stream.write('d');
			
			for(java.util.Map.Entry<String, BElement> entry : this.entrySet())
			{
				BString key = new BString(entry.getKey().getBytes("UTF8"));
				BElement element = entry.getValue();
				key.encode(stream);
				element.encode(stream);
			}
			
			stream.write('e');
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
