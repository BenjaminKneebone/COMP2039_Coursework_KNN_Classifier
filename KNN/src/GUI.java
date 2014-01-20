import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

public class GUI extends JFrame {

	private TunaKNNClassifier knn = new TunaKNNClassifier("data.txt");
	private GraphCreator gc = new GraphCreator();
	
	private JComboBox<String> KNNSelection;
	private JComboBox<String> KNNMethod;
		
	//Used to label check boxes and text fields
	private String[] field = {"Mean Depth", "Median Depth", "SD Depth", "IQR Depth", "Mean Temp", "Median Temp", "SD Temp", "IQR Temp"};
	
	private EastPanel eastPanel;
	private ClassificationPanel classPanel;
	
	//Panel containing everything within the Frame
	private JPanel contentPanel;
	
	//Holds the chart
	private JPanel chartPanel = new JPanel();
	
	//User can choose which dimensions to compare to
	private JCheckBox[] checkBox = new JCheckBox[8];
	
	//Menu item giving the user a choice whether classification auto-updates when a check box is changed
	private JCheckBoxMenuItem menuItemClassificationUpdate = new JCheckBoxMenuItem("Auto-Update Classification", true);
	
	//Menu item giving the user a choice whether to show the sample data on the graph
	private JCheckBoxMenuItem menuItemShowSample = new JCheckBoxMenuItem("Show Sample", true);
	
	//Indicates which dimensions are currently selected
	private boolean dimensions[] = new boolean[8];
	
	public GUI(){
		initialize();
	}
	
