package lv.pipe;

import java.util.Properties;

import lv.label.Annotation;

public interface PipeTool {
	
	public void init(Properties prop);
	
	public Annotation process(Annotation doc);
	
	public Annotation processParagraph(Annotation paragraph);
	
	public Annotation processSentence(Annotation sentence);
	
}
