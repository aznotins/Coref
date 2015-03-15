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
