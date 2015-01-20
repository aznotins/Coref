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
package lv.coref.score;


public class ScorerMUC extends Scorer {

	public ScorerMUC() {
		super();
	}

//	protected void calculateRecall(Text text) {
//		int rDen = 0;
//		int rNum = 0;
//
//		for (MentionChain mc : text.getPairedText().getMentionChain(id)) {
//			if (g.corefMentions.size() == 0) {
//				log.severe("NO MENTIONS for cluster " + g.getClusterID());
//				continue;
//			}
//			rDen += g.corefMentions.size() - 1;
//			rNum += g.corefMentions.size();
//
//			Set<CorefCluster> partitions = new HashSet<CorefCluster>();
//			log.fine("--GoldCluster #" + g.id);
//			for (Mention goldMention : g.corefMentions) {
//				if (goldMention.node.mention == null /*
//													 * !sameMention(doc,
//													 * goldMention.node)
//													 */) { // twinless
//															// goldmention
//					rNum--;
//					log.fine("\t* [" + goldMention.nerString + "]" + "\t"
//							+ goldMention.getContext(text, 3) + "\t@ "
//							+ goldMention.node.id);
//				} else {
//					partitions.add(text.corefClusters
//							.get(goldMention.node.mention.corefClusterID));
//					log.fine("\t" + goldMention.node.mention.corefClusterID
//							+ " [" + goldMention.nerString + "]" + "\t"
//							+ goldMention.getContext(text, 3) + "\t@ "
//							+ goldMention.node.id);
//				}
//			}
//			rNum -= partitions.size();
//		}
//		if (rDen != text.goldMentions.size()
//				- text.goldCorefClusters.values().size()) {
//			log.severe("rDen is " + rDen);
//			log.severe("doc.allGoldMentions.size() is "
//					+ text.goldMentions.size());
//			log.severe("doc.goldCorefClusters.values().size() is "
//					+ text.goldCorefClusters.values().size());
//		}
//		assert (rDen == (text.goldMentions.size() - text.goldCorefClusters
//				.values().size()));
//
//		recallNumSum += rNum;
//		recallDenSum += rDen;
//	}
//
//	@Override
//	protected void calculatePrecision(Document doc) {
//		int pDen = 0;
//		int pNum = 0;
//
//		for (CorefCluster c : doc.corefClusters.values()) {
//			if (c.corefMentions.size() == 0)
//				continue;
//			pDen += c.corefMentions.size() - 1;
//			pNum += c.corefMentions.size();
//			Set<CorefCluster> partitions = new HashSet<CorefCluster>();
//			// LVCoref.logger.fine("--PredictedCluster #" + c.id );
//			for (Mention predictedMention : c.corefMentions) {
//				if (predictedMention.node.goldMention == null /*
//															 * !sameMention(doc,
//															 * predictedMention
//															 * .node)
//															 */) { // twinless
//																	// goldmention
//					pNum--;
//					// LVCoref.logger.fine("\t* ["+predictedMention.nerString+"]"+
//					// "\t"+predictedMention.getContext(doc, 3)+ "\t@ "+
//					// predictedMention.node.id);
//				} else {
//					partitions
//							.add(doc.goldCorefClusters
//									.get(predictedMention.node.goldMention.goldCorefClusterID));
//					// LVCoref.logger.fine("\t"+predictedMention.node.goldMention.goldCorefClusterID+" ["+predictedMention.nerString+"]"
//					// + "\t"+predictedMention.node.mention.getContext(doc, 3)
//					// +"\t@ "+ predictedMention.node.id);
//				}
//			}
//			pNum -= partitions.size();
//		}
//		assert (pDen == (doc.mentions.size() - doc.corefClusters.values()
//				.size()));
//
//		precisionDenSum += pDen;
//		precisionNumSum += pNum;
//	}

}
