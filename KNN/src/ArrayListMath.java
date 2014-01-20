import java.util.ArrayList;


public abstract class ArrayListMath {

	/**Returns the mean of the values stored in the ArrayList passed into the method
	 * @param values An instance of ArrayList<Double>
	 * @return The mean of the Double values
	 */
	public static double mean(ArrayList<Double> values){
		double total = 0;
		
		if(values.size() != 0){
			for(double value : values)
				total += value;
		
			return total/values.size();	
		}else{
			System.out.println("ArrayList size is 0. Mean could not be calculated");
			return 0;
		}
	}
	
	/**Returns the standard deviation of the values stored in the ArrayList<Double>
	 * passed into the method
	 * @param values An instance of ArrayList<Double>
	 * @return The standard deviation of the Double values
	 */
	public static double standardDeviation(ArrayList<Double> values){
		//Calculate the Standard deviation of the values, not given the mean
		
		double mean = ArrayListMath.mean(values);
		
		if(values.size() != 0){
		
			//Total of (x - Mean)^2
			double total = 0;
			
			for(double value: values)
				total += Math.pow(value - mean, 2);
			
			return Math.sqrt(total / (values.size() - 1));	
		}else{
			System.out.println("ArrayList size is 0. SD could not be calculated");
			return 0;
		}
	}
	
	/**Returns the standard deviation of the values stored in the ArrayList passed into the method
	 * @param values An instance of ArrayList<Double>
	 * @param mean The mean of the Double values
	 * @return The standard deviation of the Double values
	 */
	public static double standardDeviation(ArrayList<Double> values, double mean){
		//Calculate the Standard deviation of the values, given the mean
		
		if(values.size() != 0){
		
			//Total of (x - Mean)^2
			double total = 0;
			
			for(double value: values)
				total += Math.pow(value - mean, 2);
			
			return Math.sqrt(total / (values.size() - 1));	
		}else{
			System.out.println("ArrayList size is 0. SD could not be calculated");
			return 0;
		}
	}
	
	/**Returns a new ArrayList<Double> object holding the standardised scores of the 
	 * ArrayList<Double> passed in. ((x - mean) / sd)).
	 * Does not alter the ArrayList<Double> passed in
	 * @param values The returned Object will be this ArrayList<Double> standardised
	 * @return An ArrayList<Double> of standardised values
	 */
	public static ArrayList<Double>scaleDataByStandardScore(ArrayList<Double> values){
		//Scale by the process of ((x - mean) / sd)
		if(values.size() != 0){	
			double mean = ArrayListMath.mean(values);
			double sd = ArrayListMath.standardDeviation(values, mean);
			
			//Create a new array list so we do not alter the values of the list passed in
			ArrayList<Double> standValues = new ArrayList<Double>();
			
			for(double value : values)
				standValues.add((value - mean) / sd);		
			
			return standValues;
		}else{
			System.out.println("ArrayList to scale is empty");
			return null;
		}
	}
	
	/**Returns a new ArrayList<Double> object holding the standardised scores of the 
	 * ArrayList<Double> passed in. ((x - mean) / sd).
	 * Does not alter the ArrayList<Double> passed in
	 * @param values The returned Object will be this ArrayList<Double> standardised
	 * @mean The mean of the Double values passed in
	 * @sd The standard deviation of the Double values passed in
	 * @return An ArrayList<Double> of standardised values
	 */
	public static ArrayList<Double>scaleDataByStandardScore(ArrayList<Double> values, double mean,double sd){
		if(values.size() != 0){
			//Create a new array list so we do not alter the values of the list passed in
			ArrayList<Double> standValues = new ArrayList<Double>();
			
			for(double value : values)
				standValues.add((value - mean) / sd);		
			
			return standValues;
		}else{
			System.out.println("ArrayList to scale is empty");
			return new ArrayList<Double>();
		}
	}
}
