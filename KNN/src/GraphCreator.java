import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class GraphCreator {

	//Holds the data read in from the file
	private ArrayList<String> classification;
	private ArrayList<ArrayList<Double>> unstandardisedValues = new ArrayList<ArrayList<Double>>();
	
	private boolean[] dimensionsToShow = new boolean[8];
	private final String[] dimensionNames = {"Mean Depth", "Median Depth", "SD Depth", "IQR Depth", 
									   "Mean Temp", "Median Temp", "SD Temp", "IQR Temp"};
	
	private boolean showSample = true;
	
	private double[] sampleData = {0,0,0,0,0,0,0,0};
	
	public GraphCreator(){
		reloadData("data.txt");
	}
	
	public void reloadData(String filename){
		unstandardisedValues = TunaDataFileRead.readValues(filename);
		classification = TunaDataFileRead.readClassifications(filename);
	}
	
	public void setDimensionsToShow(boolean meanDepth, boolean medianDepth, boolean sdDepth, boolean iqrDepth,
										boolean meanTemp, boolean medianTemp, boolean sdTemp, boolean iqrTemp){
		//Set the dimensions that the next created graph should show
		dimensionsToShow[0] = meanDepth;
		dimensionsToShow[1] = medianDepth;
		dimensionsToShow[2] = sdDepth;
		dimensionsToShow[3] = iqrDepth;
		dimensionsToShow[4] = meanTemp;
		dimensionsToShow[5] = medianTemp;
		dimensionsToShow[6] = sdTemp;
		dimensionsToShow[7] = iqrTemp;
	}
	
	public void setSampleData(double[] sampleData){
		//Set the sample data (May/May not be shown on the graph
		for(int x = 0; x < 8; x++)
			this.sampleData[x] = sampleData[x];
	}
	
	public void showSample(boolean show){
		//Set wheter sample will be shown on the graph
		showSample = show;
	}
	
	public JPanel lineChart(){
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		//For all the classified sets we have
		for(int y = 0; y < unstandardisedValues.get(0).size(); y++){
			XYSeries series = new XYSeries("Test" + y);
			
			//Create a series containing the dimensions that have been set
			int t = 0;
			for(int x = 0; x < 8; x++)
				if(dimensionsToShow[x]){	
					series.add(t, unstandardisedValues.get(x).get(y));
					t++;
				}
				
			//Add the series to the dataset
			dataset.addSeries(series);
		}
		
		//Add the sample if the user wants it
		if(showSample){
			XYSeries testDataSeries = new XYSeries("Test sample");
			int t = 0;
			for(int x = 0; x < 8; x++)
				if(dimensionsToShow[x]){	
					testDataSeries.add(t, sampleData[x]);
					t++;
				}
			dataset.addSeries(testDataSeries);
		}
		
		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Classifications",      // chart title
				"Category",                      // x axis label
				"Value",                      // y axis label
				dataset,                  // data
				PlotOrientation.VERTICAL,
				true,                     // include legend
				true,                     // tooltips
				false                     // urls
				);

		chart.setBackgroundPaint(Color.white);

		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);

		//Create a custom legend. One item for each category
		LegendItemCollection legendItems =new LegendItemCollection();
		legendItems.add(new LegendItem("Shallow Behaviour", Color.GREEN));
		legendItems.add(new LegendItem("Thermocline Association", Color.RED));
		legendItems.add(new LegendItem("U-Shaped Dive", Color.ORANGE));
		legendItems.add(new LegendItem("V-Shaped Dive", Color.BLUE));
		legendItems.add(new LegendItem("Unclassified", Color.CYAN));
		
		//Add an extra legend item if the sample is displayed
		if(showSample)
			legendItems.add(new LegendItem("Sample", Color.BLACK));
		
		//Attach the custom legend to the graph
		plot.setFixedLegendItems(legendItems);
		
		//Create a list of the dimensions that will be shown
		ArrayList<String> dimensionsShown = new ArrayList<String>();
		for(int x = 0; x < 8; x++){
			if(dimensionsToShow[x])
				dimensionsShown.add(dimensionNames[x]);
		}
		String[] strArray = dimensionsShown.toArray(new String[dimensionsShown.size()]);
		
		//The above list is passed to be the labels on the X-axis
		SymbolAxis sa = new SymbolAxis("Measurement", strArray);
		plot.setDomainAxis(sa);
		
		//Show shapes if only one dimensions shown (Doesn't show up otherwise)
		boolean showShapes = (dimensionsShown.size() == 1);

		int seriesToColourCodeCount = dataset.getSeriesCount();
		if(showSample)
			seriesToColourCodeCount = dataset.getSeriesCount() - 1;
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for(int x = 0; x < seriesToColourCodeCount; x++){
			
			//Colour the series based on classification
			if(classification.get(x).matches("S"))
				renderer.setSeriesPaint(x, Color.GREEN);
		
			if(classification.get(x).matches("T"))
				renderer.setSeriesPaint(x, Color.RED);
			
			if(classification.get(x).matches("U"))
				renderer.setSeriesPaint(x, Color.ORANGE);
			
			if(classification.get(x).matches("V"))
				renderer.setSeriesPaint(x, Color.BLUE);
			
			if(classification.get(x).matches("TX"))
				renderer.setSeriesPaint(x, Color.CYAN);
			
			//Set shapes to be visible if only 1 dimension
			renderer.setSeriesShapesVisible(x, showShapes);
		}
		
		//If the sample is shown, it should be black and have the shapes visible
		if(showSample){
			renderer.setSeriesPaint(dataset.getSeriesCount() - 1, Color.BLACK);
			renderer.setSeriesShapesVisible(dataset.getSeriesCount() - 1, true);
		}
		plot.setRenderer(renderer);

		//Set both axis to have Integer intervals.
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 
		
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		return chartPanel;
	}
}















