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

import java.awt.Color;

import lv.coref.data.Mention;

public class ViewerUtils {
	static final int MAX_COLORS = 40;
	
	static public Color getMentionClusterColor(Mention mention) {
		int mcid = 1;
		try {
			mcid = Integer.parseInt(mention.getMentionChain().getID());
		} catch (Exception e) {
			e.printStackTrace();
		}
		mcid = mcid % MAX_COLORS;
		float step = ((float) 1.0) / MAX_COLORS;
		Color color = new Color(Color.HSBtoRGB(1f / MAX_COLORS * mcid, 1f, 0.5f ));
		return color;
	}
}
