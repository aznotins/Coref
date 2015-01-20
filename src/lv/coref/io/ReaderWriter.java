package lv.coref.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import lv.coref.data.Text;

public abstract class ReaderWriter {

	private String fileID;

	private boolean readingCoreferences = true;

	public abstract Text read(BufferedReader in) throws Exception;

	public abstract void write(PrintStream out, Text t) throws Exception;

	protected abstract void initialize(Text t);

	public Text read(String filename) throws Exception {
		setFileID(new File(filename).getPath());
		BufferedReader br = new BufferedReader(new FileReader(filename));
		Text text = read(br);
		br.close();
		return text;
	}

	public void write(String filename, Text t) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		PrintStream ps = new PrintStream(fos);
		write(ps, t);
		ps.close();
		fos.close();
	}
	
	private void write(OutputStream out, Text t, boolean close) throws Exception {
		PrintStream ps = new PrintStream(out, true, "UTF-8");
		write(ps, t);
		if (close) ps.close();
	}

	public void write(OutputStream out, Text t) throws Exception {
		write(out, t, true);
	};

	public void write(String filename, Collection<Text> texts) throws Exception {
		PrintStream ps = new PrintStream(new FileOutputStream(filename));
		for (Text t : texts) {
			initialize(t);
			write(ps, t, false);
		}
		ps.close();
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public boolean isReadingCoreferences() {
		return readingCoreferences;
	}

	public void setReadingCoreferences(boolean readingCoreferences) {
		this.readingCoreferences = readingCoreferences;
	}

}
