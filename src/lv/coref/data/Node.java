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
package lv.coref.data;

import java.util.ArrayList;
import java.util.List;

public class Node {

	private String label;
	private List<Token> heads = new ArrayList<>();
	private Node parent;
	private List<Node> children = new ArrayList<>();

	private int start = -1;
	private int end = -1;
	private boolean root = false;

	public Node() { }

	public Node(String label, List<Token> heads) {
		this.label = label;
		this.heads = heads;
	}

	public Node(String label, int start, int end) {
		this.label = label;
		this.start = start;
		this.end = end;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public Sentence getSentence() {
		if (heads == null || heads.size() == 0)
			return null;
		return heads.get(0).getSentence();
	}

	public List<Token> getTokens() {
		if (getSentence() == null) return heads;
		return getSentence().subList(start, end);
	}

	public void setHeads(List<Token> heads) {
		this.heads = heads;
	}
	
	public void setHead(Token head) {
		this.heads.clear();
		this.heads.add(head);
		head.setNode(this);
	}

	public List<Token> getHeads() {
		return heads;
	}
	
	public Token getHead() {
		if (heads.size() > 0) return heads.get(0);
		else return null;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return parent;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public boolean isRootNode() {
		return root;
	}
	
	public void setRoot(boolean root) {
		this.root = root;
	}
	
	public int getHeight() {
		if (this == null) return -1;
		Node n = this;
		int h = -1;	
		while (n != null) {
			n = n.getParent();
			h++;
		}
		return h;
	}
	
	public void addChild(Node n) {
		children.add(n);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("[");
		s.append(heads);
		for (Token t : getTokens()) {
			s.append(" ").append(t);
		}
		s.append(" |").append(label);
		s.append(String.format(" (%d:%d:%d)", getHeight(), start, end));
		s.append("]");
		return s.toString();
	}
	
	/**
	 * Get all nodes under node n
	 * @param n
	 * @param included
	 * @return
	 */
	public List<Node> getNestedNodes(boolean included) {
		List<Node> r = new ArrayList<>();
		if (this == null) return r;
		if (included) r.add(this);
		for (Node nn : getChildren()) {
			r.add(nn);
			r.addAll(nn.getNestedNodes(false));
		}
		return r;
	}
	
	public String getNestedNodeString(boolean included) {
		return getNestedNodeString(included, 0);
	}
	
	public String getNestedNodeString(boolean included, int lvl) {
		StringBuilder s = new StringBuilder();
		if (this == null) return "null";
		if (included) { s.append(this).append("\n"); lvl++; }
		for (Node nn : getChildren()) {
			for (int i = 0; i < lvl; i++) s.append("  ");
			s.append(nn).append("\n");
			s.append(nn.getNestedNodeString(false, lvl+1));
		}
		return s.toString();
	}
	
	public static void main(String[] args) {

	}

}
