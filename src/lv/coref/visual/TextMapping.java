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
