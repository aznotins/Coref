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
package integration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import lv.coref.data.NamedEntity;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.PipeClient;
import lv.label.Annotation;
import lv.pipe.Pipe;
import lv.util.FileUtils;

public class SmokeTest {
	private final static Logger log = Logger.getLogger(SmokeTest.class.getName());

	public static boolean USE_PIPE_CLIENT = true;

	public static void main(String[] args) throws IOException {
		Config.logInit();

//		testFolder(new File("data/text_corpus/txt/"), 100);
		printNer(FileUtils.getFiles("data/text_corpus/txt/", -1, -1, ".txt"));

		Pipe.close();
		System.exit(0);
	}

	public static void testFolder(File folder, int limit) throws IOException {
		int c = 0;
		for (File f : folder.listFiles()) {
			log.severe(f.getName());
			if (c++ > limit)
				break;
			if (!f.isFile())
				continue;
			String stringText = FileUtils.readFile(f.getAbsolutePath());

			
			if (USE_PIPE_CLIENT) {
				Text t = PipeClient.getInstance().getText(stringText);
			} else {
				Annotation a = Pipe.getInstance().process(stringText);
			}
		}
	}
	
	public static void printNer(List<String> files) throws IOException {		
		for (String filename : files) {
			log.severe(filename);
			String stringText = FileUtils.readFile(filename);
			Text t = null;
			if (USE_PIPE_CLIENT) {
				t = PipeClient.getInstance().getText(stringText);				
			} else {
				Pipe.getInstance().setTools("tokenizer tagger ner");
				Annotation a = Pipe.getInstance().process(stringText);
				t = Annotation.makeText(a);
			}			
			for (Sentence s : t.getSentences()) {
				//System.err.println(s);
				for (NamedEntity ne : s.getNamedEntities()) {
//					System.err.println(s);
					
					if (!ne.getLabel().equals("organization")) continue;
					System.err.printf("%s\t\t\t%s\n", ne, s);
				}
			}
		}
	}

}
