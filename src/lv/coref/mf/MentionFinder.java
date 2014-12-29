package lv.coref.mf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.data.Constants.PosTag;
import lv.coref.data.Constants.Type;
import lv.coref.data.Mention;
import lv.coref.data.NamedEntity;
import lv.coref.data.Node;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;

public class MentionFinder {

	private int nextID = 1;
	
	public String getnextID() {
		return Integer.toString(nextID++);
	}
	
	public void findMentions(Text text) {
		for (Paragraph p : text) {
			for (Sentence s : p) {
				findMentionInSentence(s);
			}
		}	
	}
	
	public void findMentionInSentence(Sentence sentence) {
		//addNounMentions(sentence);
		//addNounPhraseMentions(sentence);
		addNamedEntityMentions(sentence);
		addNounPhraseMentions2(sentence);
		
		addCoordinations(sentence);
		
		MentionCleaner.cleanSentenceMentions(sentence);
		
		updateMentionHeads(sentence);
		
	}
	
	private void updateMentionHeads(Sentence sentence) {
		for (Mention m : sentence.getMentions())
			if (m.getHeads().isEmpty())
				m.addHead(m.getLastToken());
	}
	
	private void addNamedEntityMentions(Sentence sent) {
		for (NamedEntity n : sent.getNamedEntities()) {
			List<Token> tokens = n.getTokens();
			List<Token> heads = new ArrayList<>();
			heads.add(tokens.get(tokens.size()-1));
			Mention m = new Mention(tokens, heads, getnextID());
			sent.addMention(m);
			m.setType(Type.NE);
		}
	}
	
	private void addNounMentions(Sentence sent) {
		for (Token t : sent) {
			if (t.getTag().startsWith("n")) {
				Mention m = new Mention(t);
				m.setID(getnextID());
				sent.addMention(m);
			}
		}
	}
	
	private void addCoordinations(Sentence sent) {
		Node n = sent.getRootNode();
		addCoordinations(sent, n);
		
	}
	private void addCoordinations(Sentence sent, Node n) {
		
		for (Node child : n.getChildren()) {
			if (n.getLabel().endsWith("crdParts:crdPart")
					&& n.getHeads().get(0).getPosTag()==PosTag.N) {				
				List<Token> tokens = n.getTokens();
				List<Token> heads = new ArrayList<>();
				for (Token t : tokens) {
					if (t.getDependency().endsWith("crdPart")) {
						heads.add(t);
					}
				}
				if (heads.size() > 1) {
					Mention m = new Mention(tokens, heads, getnextID() );
					sent.addMention(m);
					m.setType(Type.CONJ);
					System.err.println("MENTION COORDINATION: " + m);
				}
			} else {
				addCoordinations(sent, child);
			}
		}
		
	}
	
	private void addNounPhraseMentions(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.N) {
				Mention m = new Mention(n.getTokens(), n.getHeads(), getnextID());
				sent.addMention(m);
			}
		}
	}
	
	private void addNounPhraseMentions2(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.N) {
				List<Token> tokens = sent.subList(n.getStart(), n.getHeads().get(n.getHeads().size()-1).getPosition() + 1);
				
				//simple filter out incorrect borders due conjunctions, punctuation
				int start = 0, end = tokens.size();
				Set<String> fillerLemmas = new HashSet<String>(Arrays.asList("un", ",", "."));
				while(start < tokens.size()) {
					Token t = tokens.get(start);
					if (fillerLemmas.contains(t.getLemma())) {
						start++;
					} else break;
				}
				while(end > 0) {
					Token t = tokens.get(end-1);
					if (fillerLemmas.contains(t.getLemma())) {
						end--;
					} else break;
				}
				tokens = tokens.subList(start, end);	
				
				Mention m = new Mention(tokens,n.getHeads(),getnextID());
				m.setType(Type.NP);
				sent.addMention(m);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text t;
		//t = rw.getText("news_63.conll");
		t = rw.getText("sankcijas.conll");
		
		MentionFinder mf = new MentionFinder();
		mf.findMentions(t);
		
		System.out.println(t);
		
		for (Mention m : t.getMentions()) {
			System.out.println(m);
		}

	}

}
