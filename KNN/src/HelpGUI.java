import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;



public class HelpGUI extends JFrame{

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	public HelpGUI(){
		
		JPanel panel = (JPanel) this.getContentPane();
		panel.setLayout(new GridLayout(9,1));
		
		panel.add(new JLabel("<html><u>Show Sample</u></html>", JLabel.CENTER));
		panel.add(new JLabel("ON: Every time the dimensions shown are changed, the inputs are all " +
				"checked (That they are numbers), and the values displayed on the graph, coloured black" , JLabel.CENTER));
		panel.add(new JLabel("OFF: The sample is not shown. Inputs are not checked", JLabel.CENTER));
		panel.add(new JLabel(""));
		panel.add(new JLabel("<html><u>Auto-Update Classification</u></html>", JLabel.CENTER));
		panel.add(new JLabel("ON: Every time the dimensions shown are changed, the inputs are all " +
				"checked (That they are numbers), and the classification is updated", JLabel.CENTER));
		panel.add(new JLabel("OFF: Classification not updated, inputs not checked", JLabel.CENTER));
		panel.add(new JLabel(""));
		panel.add(new JLabel("Turn both off these off if you just want to see lots of graphs without any hassle!", JLabel.CENTER));
		
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocation(screenSize.width/2 - (this.getWidth()/2), screenSize.height/2 - (this.getHeight()/2));
		setVisible(true);
	}	
}
