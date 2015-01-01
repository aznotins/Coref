package lv.coref.util;

import java.io.OutputStream;

public class StringOutputStream extends OutputStream {

	StringBuilder sb = new StringBuilder();

	public StringOutputStream() {
	}

	synchronized public void clear() {
		sb.setLength(0);
	}

	@Override
	synchronized public void write(int i) {
		sb.append((char) i);
	}

	@Override
	synchronized public String toString() {
		return sb.toString();
	}

}
