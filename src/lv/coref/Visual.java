package lv.coref;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import de.ims.icarus.plugins.core.IcarusFrame.PerspectiveContainer;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.coref.CoreferencePerspective;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentView;
import de.ims.icarus.plugins.coref.view.CoreferenceExplorerView;




public class Visual {

	public void test() {
		JTextPane textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();

        Style style = textPane.addStyle("I'm a Style", null);
        StyleConstants.setForeground(style, Color.red);

        try { doc.insertString(doc.getLength(), "BLAH ",style); }
        catch (BadLocationException e){}

        StyleConstants.setForeground(style, Color.blue);

        try { doc.insertString(doc.getLength(), "BLEH",style); }
        catch (BadLocationException e){}

        JFrame frame = new JFrame("Test");
        frame.getContentPane().add(textPane);
        frame.pack();
        frame.setVisible(true);
	}
	
	
	public static void main(String[] args) {
		JComponent container = new JPanel();
		
		JTextPane textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();

        Style style = textPane.addStyle("I'm a Style", null);
        StyleConstants.setForeground(style, Color.red);
        try { doc.insertString(doc.getLength(), "BLAH ",style); }
        catch (BadLocationException e){}

        StyleConstants.setForeground(style, Color.blue);

        try { doc.insertString(doc.getLength(), "BLEH",style); }
        catch (BadLocationException e){}
        
        JFrame frame = new JFrame("Test");
        frame.getContentPane().add(textPane);
        frame.pack();
        frame.setVisible(true);
        //container.add(textPane);
		frame.getContentPane().add(container);
//        CoreferenceDocumentDataPresenter pres=new CoreferenceDocumentDataPresenter();
////        CoreferenceDocumentView vi = new CoreferenceDocumentView();
////        vi.init(container);
//        Perspective pers = new Perspective() {
//			@Override
//			public void init(JComponent container) {
//				this.initView(new CoreferenceExplorerView(), new ViewContainer(getExtension()));
//				CoreferenceExplorerView c = new CoreferenceExplorerView();
//				c.init(container);		
//			}
//		};
//		
//		pers.init(container);
        
        
		CoreferencePerspective p = new CoreferencePerspective();
		p.init(container);
		//c.init(container);

		
		

	}


}
