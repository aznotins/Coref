/*
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
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter.TYPE;
import lv.coref.lv.Constants;
import lv.coref.util.Pair;
import lv.coref.util.StringUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MmaxReaderWriter extends ReaderWriter {
	
	private List<String> words;

	@Override
	public Text read(BufferedReader in) throws Exception {
		return null;
	}

	@Override
	public void write(PrintStream out, Text t) throws Exception {
	}

	@Override
	protected void initialize(Text t) {
	}

	public void write(String filename, Text t) throws Exception {
		writeProject(filename + ".mmax");
		writeWords(filename + "_words.xml", t);
		writeSentences(filename + "_sent.xml", t);
		writeCoreferences(filename + "_coref.xml", t);
		ConllReaderWriter crw = new ConllReaderWriter(TYPE.CONLL);
		crw.write(filename + ".mmaxconll", t);
	}
	
	public Text read(String filename) throws Exception {
		ConllReaderWriter crw = new ConllReaderWriter(TYPE.CONLL, false);
		Text text = crw.read(filename + ".mmaxconll");
		readWords(text, filename + "_words.xml");
		readCoreferences(text, filename + "_coref.xml");
		return text;
	}

	private void writeProject(String filename) {
		String wordsPath = StringUtils.getBaseName(filename, ".mmax");
		wordsPath += "_words.xml";
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<mmax_project>\n");
		sb.append("<words>").append(wordsPath).append("</words>\n");
		sb.append("<keyactions></keyactions>\n");
		sb.append("<gestures></gestures>\n");
		sb.append("</mmax_project>\n");
		StringUtils.printToFile(filename, sb.toString());
	}

	private void writeWords(String filename, Text text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!DOCTYPE words SYSTEM \"words.dtd\">\n");
		sb.append("<words>");
		int iToken = 1;
		for (Sentence sentence : text.getSentences()) {
			for (Token t : sentence) {
				sb.append("<word id=\"word_").append(iToken++).append("\">");
				sb.append(StringEscapeUtils.escapeXml11(t.getWord()));
				sb.append("</word>\n");
			}
		}
		sb.append("</words>");
		StringUtils.printToFile(filename, sb.toString());
	}

	private static String createSpanString(String s, int from, int to) {
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		sb.append(from);
		if (from != to) {
			sb.append("..");
			sb.append(s);
			sb.append(to);
		}
		return sb.toString();
	}
	
	private static Pair<Integer,Integer> getSpanFromString(String spanString, String label) {
		String[] intervals = spanString.split(",");
		String[] interval = intervals[0].split("\\.\\.");		
		int start = Integer.parseInt(interval[0].substring(label.length()));
		int end = start;
		if (interval.length > 1) {
			end = Integer.parseInt(interval[1].substring(label.length()));
		}
		return new Pair<Integer,Integer>(start, end);
	}
	
	private static List<Token> getHeadsFromString(String headString, List<Token> tokens) {
		List<Token> heads = new ArrayList<>();
		String[] headsArr = headString.split(" ");
		int iHead = 0;
		for (Token t : tokens) {
			if (t.getWord().equalsIgnoreCase(headsArr[iHead])) {
				heads.add(t);
				iHead++;
				if (iHead >= headsArr.length) break;
			}
		}
		if (heads.size() == 0) {
			System.err.println("Didn't found head, use last token as head: headString=\"" + headString +"\" tokens=" + tokens);
			heads.add(tokens.get(tokens.size()-1));
		}
		return heads;
	}

	private void writeSentences(String filename, Text text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!DOCTYPE markables SYSTEM \"markables.dtd\">\n");
		sb.append("<markables xmlns=\"www.eml.org/NameSpaces/sentence\">\n");
		int start = 1;
		int iSent = 1;
		for (Sentence s : text.getSentences()) {
			int end = start + s.size() - 1;
			sb.append("<markable mmax_level=\"sentence\"");
			sb.append(" id=\"markable_").append(iSent++).append("\"");
			sb.append(" span=\"").append(createSpanString("word_", start, end))
					.append("\"");
			sb.append(" />\n");
			start = end + 1;
		}
		sb.append("</markables>");
		StringUtils.printToFile(filename, sb.toString());
	}

	private void writeCoreferences(String filename, Text text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!DOCTYPE markables SYSTEM \"markables.dtd\">\n");
		sb.append("<markables xmlns=\"www.eml.org/NameSpaces/coref\">\n");

		int iMent = 1;
		for (Mention m : text.getMentions()) {
			String corefClass = m.getMentionChain().size() > 1 ? "set_" + m
					.getMentionChain().getID() : "empty";
			String span = createSpanString("word_", m.getFirstToken().getTextPosition(),
					m.getLastToken().getTextPosition());
			String headString = StringUtils.join(m.getHeads(), " ");
			headString = StringEscapeUtils.escapeXml11(headString);
			sb.append("<markable");
			sb.append(" id=\"markable_").append(iMent++).append("\"");
			sb.append(" span=\"").append(span).append("\"");
			sb.append(" coref_class=\"").append(corefClass).append("\"");
			sb.append(" category=\"").append(m.getCategory().name())
					.append("\"");
			sb.append(" mmax_level=\"").append("coref").append("\"");
			sb.append(" rule=\"").append("unknown").append("\"");
			sb.append(" type=\"").append(m.getType().name()).append("\"");
			sb.append(" heads=\"").append(headString).append("\"");
			sb.append(" />\n");
		}
		sb.append("</markables>");
		StringUtils.printToFile(filename, sb.toString());
	}

	public void readWords(Text text, String fileName) {
		try {
			File file = new File(fileName);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();	
			Document doc = dBuilder.parse(file);
			NodeList markables = doc.getElementsByTagName("word");
			words = new ArrayList<String>();
			for (int i = 0; i < markables.getLength(); i++) {
				Node markable = markables.item(i);
				String word = markable.getFirstChild().getNodeValue();
				word = StringEscapeUtils.unescapeXml(word);
				// String idString = markable.getAttributes().getNamedItem("id").getNodeValue();
				words.add(word);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readCoreferences(Text text, String fileName) {
		try {
			File file = new File(fileName);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			NodeList markables = doc.getElementsByTagName("markable");
			int mentionId = 1;
			int twinlessMCId = 10000; // ids for twinless mentions
			for (int i = 0; i < markables.getLength(); i++) {
				Node markable = markables.item(i);
				String spanString = (markable.getAttributes().getNamedItem("span") != null) ?  markable.getAttributes().getNamedItem("span")
						.getNodeValue() : null;
				String category = (markable.getAttributes().getNamedItem("category") != null) ? markable.getAttributes().getNamedItem("category")
						.getNodeValue() : null;
				String type = (markable.getAttributes().getNamedItem("type") != null) ? markable.getAttributes().getNamedItem("type").getNodeValue() : null;
				String rule = (markable.getAttributes().getNamedItem("rule") != null) ? markable.getAttributes().getNamedItem("rule")
						.getNodeValue() : null;
				String headString = (markable.getAttributes().getNamedItem("heads") != null) ? markable.getAttributes().getNamedItem("heads")
						.getNodeValue() : "";
				headString = StringEscapeUtils.unescapeXml(headString);
				String corefString = markable.getAttributes()
						.getNamedItem("coref_class").getNodeValue();
				
				
				String id = null;
				if (corefString.startsWith("set_"))
					id = corefString.substring(4);
				else 
					id = Integer.toString(twinlessMCId++);
				Pair<Integer,Integer> span = getSpanFromString(spanString, "word_");
				int start = span.first - 1;
				int end = span.second - 1;
				Token startToken = text.getToken(start);
				Token endToken = text.getToken(end);
				List<Token> tokens = startToken.getSentence().subList(startToken.getPosition(), endToken.getPosition() + 1);
				List<Token> heads = getHeadsFromString(headString, tokens);
				Mention m = new Mention(Integer.toString(mentionId++), tokens, heads);
				m.setCategory(category);
				m.setType(Constants.Type.valueOf(type.toUpperCase()));
				
				
				startToken.getSentence().addMention(m);				
				MentionChain mc = text.getMentionChain(id);
				if (mc == null) {
					mc = new MentionChain(id, m);
					startToken.getSentence().getText().addMentionChain(mc);
				} else {
					mc.add(m);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		Text text;
		
		//text = new ConllReaderWriter(null, false).read("data/mmax/test.corefconll");		
		//text = new MmaxReaderWriter().read("data/mmax/test");
		text = new MmaxReaderWriter().read("data/mktest_2015-jan/test_taube");
		System.err.println(text);
		//new MmaxReaderWriter().write("data/mmax/test1", text);
	}

}
