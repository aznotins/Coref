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

	String fileID;

	public abstract Text read(BufferedReader in) throws Exception;

	public abstract void write(PrintStream out, Text t) throws Exception;

	protected abstract void initialize(Text t);

	public Text read(String filename) throws Exception {
		setFileID(new File(filename).getPath());
		BufferedReader br = new BufferedReader(new FileReader(filename));
		return read(br);
	}

	public void write(String filename, Text t) throws Exception {
		PrintStream ps = new PrintStream(new FileOutputStream(filename));
		write(ps, t);
		ps.close();
	}

	public void write(OutputStream out, Text t) throws Exception {
		PrintStream ps = new PrintStream(out, true, "UTF-8");
		write(ps, t);
		ps.close();
	};

	public void write(String filename, Collection<Text> texts) throws Exception {
		PrintStream ps = new PrintStream(new FileOutputStream(filename));
		for (Text t : texts) {
			initialize(t);
			write(ps, t);
		}
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

}
