package lv.coref.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NamedEntity implements Comparable<NamedEntity> {

	private String label;
	private List<Token> tokens = new ArrayList<>();
	
	public NamedEntity(String label, List<Token> tokens) {
		this.label = label;
		this.tokens = tokens;
		for (Token t : tokens) t.setNamedEntity(this);
	}
	
	public void setLabel(String label) { this.label = label; }
	
	public String getLabel() { return label; }
	
	public void setTokens(List<Token> tokens) { this.tokens = tokens; }
	
	public List<Token> getTokens() { return tokens; }
	
	@Override
	public int compareTo(NamedEntity o) {
		Iterator<Token> it1 = getTokens().iterator();
		Iterator<Token> it2 = o.getTokens().iterator();
		while (it1.hasNext() && it2.hasNext()) {
			Token t1 = it1.next();
			Token t2 = it2.next();
			if (t1.compareTo(t2) != 0)
				return t1.compareTo(t2);
		}
		if (it1.hasNext())
			return 1;
		if (it2.hasNext())
			return -1;

		return 0;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("{");
		s.append(getLabel());
		s.append(" ").append(getTokens());
		s.append("}");
		return s.toString();
	}

}
