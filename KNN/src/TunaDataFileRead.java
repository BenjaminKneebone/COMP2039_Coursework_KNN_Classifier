import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class TunaDataFileRead {

	//Mean Depth ..... IQR Temp
	private static int noOfDimensions = 8;
	
	/**Returns an ArrayList<ArrayList<Double>> where each ArrayList<Double> stores data
	 * from one dimension from the specified file. It reads the file from line 1 assuming
	 * line 0 is column headers.
	 * @param filename The file holding the raw data.
	 * @return ArrayList<ArrayList<Double>> of the raw data
	 */
	public static ArrayList<ArrayList<Double>> readValues(String filename){
		
		//An array list for each of the measurements
		ArrayList<ArrayList<Double>> values = new ArrayList<ArrayList<Double>>();
		
		//Create the array lists
		for(int x = 0; x < noOfDimensions; x++)
			values.add(new ArrayList<Double>());
				
		Scanner s;
		try {
			s = new Scanner(new BufferedReader(new FileReader(filename)));
			
			s.nextLine();  //Skips column titles
			
			while (s.hasNext()) {
				//Read in each value to the relevant array list
				for(ArrayList<Double> list : values)
					list.add(s.nextDouble());
				
				//Skip the classification 
				s.next();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
				
		return values;
	}
	
	/**Returns an ArrayList<String> representing the classifications given 
	 * to the data points in the raw data file in the specified file. 
	 * It reads the file from line 1 assuming line 0 is column headers.
	 * @param filename The raw data file 
	 * @return ArrayList<String> of classifications
	 */
	public static ArrayList<String> readClassifications(String filename){
	
		//Holds all the classifications
		ArrayList<String> classification = new ArrayList<String>();
		
		Scanner s;
		try {
			s = new Scanner(new BufferedReader(new FileReader(filename)));
			
			s.nextLine();  //Skips column titles
			
			while (s.hasNext()) {
				//Skip the double values
				for(int x = 0; x < noOfDimensions; x++)
					s.nextDouble();
				
				//Read in the classification
				classification.add(s.next());
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		return classification;
	}
}
