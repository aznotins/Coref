package lv.coref.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import lv.coref.data.Text;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;

public class CorefPipe {
	public static enum FORMAT {
		CONLL, JSON
	};

	private FORMAT input = FORMAT.CONLL;
	private FORMAT output = FORMAT.CONLL;
	private boolean solve = true;

	public static void help() {
		System.out.println("=== LVCoref v2.0 ===");
		System.out.println("INPUT:");
		System.out.println("\t--input [CONLL, JSON]");
		System.out.println("OUTPUT:");
		System.out.println("\t--input [CONLL, JSON]");
		System.out.println("OPTIONS:");
		System.out.println("\t--solve [yes, no]: resolve mentions and coreferences");
	}

	CorefPipe() {
	}

	CorefPipe(FORMAT input, FORMAT output) {
		this.input = input;
		this.output = output;
	}

	public void configure(String args[]) {
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			try {
				if (a.equalsIgnoreCase("--input"))
					input = FORMAT.valueOf(args[i + 1].toUpperCase());
				else if (a.equalsIgnoreCase("--output"))
					output = FORMAT.valueOf(args[i + 1].toUpperCase());
				else if (a.equalsIgnoreCase("--solve") && args[i + 1].equalsIgnoreCase("no"))
					solve = false;
				else if (a.equalsIgnoreCase("--help")
						|| a.equalsIgnoreCase("-h")) {
					help();
					System.exit(0);
				}
			} catch (Exception e) {
				System.err.println("Error while configuring");
				e.printStackTrace();
			}
		}
	}

	public Text read(ReaderWriter rw) {
		Text text = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in, "UTF8"));
			text = rw.read(in);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public void write(ReaderWriter rw, Text text) {
		try {
			rw.write(System.out, text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		ReaderWriter in = null;
		ReaderWriter out = null;
		if (input.equals(FORMAT.JSON))
			in = new JsonReaderWriter();
		else if (input.equals(FORMAT.CONLL))
			in = new ConllReaderWriter();
		if (input.equals(output))
			out = in;
		else {
			if (output.equals(FORMAT.JSON))
				out = new JsonReaderWriter();
			else if (output.equals(FORMAT.CONLL))
				out = new ConllReaderWriter();
		}

		while (true) {
			Text text = read(in);
			if (text == null || text.isEmpty())
				break;
			if (solve) {
				new MentionFinder().findMentions(text);
				new Ruler().resolve(text);	
			}
			write(out, text);
		}

	}

	public static void main(String args[]) {
		CorefPipe pipe = new CorefPipe();
		pipe.configure(args);
		pipe.run();
	}
}