	private void initialize(){
		/*------- Initialise Panels -------*/
		
		eastPanel = new EastPanel();
		chartPanel = gc.lineChart();
		classPanel = new ClassificationPanel();
		
	    /*------- Set up Frame ------*/ 
		
		contentPanel = (JPanel) this.getContentPane();
		contentPanel.setLayout(new BorderLayout());
		
		contentPanel.add(eastPanel, BorderLayout.EAST);
		contentPanel.add(chartPanel, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(classPanel);
		southPanel.add(new JLabel("Go to File to turn off the Automatic Classification update/Show Sample on the graph." +
								  "This will stop the sanitation of inputs until you click classify"), BorderLayout.SOUTH);
		contentPanel.add(southPanel, BorderLayout.SOUTH);
		
		/*------- SET UP THE MENU BAR -------*/
		
		//Item sets whether classification should update when a check box is clicked
		menuItemClassificationUpdate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Update the classification if we have turned auto-update on 
				if(menuItemClassificationUpdate.isSelected()){
					if(classPanel.setGCSampleData())
						classPanel.updateClassification();
				}else
					menuItemClassificationUpdate.setSelected(false);
			}
		});
		
		//Item sets whether classification should update when a check box is clicked
		menuItemShowSample.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				//If the user wants the sample shown
				if(menuItemShowSample.isSelected())
					//Check we can extract the data we need
					if(classPanel.setGCSampleData()){
						//Tell the graph creator we want the sample on the next graph
						gc.showSample(true);
					
						//Update the graph if we can to show sample
						updateGraph();
						
						GUI.this.validate();
					}else{
						//Could not extract data, don't show sample
						menuItemShowSample.setSelected(false);
					}
				else{
					//Tell the graph creator not to show the sample
					gc.showSample(false);
					//Update the graph with sample removed
					updateGraph();
					
					GUI.this.validate();
				}
			}
		});
		
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		JMenuItem menuItemHelp = new JMenuItem("Show Help");
		menuItemHelp.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new HelpGUI();
			}
		});
		
		JMenu menuFile = new JMenu("File");
		menuFile.add(menuItemClassificationUpdate);
		menuFile.add(menuItemShowSample);
		menuFile.add(menuItemExit);
		
		JMenu menuHelp = new JMenu("Help");
		menuHelp.add(menuItemHelp);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);
		
		
		/*------- Frame initialisation -------*/
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Fine Tuna");
		pack();
		setVisible(true);
	}
	
	
	private void updateDimensions(){
		//Tell the classifier which dimensions are to be checked
		for(int x = 0; x < 8; x++)
		dimensions[x] = checkBox[x].isSelected();
	}
		
	private void setGCDimensions(){
		//Tell the graph creator which dimensions are to be shown
		gc.setDimensionsToShow(checkBox[0].isSelected(), checkBox[1].isSelected(), 
							   checkBox[2].isSelected(), checkBox[3].isSelected(), 
				               checkBox[4].isSelected(), checkBox[5].isSelected(), 
				               checkBox[6].isSelected(), checkBox[7].isSelected());
	}
	
	public void updateAccuracy(){
		/*Tell the Classifier which dimensions are to be checked when running
		 * the leave-one-out test, then run the test and display the accuracy
		 */
		if(KNNSelection.getSelectedIndex() != 0 && allCheckBoxesUnselected()){
			JOptionPane.showMessageDialog(contentPanel, "Classification does not work with i > 1 and no selected dimensions");
			KNNSelection.setSelectedIndex(0);
		}
			
		eastPanel.setAccuracyLabel(String.format("%.2f%%", knn.oneLeftOutTest(dimensions,KNNSelection.getSelectedIndex() + 1,KNNMethod.getSelectedIndex() + 1)));
	}
	
	public void updateGraph(){
		/*Tell the Graph which dimensions are to be shown on the graph, then
		 * draw the graph and put it on the main panel
		 */
		setGCDimensions();
		contentPanel.remove(chartPanel);
		chartPanel = gc.lineChart();
		contentPanel.add(chartPanel);
	}
	
	private boolean allCheckBoxesUnselected(){
		for(int x = 0; x < 8; x++)
			if(checkBox[x].isSelected())
				return false;
			
		return true;	
	}
	
	private class EastPanel extends JPanel{
				
		private JLabel measurementTitle = new JLabel("Measurements:", JLabel.CENTER);
		private Font measurementTitleFont = new Font("SansSerif", Font.BOLD, 22);
		
		//Tells the user how accurate the current dimension selection is
		private JLabel accuracy;
		private Font accuracyFont = new Font("SansSerif", Font.BOLD, 30);
		private JLabel accuracyTitle = new JLabel("Accuracy:", JLabel.CENTER);
		private Font accuracyTitleFont = new Font("SansSerif", Font.BOLD, 24);
		
		public EastPanel(){
		
			setLayout(new BorderLayout());
			setBorder(new MatteBorder(0,5,0,0, Color.BLACK));
			
			measurementTitle.setFont(measurementTitleFont);
			add(measurementTitle, BorderLayout.NORTH);
			
			//Add check boxes and buttons
			add(new SettingsPanel());
			
			accuracy = new JLabel("",JLabel.CENTER);
			accuracy.setFont(accuracyFont);
			accuracyTitle.setFont(accuracyTitleFont);
			
			//Panel that displays accuracy
			JPanel accuracyPanel = new JPanel(new GridLayout(2,1));
			accuracyPanel.setBorder(new MatteBorder(5,5,5,5, Color.RED));
			
			accuracyPanel.add(accuracyTitle);
			accuracyPanel.add(accuracy);
			
			add(accuracyPanel, BorderLayout.SOUTH);
		}
		
		public void setAccuracyLabel(String text){
			accuracy.setText(text);
		}
	}
	
	private class SettingsPanel extends JPanel implements ItemListener{
		
		/*Indicates whether a button has been pressed, as setting all the check boxes to selected triggers
		  itemStateChanged 8 times, which need to be ignored if as a result of clicking a button*/
		private boolean buttonPress = false;
		
		public SettingsPanel(){
			
			setLayout(new GridLayout(12,1));
			//Add check boxes with appropriate names, set the panel as the listener
			for(int x = 0; x < field.length; x++){
				checkBox[x] = new JCheckBox(field[x]);
				checkBox[x].addItemListener(this);
				add(checkBox[x]);
			}
						
			JPanel buttonPanel1 = new JPanel();
			JButton selectAll = new JButton("Select All");
			selectAll.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//Button has been pressed
					buttonPress = true;
					setAllCheckBoxes(true);
					//Check boxes finished updating
					buttonPress = false;
					
					//If either the sample is drawn on the graph, or the classification auto-updates
					if(menuItemClassificationUpdate.isSelected() || menuItemShowSample.isSelected()){
						//Check we can extract the required data from the text boxes
						if(classPanel.setGCSampleData()){
							
							//Show the accuracy of the new selection
							updateAccuracy();
					
							//Update graph and classification if on auto-update
							updateGraph();	
	
							classPanel.updateClassification();
						}else{
							//Could not get the data, do not update graph
							
							//Button has been pressed
							buttonPress = true;
							setAllCheckBoxes(false);
							//Check boxes finished updating
							buttonPress = false;
						}
					}else{
						//Don't need to extract any information, show the graph
						updateAccuracy();
						updateGraph();
						GUI.this.validate();
					}
				}
			});
			buttonPanel1.add(selectAll);
			add(buttonPanel1);
			
			JPanel buttonPanel2 = new JPanel();
			JButton deselectAll = new JButton("De-Select All");
			deselectAll.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//Button has been pressed
					buttonPress = true;
					setAllCheckBoxes(false);
					//Check boxes finished updating
					buttonPress = false;

					//Don't need to extract/verify any information, show the graph
					updateAccuracy();
					updateGraph();
					GUI.this.validate();
				}
			});
			buttonPanel2.add(deselectAll);
			add(buttonPanel2);
			
			String[] knnI = {"KNN-1","KNN-2","KNN-3","KNN-4","KNN-5","KNN-6","KNN-7","KNN-8","KNN-9","KNN-10"};
			KNNSelection = new JComboBox<String>(knnI);
			KNNSelection.addActionListener(new ActionListener(){	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateAccuracy();
					if(menuItemClassificationUpdate.isSelected())
						classPanel.updateClassification();
				}
			});
			add(KNNSelection);
			
			String[] knnOptions = {"KNN Normal", "KNN Weighted", "KNN Weighted-Squared", "Bayes Naive Classifier"};
			KNNMethod = new JComboBox<String>(knnOptions);
			KNNMethod.addActionListener(new ActionListener(){	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateAccuracy();
					if(menuItemClassificationUpdate.isSelected())
						classPanel.updateClassification();
				}
			});
			add(KNNMethod);
			
		}

		@Override
		//Update the graph is a check box has been changed
		public void itemStateChanged(ItemEvent arg0) {			
			if(!buttonPress){
				updateDimensions();
				
				//If either the sample is drawn on the graph, or the classification auto-updates
				if(menuItemClassificationUpdate.isSelected() || menuItemShowSample.isSelected()){
					//Check we can extract the required data from the text boxes
					if(classPanel.setGCSampleData()){
						//Show the accuracy of the new selection
						updateAccuracy();
				
						//Update graph and classification if on auto-update
						updateGraph();	

						classPanel.updateClassification();
						GUI.this.validate();
					}else{
						//Could not update, set the triggered box to unselected
						buttonPress = true;
						((JCheckBox) arg0.getItem()).setSelected(false);
						buttonPress = false;
					}
				}else{
					//Don't need to extract any information, show the graph
					updateAccuracy();
					updateGraph();
					GUI.this.validate();
				}
				
				//Set labels of fields required to red
				for(int x = 0; x < 8; x++)
					if(arg0.getSource() == checkBox[x])
						classPanel.changeLabelColour(x, checkBox[x].isSelected());
			}
		}
		
		public void setAllCheckBoxes(boolean selection){
			//Set all the check boxes to the boolean parameter
			for(int x = 0; x < field.length; x++){
				checkBox[x].setSelected(selection);	
				classPanel.changeLabelColour(x, selection);
				updateDimensions();
			}
		}
	}
	
	private class ClassificationPanel extends JPanel{
	
		//Field Labels
		private JLabel[] labels = new JLabel[8];
		
		//Fields to input 12 values
		private JTextField[] testDataInput = new JTextField[8]; 

		private JButton classify = new JButton("Classify!");
		private JLabel result = new JLabel("----", JLabel.CENTER);

		//Holds the values to be passed into the classifier
		private double[] testData = new double[8];
		
		public ClassificationPanel(){
			
			setLayout(new GridLayout(2,9));
			setBorder(new MatteBorder(5,0,0,0, Color.BLACK));			
			
			//Give the JLabels their name as appropriate
			for(int x = 0; x < 8; x++){
				labels[x] = new JLabel(field[x]);
				add(labels[x]);
			}
			
			classify.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					//If we extracted all the data we need
					if(setGCSampleData()){
						//Update the classification
						updateClassification();
					}
					
					if(menuItemShowSample.isSelected()){
						updateGraph();
						
						GUI.this.validate();
					}
				}
			});
			
			//Add button that triggers classification
			add(classify);
			
			//Create Text boxes to input data. 
			for(int x = 0; x < 8; x++){
				testDataInput[x] = new JTextField("0", 5);
				add(testDataInput[x]);
			}
			
			//Add label that shows classification result
			add(result);	
		}
		
		public void changeLabelColour(int x, boolean red){
			if(red)
				labels[x].setForeground(Color.RED);
			else
				labels[x].setForeground(Color.BLACK);
		}
		
		public boolean setGCSampleData(){
			//For all 8 boxes
			for(int x = 0; x < 8; x++){
				//If this field is required
				if(checkBox[x].isSelected())
					//Try to retrieve the data from that box
					try{
						testData[x] = (Double.valueOf(testDataInput[x].getText()));
					}catch(NumberFormatException nf){
						//Alert the user that a double could not be retrieved
						JOptionPane.showMessageDialog(contentPanel, "Please enter a valid number in all required fields");
						//We will not perform the classification
						result.setText("----");
						return false;
					}
				
			}
			gc.setSampleData(testData);
			return true;
		}
		
		public void updateClassification(){	
				contentPanel.remove(chartPanel);
			    gc.setSampleData(testData);
			    chartPanel = gc.lineChart();
			    contentPanel.add(chartPanel);
				
				//Show the classification of the data.
				result.setText(knn.classify(dimensions, testData, KNNSelection.getSelectedIndex() + 1, KNNMethod.getSelectedIndex()+1));
		}
	}
}
