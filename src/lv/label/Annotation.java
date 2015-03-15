package lv.label;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lv.label.Labels.LabelDependency;
import lv.label.Labels.LabelIndex;
import lv.label.Labels.LabelLemma;
import lv.label.Labels.LabelList;
import lv.label.Labels.LabelMorphoFeatures;
import lv.label.Labels.LabelNer;
import lv.label.Labels.LabelParagraphs;
import lv.label.Labels.LabelParent;
import lv.label.Labels.LabelPosTag;
import lv.label.Labels.LabelPosTagSimple;
import lv.label.Labels.LabelSentences;
import lv.label.Labels.LabelText;
import lv.label.Labels.LabelTokens;
import lv.util.SimpleTypeSafeMap;

public class Annotation extends SimpleTypeSafeMap {

	private static final long serialVersionUID = 1L;

	public Annotation() {

	}

	public Annotation(String text) {
		set(LabelText.class, text);
	}

	public void setText(String text) {
		set(LabelText.class, text);
	}

	public String getText() {
		return get(LabelText.class);
	}

	public void setLemma(String text) {
		set(LabelLemma.class, text);
	}

	public String getLemma() {
		return get(LabelLemma.class);
	}

	public void setNer(String text) {
		set(LabelNer.class, text);
	}

	public String getNer() {
		return get(LabelNer.class);
	}

	public static List<Annotation> flatten(Annotation a, String BOUNDARY) {
		List<Annotation> r = new ArrayList<>();
		main: if (a.has(LabelParagraphs.class)) {
			for (Annotation p : a.get(LabelParagraphs.class)) {
				if (!p.has(LabelSentences.class))
					break main;
				for (Annotation s : p.get(LabelSentences.class)) {
					if (!s.has(LabelTokens.class))
						break main;
					for (Annotation t : s.get(LabelTokens.class)) {
						r.add(t);
					}
					r.add(new Annotation(BOUNDARY));
				}
				r.add(new Annotation(BOUNDARY));
			}
		} else if (a.has(LabelSentences.class)) {
			for (Annotation s : a.get(LabelSentences.class)) {
				if (!s.has(LabelTokens.class))
					break main;
				for (Annotation t : s.get(LabelTokens.class)) {
					r.add(t);
				}
				r.add(new Annotation(BOUNDARY));
			}
		} else {
			if (!a.has(LabelTokens.class))
				break main;
			for (Annotation t : a.get(LabelTokens.class)) {
				r.add(t);
			}
		}
		return r;
	}

	public static String getConllString(Annotation a) {
		StringBuilder sb = new StringBuilder();
		if (a.has(LabelParagraphs.class)) {
			for (Annotation p : a.get(LabelParagraphs.class)) {
				if (!p.has(LabelSentences.class))
					continue;
				for (Annotation s : p.get(LabelSentences.class)) {
					if (!s.has(LabelTokens.class))
						continue;
					for (Annotation t : s.get(LabelTokens.class)) {
						sb.append(t.get(LabelIndex.class));
						sb.append("\t").append(t.getText());
						sb.append("\t").append(t.getLemma());
						sb.append("\t").append(t.get(LabelPosTagSimple.class));
						sb.append("\t").append(t.get(LabelPosTag.class));
						sb.append("\t").append(t.get(LabelMorphoFeatures.class));
						sb.append("\t").append(t.get(LabelParent.class));
						sb.append("\t").append(t.get(LabelDependency.class));
						sb.append("\t").append(t.get(LabelNer.class));
						sb.append("\n");
					}
					sb.append("\n");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public String toStringPretty() {
		StringBuilder s = new StringBuilder();
		s.append("Annotation [\n");
		s.append(toStringPretty(1));
		s.append("]");
		return s.toString();
	}

	private static final String PRETTY_SEPERATOR = "  ";

	private String toStringPretty(int level) {
		StringBuilder s = new StringBuilder();
		for (Map.Entry<Class<?>, Object> x : entrySet()) {
			String key = x.getKey().getSimpleName();
			String type = x.getValue().getClass().getSimpleName();
			Object oVal = x.getValue();
			String value = x.getValue().toString();

			for (int i = 0; i < level; i++)
				s.append(PRETTY_SEPERATOR);
			s.append(key).append(": ");

			if ((Collection.class).isAssignableFrom(x.getValue().getClass())) {
				try {
					@SuppressWarnings("unchecked")
					Collection<Annotation> ca = (Collection<Annotation>) oVal;
					boolean first = true;
					for (Annotation a : ca) {
						if (first) {
							s.append("<").append(type).append(">");
							s.append("\n");
							first = false;
						} else {
							for (int i = 0; i <= level; i++)
								s.append(PRETTY_SEPERATOR);
							s.append("-");
							s.append("\n");
						}
						s.append(a.toStringPretty(level + 1));
					}
				} catch (ClassCastException e) {
					value = value.replaceAll("\n", "_NEWLINE_");
					s.append(value).append("\n");
				}
			} else {
				value = value.replaceAll("\n", "_NEWLINE_");
				s.append(value).append("\n");
			}
		}
		return s.toString();
	}

	public static void main(String[] args) {
		Annotation d = new Annotation();

		d.set(LabelText.class, "dokumenta teksts");

		d.set(LabelList.class, Arrays.asList("1", "2"));

		Annotation t1_1 = new Annotation();
		t1_1.set(LabelText.class, "jauns");
		Annotation t1_2 = new Annotation();
		t1_2.set(LabelText.class, "auto");
		d.set(LabelSentences.class, Arrays.asList(t1_1, t1_2));

		System.err.println(d);
		System.err.println(d.toStringPretty());

		try {
			FileOutputStream fileOut = new FileOutputStream("tmp.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(d);
			out.close();
			fileOut.close();
			System.err.println("Serialized data is saved");
		} catch (IOException i) {
			i.printStackTrace();
		}

		Annotation aa;
		try {
			FileInputStream fileIn = new FileInputStream("tmp.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			aa = (Annotation) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return;
		}

		System.err.println(aa.toStringPretty());
	}

}
