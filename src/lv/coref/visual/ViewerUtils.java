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
