package lv.coref.data;

import java.util.ArrayList;
import java.util.List;

public class Paragraph extends ArrayList<Sentence>{

	private static final long serialVersionUID = -4746893959959169033L;
	
	private Text text;
	private int position;
	
	
	public boolean add(Sentence s) {
		s.setPosition(this.size());
		s.setParagraph(this);
		return super.add(s);
	}
	
	public Text getText() {
		return text;
	}
	
	public void setText(Text text) {
		this.text = text;
	}
	
	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public List<Mention> getMentions() {
		List<Mention> r = new ArrayList<>();
		for (Sentence s : this) {
			r.addAll(s.getMentions());
		}
		return r;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Sentence sentence : this)
			sb.append(sentence.toString()).append("\n");
		return sb.toString();
	}

}
