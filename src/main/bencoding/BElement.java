package bencoding;

import java.io.OutputStream;

public interface BElement {
	void encode(OutputStream stream);
}
