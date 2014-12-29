package lv.coref.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lv.coref.mf.MentionFinder;
import lv.coref.util.Triple;

public class Sentence extends ArrayList<Token> {

	private static final long serialVersionUID = 6452651828665864779L;

	private Paragraph paragraph;
	private int position;

	// private Set<Mention> mentions = new TreeSet<>(); // TODO nodzēš nepareizi
	// pievienojot jaunus m
	private Set<Mention> mentions = new HashSet<>();
	private Node root;
	private Set<NamedEntity> namedEntities = new TreeSet<>();

	public Sentence() {
	}

	public boolean add(Token token) {
		token.setPosition(this.size());
		token.setSentence(this);
		return super.add(token);
	}

	public Paragraph getParagraph() {
		return paragraph;
	}

	public void setParagraph(Paragraph paragraph) {
		this.paragraph = paragraph;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<Mention> getMentions() {
		return new ArrayList<>(mentions);
	}

	public void addMention(Mention mention) {
		if (!this.mentions.add(mention))
			this.removeMention(mention);
	}

	public void removeMention(Mention mention) {
		mentions.remove(mention);
		// System.err.println("SENTECE remove mention " + mention + " "
		// + mentions.contains(mention));
		for (Token t : mention.getTokens()) {
			t.removeMention(mention);
			// for (Mention m : t.getMentions()) {
			// System.err.println(mention + " " + m + " " + mention.equals(m)
			// + mention.compareTo(m) + " " + mention.hashCode() + ":"
			// + m.hashCode());
			// }
			// System.err.println("TOKEN remove mention "
			// + t.getMentions().contains(mention) + t.getMentions());
		}
	}

	public List<NamedEntity> getNamedEntities() {
		return new ArrayList<>(namedEntities);
	}

	public void addNamedEntity(NamedEntity namedEntity) {
		namedEntities.add(namedEntity);
	}

	public Node getRootNode() {
		return root;
	}

	public void setRootNode(Node root) {
		this.root = root;
	}

	/**
	 * Initialize syntactic node tree using CONLL parent field
	 * 
	 * @return Sentence root node
	 */
	public Node initializeNodeTree() {
		Node root = new Node("_ROOT_", 0, size());
		root.setHead(new Token("_sentroot_", "_", "_"));
		Map<Token, Node> nodes = new HashMap<>();
		for (Token t : this) {
			Node n;
			if (nodes.containsKey(t))
				n = nodes.get(t);
			else {
				n = new Node(t.getDependency(), t.getPosition(),
						t.getPosition() + 1);
				n.setHead(t);
				nodes.put(t, n);
			}
			Token p = t.getParentToken();
			if (p == null) {
				n.setParent(root);
				root.addChild(n);
			} else {
				Node pn;
				if (nodes.containsKey(p))
					pn = nodes.get(p);
				else {
					pn = new Node(p.getDependency(), p.getPosition(),
							p.getPosition() + 1);
					pn.setHead(p);
					nodes.put(p, pn);
				}
				n.setParent(pn);
				pn.addChild(n);
			}
		}
		// get correct span borders for nodes
		initializeNodeBorders(root);
		this.root = root;
		return root;
	}

	/**
	 * Initialize node span borders, assuming correctly set up tree (all
	 * children and parents) TODO optimize - for borders use ordered children
	 * list last and first element
	 */
	public int[] initializeNodeBorders(Node n) {
		for (Node nn : n.getChildren()) {
			int[] arr = initializeNodeBorders(nn);
			n.setStart(Math.min(n.getStart(), arr[0]));
			n.setEnd(Math.max(n.getEnd(), arr[1]));
		}
		return new int[] { n.getStart(), n.getEnd() };
	}

	/**
	 * Get all ordered nodes
	 * 
	 * @return
	 */
	public List<Node> getNodes(boolean rootIncluded) {
		return root.getNestedNodes(rootIncluded);
	}

	/**
	 * Initialize Named Entities from spans (start, end, label)
	 * 
	 * @param spans
	 */
	public void initializeNamedEntities(
			List<Triple<Integer, Integer, String>> spans) {
		for (Triple<Integer, Integer, String> span : spans) {
			NamedEntity n = new NamedEntity(span.third, this.subList(
					span.first, span.second + 1));
			namedEntities.add(n);
		}
	}

	/**
	 * Initialize coreferences from spans (start, end, id)
	 * 
	 * @param spans
	 * @param mf
	 */
	public void initializeCoreferences(
			List<Triple<Integer, Integer, String>> spans, MentionFinder mf) {
		Text text = getText();
		for (Triple<Integer, Integer, String> span : spans) {
			String id = span.third;
			List<Token> tokens = this.subList(span.first, span.second + 1);
			List<Token> heads = this.subList(span.second, span.second + 1);
			Mention m = new Mention(tokens, heads, mf.getnextID());
			if (text.getMentionChain(id) == null) {
				text.addMentionChain(new MentionChain(id));
			}
			MentionChain mc = text.getMentionChain(id);
			mc.add(m);
			addMention(m);
		}
	}

	public Text getText() {
		return getParagraph().getText();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Token t : this) {
			for (@SuppressWarnings("unused")
			Mention m : t.getStartMentions()) {
				sb.append("[");
			}
			sb.append(t.toString());
			for (Mention m : t.getEndMentions()) {
				sb.append(" |").append(m.getType());
				if (m.getMentionChain() != null)
					sb.append("|").append(m.getMentionChain().getID());
				sb.append("|").append(m.getID());
				// sb.append(" ").append(m.toParamString());
				sb.append("]");
			}
			sb.append(" ");
		}
		for (Mention m : getMentions()) {
			sb.append("\n\t");
			if (m.getMention(false) != null)
				sb.append("+");
			else
				sb.append("-");
			if (m.getMention(true) != null)
				sb.append("+");
			else
				sb.append("-");
			sb.append(m);
		}
		return sb.toString();
	}

}
