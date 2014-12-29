package lv.coref.visual;

import java.util.HashMap;
import java.util.Map;

import lv.coref.data.Mention;
import lv.coref.data.Token;

public class TextMapping {
	Map<Token, Marker> tokenMarkerMap = new HashMap<>();
	Map<Mention, Marker> mentionMarkerMap = new HashMap<>();

	public TextMapping() {

	}

	public Marker getTokenMarker(Token t) {
		return tokenMarkerMap.get(t);
	}

	public void addTokenMarkerPair(Token t, Marker w) {
		tokenMarkerMap.put(t, w);
	}

	public Marker getMentionMarker(Mention m) {
		return mentionMarkerMap.get(m);
	}

	public void addMentionMarkerPair(Mention m, Marker l) {
		mentionMarkerMap.put(m, l);
	}

}
