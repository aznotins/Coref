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
package lv.coref.tests;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.MentionChain;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.ConllReaderWriter;
import lv.coref.io.JsonReaderWriter;
import lv.coref.io.MmaxReaderWriter;
import lv.coref.io.ReaderWriter;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;
import lv.coref.score.SummaryScorer;
import lv.util.StringUtils;

public class CorefCorpus {
		
	public static void prepare_mkTest_2015Jan() {
		String inFolder = "data/mktest_2015-jan/json_orig/";
		String outFolder = "data/mktest_2015-jan/prepared_mmax/";
		
		List<String> files = new ArrayList<>();
		files.add(inFolder + "personibas.json");
		files.add(inFolder + "melbarde.json");
		files.add(inFolder + "idejaslatvijai.json");
		files.add(inFolder + "seile.json");
		files.add(inFolder + "dombrovskis.json");
		files.add(inFolder + "test_taube.json");
		
		try {
			for (String file : files) {
					ReaderWriter in = new JsonReaderWriter();
					ReaderWriter out = new ConllReaderWriter();
					MmaxReaderWriter mrw = new MmaxReaderWriter();
					Text text = in.read(file);
					
					new MentionFinder().findMentions(text);
					new Ruler().resolve(text);
					//text.removeCommonSingletons();
					text.removeCommonUnknownSingletons();
					
					System.err.println(file);
					System.err.println(text);
					String name = StringUtils.getBaseName(file, ".json");
					mrw.write(outFolder + name, text);
				}
			}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void evaluate_mkTest_2015Jan() {
		String inFolder = "data/mktest_2015-jan/json_orig/";
		String goldFolder = "data/mktest_2015-jan/";
		
		List<String> files = new ArrayList<>();
		files.add(inFolder + "test_taube.json");
		files.add(inFolder + "seile.json");
//		files.add(inFolder + "melbarde.json");
//		files.add(inFolder + "personibas.json");
//		files.add(inFolder + "idejaslatvijai.json");
//		files.add(inFolder + "dombrovskis.json");
		
		List<String> goldFiles = new ArrayList<>();
		for (String file : files) {
			goldFiles.add(goldFolder + StringUtils.getBaseName(file, ".json"));
		}		
		
		SummaryScorer summaryScorer = new SummaryScorer();
		
		
		try {
			for (int i = 0; i < files.size(); i++) {				
				String file = files.get(i);
				String goldFile = goldFiles.get(i);
				ReaderWriter in = new JsonReaderWriter();
				ReaderWriter inGold = new MmaxReaderWriter();
				Text text = in.read(file);
				Text gold = inGold.read(goldFile);
				text.setPairedText(gold);
				gold.setPairedText(text);
				
				new MentionFinder().findMentions(text);
				new Ruler().resolve(text);
//				text.removeCommonSingletons();
				text.removeCommonUnknownSingletons();
				
				summaryScorer.add(text);
				
				for (Sentence s : text.getSentences()) {
					System.err.println(s.getMentionString());
					System.err.println();
				}
				System.err.println(text);
				for (MentionChain mc : text.getMentionChains()) {
					System.err.println(mc);
				}
				//SwingUtilities.invokeLater(new Viewer(text));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(summaryScorer);
	}
	
	public static void main(String[] args) throws Exception {
		prepare_mkTest_2015Jan();
//		evaluate_mkTest_2015Jan();
	}
}
