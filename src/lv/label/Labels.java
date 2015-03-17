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
package lv.label;

import java.util.List;

public class Labels {

	public static class LabelText implements Label<String> {
	}

	public static class LabelTokens implements Label<List<Annotation>> {
	}

	public static class LabelSentences implements Label<List<Annotation>> {
	}

	public static class LabelParagraphs implements Label<List<Annotation>> {
	}

	public static class LabelIndex implements Label<Integer> {
	}

	public static class LabelLemma implements Label<String> {
	}

	public static class LabelPosTag implements Label<String> {
	}

	public static class LabelPosTagSimple implements Label<String> {
	}

	public static class LabelMorphoFeatures implements Label<String> {
	}

	public static class LabelParent implements Label<Integer> {
	}

	public static class LabelDependency implements Label<String> {
	}

	public static class LabelNer implements Label<String> {
	}
	
	public static class LabelDocumentId implements Label<String> {
	}
	
	public static class LabelDocumentDate implements Label<String> {
	}
	
	

	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object o) {
		return (T) o;
	}

	public static class LabelList implements Label<List<String>> {
		public Class<List<String>> getType() {
			return Labels.<Class<List<String>>> uncheckedCast(List.class);
		}
	}

}
