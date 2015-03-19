/*******************************************************************************
 * Copyright 2014,2015 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.coref.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lv.coref.data.Text;
import lv.coref.io.Config.FORMAT;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;
import lv.coref.semantic.NEL;

public class CorefPipe {
	private final static Logger log = Logger.getLogger(CorefPipe.class.getName());

	private InputStream inStream = System.in;
	private OutputStream outStream = System.out;
	private FORMAT input = FORMAT.CONLL;
	private FORMAT output = FORMAT.CONLL;

	private static CorefPipe corefPipe = null;

	public static CorefPipe getInstance() {
		if (corefPipe == null) {
			corefPipe = new CorefPipe();
		}
		return corefPipe;
	}

	public void init(String args[]) {
		// Check if user asked for help
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if (a.equalsIgnoreCase("--help") || a.equalsIgnoreCase("-h")) {
				help();
				System.exit(0);
			}
		}
		if (args.length == 0) {
			Config.getInstance(); // loads default configuration
			return;
		}
		Config.init(args);
		this.input = Config.getInstance().getINPUT();
		this.output = Config.getInstance().getOUTPUT();
		try {
			FileInputStream fis = new FileInputStream(Config.getInstance().get("prop"));
			LogManager.getLogManager().readConfiguration(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void help() {
		System.out.println("=== LVCoref v2.0 ===");
		System.out.println(new Config());
	}

	public CorefPipe() {
	}

	public CorefPipe(FORMAT input, FORMAT output) {
		this.input = input;
		this.output = output;
	}

	public void setInputStream(InputStream inStream) {
		this.inStream = inStream;
	}

	public void setOutputStream(OutputStream outStream) {
		this.outStream = outStream;
	}

	public Text read(ReaderWriter rw) {
		Text text = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "UTF8"));
			text = rw.read(in);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public void write(ReaderWriter rw, Text text) {
		try {
			rw.write(outStream, text, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		ReaderWriter in = null;
		ReaderWriter out = null;
		while (true) {
			if (input.equals(FORMAT.JSON))
				in = new JsonReaderWriter();
			else if (input.equals(FORMAT.CONLL))
				in = new ConllReaderWriter(ConllReaderWriter.TYPE.LETA);
			if (input.equals(output))
				out = in;
			else {
				if (output.equals(FORMAT.JSON))
					out = new JsonReaderWriter();
				else if (output.equals(FORMAT.CONLL))
					out = new ConllReaderWriter(ConllReaderWriter.TYPE.LETA);
			}

			Text text = read(in);
			// System.err.println("TEXT:\n" + text);
			if (text == null || text.isEmpty())
				break;

			if (Config.getInstance().getSOLVE()) {
				process(text);
			}
			write(out, text);
		}
		in.close();
		out.close();
	}

	public void process(Text text) {
		new MentionFinder().findMentions(text);
		new Ruler().resolve(text);
		text.removeCommonUnknownSingletons();
		if (Config.getInstance().isTrue(Config.PROP_KNB_ENABLE))
			NEL.getInstance().link(text);
	}

	public static void main(String args[]) {
		CorefPipe pipe = CorefPipe.getInstance();
		pipe.init(args);
		pipe.run();
	}
}
