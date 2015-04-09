package integration;

import java.util.List;

import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.coref.semantic.KNB;
import lv.coref.semantic.NEL;
import lv.label.Annotation;
import lv.pipe.Pipe;
import lv.util.FileUtils;

public class NELTest {
	
	public static boolean USE_PIPE_CLIENT = true;
	public static boolean REAL_UPLOAD = false;
	
	public static void filesTest(List<String> files, int dataSet) {
		NEL.getInstance().setRealUpload(REAL_UPLOAD);
		//NEL.getInstance().setShowDisambiguation(true);
		KNB.getInstance().setDataSet(dataSet);
		for (String filename : files) {
			System.err.printf("\n=======\nSOLVE: %s\n", filename);
			Annotation doc = CorefPipe.solveFile(filename, USE_PIPE_CLIENT, "_TEST_1_" + filename);
			
			Text text = Annotation.makeText(doc);
			text.setId("_TEST_1_" + filename);
			CorefPipe.getInstance().process(text, false);			
			System.err.printf("\n\n=======\n=======\n%s=======\n=======\n",text);	
			
			if (doc == null) {
				System.err.printf("ERROR: unable to process file (returned null Annotation) %s\n", filename);
				break;
			}
			System.err.println();
		}
	}

	public static void main(String[] args) {
		Config.logInit();
		//KNB.getInstance().clearDataSet(-998);
		filesTest(FileUtils.getFiles("resource/testdata/mk_test", -1, -1, ".txt"), -999);
		
		Pipe.close();
	}
}
