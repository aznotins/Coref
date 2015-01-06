package lv.coref.score;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;
import lv.coref.util.StringOutputStream;
import lv.coref.util.SystemUtils;

public class ConllEvalScorer {

	public static final String EVAL_SCRIPT = "resource/scorer8/scorer.pl";

	public String resultSummary;
	public Scorer mucScorer = new Scorer();
	public Scorer bCubedScorer = new Scorer();
	public Scorer ceafMScorer = new Scorer();
	public Scorer ceafEScorer = new Scorer();
	public Scorer blancScorer = new Scorer();
	public Scorer averagedScorer = new Scorer();

	public boolean scoreHeads = false;

	public ConllEvalScorer() {
	}

	public ConllEvalScorer(boolean scoreHeads) {
		this.scoreHeads = scoreHeads;
	}

	private Text createHeadMockup(Text t) {
		try {
			String textFile = "tmp/" + UUID.randomUUID() + ".conll";
			new ConllReaderWriter().write(textFile, t);
			String goldTextFile = "tmp/" + UUID.randomUUID() + ".conll";
			new ConllReaderWriter().write(goldTextFile, t.getPairedText());
			Text copyText = new ConllReaderWriter().read(textFile);
			Text copyGoldText = new ConllReaderWriter().read(goldTextFile);
			convertToHeadMentions(copyText);
			convertToHeadMentions(copyGoldText);
			copyText.setPairedText(copyGoldText);
			copyGoldText.setPairedText(copyText);
			// System.out.println(copyText);
			return copyText;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;		
	}

	private void convertToHeadMentions(Text text) {
		for (Mention m : text.getMentions()) {
			for (Token t : m.getTokens()) {
				t.removeMention(m);
			}
			if (m.getHeads().size() > 0) {
				m.setTokens(Arrays.asList(m.getHeads().get(0)));
				m.getHeads().get(0).addMention(m);
			} else {
				System.err.println("No heads for mention : " + m);
			}
		}
		for (Sentence s : text.getSentences()) {
			for (Token t : s) {
				if (t.getMentions().size() > 1) {
					System.err
							.println("convertToHeadMentions multiple mentions on token (keep first) "
									+ t + " : " + t.getMentions());
					// t.getMentions().iterator().next();
				}
			}
		}
	}

	public void add(Text text) {
		add(Arrays.asList(text));
	}

	public void add(List<Text> scoreTexts) {
		try {
			List<Text> texts = new ArrayList<>(scoreTexts.size());
			List<Text> goldTexts = new ArrayList<>(scoreTexts.size());
			for (Text t : scoreTexts) {
				if (scoreHeads)
					t = createHeadMockup(t);				
				texts.add(t);
				if (t.getPairedText() == null) {
					System.err.println("No paired text set for " + t.getId());
					return;
				}
				t.getPairedText().setId(t.getId());
				goldTexts.add(t.getPairedText());				
//				System.err.println(t);
//				System.err.println(t.getPairedText());
			}
			
			String textFile = "tmp/" + scoreTexts.size() + "_" + UUID.randomUUID() + ".conll";
			String goldTextFile = "tmp/" + scoreTexts.size() + "_" + UUID.randomUUID() + ".corefconll";
			ConllReaderWriter rw = new ConllReaderWriter();
			rw.write(textFile, texts);
			rw.write(goldTextFile, goldTexts);
			
			runMetricScorer("muc", goldTextFile, textFile);
			runMetricScorer("bcub", goldTextFile, textFile);
//			runMetricScorer("ceafe", goldTextFile, textFile);
			
			averagedScorer.setPrecision((mucScorer.getPrecision() + bCubedScorer.getPrecision()) / 2.0);
			averagedScorer.setRecall((mucScorer.getRecall()	+ bCubedScorer.getRecall()) / 2.0);
			averagedScorer.setF1((mucScorer.getF1() + bCubedScorer.getF1()) / 2.0);
			
//			averagedScorer.setPrecision((mucScorer.getPrecision() + bCubedScorer.getPrecision() + ceafEScorer.getPrecision()) / 3.0);
//			averagedScorer.setRecall((mucScorer.getRecall()	+ bCubedScorer.getRecall() + ceafEScorer.getRecall()) / 3.0);
//			averagedScorer.setF1((mucScorer.getF1() + bCubedScorer.getF1() + ceafEScorer.getF1()) / 3.0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void runMetricScorer(String metric, String refFile, String hypFile) {
		ProcessBuilder process = new ProcessBuilder("perl", EVAL_SCRIPT,
				metric, refFile, hypFile, "none"); // "none"
		StringOutputStream errSos = new StringOutputStream();
		StringOutputStream outSos = new StringOutputStream();
		PrintWriter out = new PrintWriter(outSos);
		PrintWriter err = new PrintWriter(errSos);
		SystemUtils.run(process, out, err);
		out.close();
		err.close();
		String resultSummary = outSos.toString();
		String errStr = errSos.toString();
		if (errStr.length() > 0) {
			resultSummary += "\nERROR: " + errStr;
		}
		this.resultSummary += resultSummary;
		parseResultSummary(resultSummary, metric);
	}

	public String getSummary() {
		return resultSummary;
	}
	
	public void parseResultSummary(String summary, String metric) {
		Pattern f1 = Pattern
				.compile("(?:Coreference|BLANC):.*Recall: \\((\\d+\\.?\\d*) / (\\d+\\.?\\d*)\\) (.*)%\tPrecision: \\((\\d+\\.?\\d*) / (\\d+\\.?\\d*)\\) (.*)%\tF1: (.*)%");
		Matcher f1Matcher = f1.matcher(summary);
		int count = 5;
		Double[] F1s = new Double[count];
		Double[] Ps = new Double[count];
		Double[] Rs = new Double[count];
		Double[] tp = new Double[count];
		Double[] tpfn = new Double[count];
		Double[] tpfp = new Double[count];
		String[] names = new String[count];
		int i = 0;
		while (f1Matcher.find()) {
			names[i] = f1Matcher.group(1);
			tp[i] = Double.parseDouble(f1Matcher.group(1));
			tpfn[i] = Double.parseDouble(f1Matcher.group(2));
			Rs[i] = Double.parseDouble(f1Matcher.group(3));
			tpfp[i] = Double.parseDouble(f1Matcher.group(5));
			Ps[i] = Double.parseDouble(f1Matcher.group(6));
			F1s[i] = Double.parseDouble(f1Matcher.group(7));
			i++;
		}
		
		if (!metric.equals("all")) {
		
			Scorer s = new Scorer();
			if (metric.equals("muc")) s = mucScorer;
			else if (metric.equals("bcub")) s = bCubedScorer;
			else if (metric.equals("ceafe")) s = ceafEScorer;
			else if (metric.equals("ceafm")) s = ceafMScorer;
			else if (metric.equals("blanc")) s = blancScorer;
			
			s.setPrecision(Ps[0] / 100.0);
			s.setRecall(Rs[0] / 100.0);
			s.setF1(F1s[0] / 100.0);
			s.setTP(tp[0]);
			s.setFP(tpfp[0] - tp[0]);
			s.setFN(tpfn[0] - tp[0]);
		} else {
			parseResultSummary(summary);
		}
	
	}
	

	/** Average F1 of MUC, B^3, CEAF_E */
	public void parseResultSummary(String summary) {
		Pattern f1 = Pattern
				.compile("(?:Coreference|BLANC):.*Recall: \\((\\d+\\.?\\d*) / (\\d+\\.?\\d*)\\) (.*)%\tPrecision: \\((\\d+\\.?\\d*) / (\\d+\\.?\\d*)\\) (.*)%\tF1: (.*)%");
		Matcher f1Matcher = f1.matcher(summary);
		int count = 5;
		Double[] F1s = new Double[count];
		Double[] Ps = new Double[count];
		Double[] Rs = new Double[count];
		Double[] tp = new Double[count];
		Double[] tpfn = new Double[count];
		Double[] tpfp = new Double[count];
		String[] names = new String[count];
		int i = 0;
		while (f1Matcher.find()) {
			names[i] = f1Matcher.group(1);
			tp[i] = Double.parseDouble(f1Matcher.group(1));
			tpfn[i] = Double.parseDouble(f1Matcher.group(2));
			Rs[i] = Double.parseDouble(f1Matcher.group(3));
			tpfp[i] = Double.parseDouble(f1Matcher.group(5));
			Ps[i] = Double.parseDouble(f1Matcher.group(6));
			F1s[i] = Double.parseDouble(f1Matcher.group(7));
			i++;
		}

		Pattern pattern = Pattern.compile("METRIC\\s+(.*):");
		Matcher matcher = pattern.matcher(summary);
		i = 0;
		while (matcher.find()) {
			names[i] = matcher.group(1);
			i++;
		}
		if (!names[0].equals("muc") || !names[1].equals("bcub")
				|| !names[2].equals("ceafm") || !names[3].equals("ceafe")
				|| !names[4].equals("blanc")) {
			System.err.println("Waited for muc bcub ceafm ceafe, received: "
					+ Arrays.asList(names));
			return;
		}

		mucScorer.setPrecision(Ps[0] / 100.0);
		mucScorer.setRecall(Rs[0] / 100.0);
		mucScorer.setF1(F1s[0] / 100.0);
		mucScorer.setTP(tp[0]);
		mucScorer.setFP(tpfp[0] - tp[0]);
		mucScorer.setFN(tpfn[0] - tp[0]);

		bCubedScorer.setPrecision(Ps[1] / 100.0);
		bCubedScorer.setRecall(Rs[1] / 100.0);
		bCubedScorer.setF1(F1s[1] / 100.0);
		bCubedScorer.setTP(tp[1]);
		bCubedScorer.setFP(tpfp[1] - tp[1]);
		bCubedScorer.setFN(tpfn[1] - tp[1]);

		ceafMScorer.setPrecision(Ps[2] / 100.0);
		ceafMScorer.setRecall(Rs[2] / 100.0);
		ceafMScorer.setF1(F1s[2] / 100.0);
		ceafMScorer.setTP(tp[2]);
		ceafMScorer.setFP(tpfp[2] - tp[2]);
		ceafMScorer.setFN(tpfn[2] - tp[2]);

		ceafEScorer.setPrecision(Ps[3] / 100.0);
		ceafEScorer.setRecall(Rs[3] / 100.0);
		ceafEScorer.setF1(F1s[3] / 100.0);
		ceafEScorer.setTP(tp[3]);
		ceafEScorer.setFP(tpfp[3] - tp[3]);
		ceafEScorer.setFN(tpfn[3] - tp[3]);

		blancScorer.setPrecision(Ps[4] / 100.0);
		blancScorer.setRecall(Rs[4] / 100.0);
		blancScorer.setF1(F1s[4] / 100.0);
		blancScorer.setTP(tp[4]);
		blancScorer.setFP(tpfp[4] - tp[4]);
		blancScorer.setFN(tpfn[4] - tp[4]);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("muc: \t").append(mucScorer.toString());
		sb.append("\nbcub: \t").append(bCubedScorer.toString());
//		sb.append("\nceafm: \t").append(ceafMScorer.toString());
//		sb.append("\nceafe: \t").append(ceafEScorer.toString());
//		sb.append("\nblanc: \t").append(blancScorer.toString());
		sb.append("\naveraged: \t").append(averagedScorer.toString());
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {
		Text t = new ConllReaderWriter().read("data/test.corefconll");
		Text gold = new ConllReaderWriter().read("data/test.corefconll");
		t.setPairedText(gold);
		gold.setPairedText(t);

		ConllEvalScorer s = new ConllEvalScorer(true);
		s.add(t);
		System.err.println(s);
		System.err.println(s.getSummary());

		// ProcessBuilder process1 = new ProcessBuilder("resource/test.bat");
		// ProcessBuilder process = new ProcessBuilder("perl",
		// "resource/scorer/scorer.pl", "all",
		// "resource/scorer/test.corefconll",
		// "resource/scorer/test.corefconll");
		// //SystemUtils.run(process);
		// StringOutputStream outSOS = new StringOutputStream();
		// StringOutputStream errSOS = new StringOutputStream();
		// PrintWriter out = new PrintWriter(outSOS);
		// PrintWriter err = new PrintWriter(errSOS);
		// SystemUtils.run(process, out, err);
		// System.out.println(outSOS);
		// System.err.println(errSOS);

	}
}
