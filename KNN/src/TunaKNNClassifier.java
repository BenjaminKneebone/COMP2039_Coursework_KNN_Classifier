import java.util.ArrayList;

public class TunaKNNClassifier {

	//Number of dimensions in the test set
	private int noDimensions = 8;
	
	//Number of expert-classified points of data we have in the test set(Classified point = 12 hour period)
	private int testSetSize;
	
	//Number of points for each classification (S,T,U,V,TX) (Stored for Bayes Naive classifier)
	private int[] noOfEachClass = new int[5];
	
	//Stores the bayes naive value necessary (e.g tally of U * (1/Total number of U))
	private double[] tallyOverTotal;
	
	//Holds the raw data read in from the file (Unstandardised test set)
	private ArrayList<String> classification;
	private ArrayList<ArrayList<Double>> unstandardisedTestSet;
	
	//Information about each dimension
	private double[] meanValues = new double[noDimensions];
	private double[] sdValues = new double[noDimensions];
		
	//Holds all the standardised values (Standardised test set)
	private ArrayList<ArrayList<Double>> standardisedTestSet = new ArrayList<ArrayList<Double>>();
		
	//The sample to be classified
	private double[] unstandardisedSampleData = new double[noDimensions];
	private double[] standardisedSampleData = new double[noDimensions];
	
	// Should the classifier be reset after a one left out test. 
	private boolean reset = true;
	
	public TunaKNNClassifier(String filename){
		unstandardisedTestSet = TunaDataFileRead.readValues(filename);
		classification = TunaDataFileRead.readClassifications(filename);
		testSetSize = unstandardisedTestSet.get(0).size();
		
		resetClassifierAllDimensions();
		
		//Count frequency of each classification for Bayes Naive in the test set
		countClassifications();
	}
	
	/**
	 * Recalculate the mean, S.D values and re-standardise the test set. (All dimensions)
	 */
	private void resetClassifierAllDimensions(){
		calculateMeanAndSDValuesAllDimensions();
		
		//Calculate the standardised test set
		standardiseTestSetAllDimensions();	
	}
		
	/**
	 * Update the arrays holding Mean and Standard Deviation for the testSet
	 * that has been read in from file and classified by experts. 
	 */
	private void calculateMeanAndSDValuesAllDimensions(){
		//Calculate the mean and standard deviation for each dimension
		for(int x = 0; x < noDimensions; x++){
			meanValues[x] = ArrayListMath.mean(unstandardisedTestSet.get(x));
			sdValues[x] = ArrayListMath.standardDeviation(unstandardisedTestSet.get(x), meanValues[x]);		
		}
	}
	
	/**
	 * Update the standardised values for all dimensions(Does not calculate mean/sd, uses stored values) 
	 */
	private void standardiseTestSetAllDimensions(){
		//Scale the data for each dimension by the method of standard score
		
		//New list of arraylists created so unstandardised double objects are not affected and still accessible
		standardisedTestSet = new ArrayList<ArrayList<Double>>();
		
		//Scale each list of data 
		for(int x = 0; x < noDimensions; x++)
				standardisedTestSet.add(ArrayListMath.scaleDataByStandardScore(unstandardisedTestSet.get(x), meanValues[x], sdValues[x]));
	}
	
	/**
	 * Update the standardised values of the sample data being stored.
	 */
	private void standardiseSample(){
		//Scale the sample
		for(int x = 0; x < noDimensions; x++)
			standardisedSampleData[x] = (unstandardisedSampleData[x] - meanValues[x]) / sdValues[x];
	}
	
	/**Set the noOfEachClass array to represent the frequency with which
	 * each classification occurs in the test set. 0-4 S,T,U,V,TX
	 */
	private void countClassifications(){
		for(int x = 0; x < 5; x++)
			noOfEachClass[x] = 0;
		
		for(String c: classification){
			if(c.matches("S")){
				noOfEachClass[0]++;
				continue;
			}
			
			if(c.matches("T")){
				noOfEachClass[1]++;
				continue;
			}
			
			if(c.matches("U")){
				noOfEachClass[2]++;
				continue;
			}
			
			if(c.matches("V")){
				noOfEachClass[3]++;
				continue;
			}
			
			if(c.matches("TX")){
				noOfEachClass[4]++;
				continue;
			}
		}
	}
	
	/**
	 * Returns a String representing the classification assigned to the data passed in, running
	 * a KNN-i classification.
	 * 
	 * @param dimensions A true value indicates this dimension should be included
	 * 0-Mean Depth, 1-Median Depth, 2-SD Depth, 3-IQR Depth, 4-Mean Temp, 5-Median Temp, 6-SD Temp, 7-IQR Temp.
	 * @param testData The double values from the 12 hour period to be classified
	 * @param i		   The i to use (KNN-i)
	 * @param method 
	 * 1-Normal KNN,  2-Weighted KNN,  3-Weighted(Squared) KNN, 4-Bayes Naive Classifier
	 * @return 		   The classification determined for the set of data
	*/
	public String classify(boolean[] dimensions, double[] testData, int i, int method){
		
		//Input sanitation
		if(i > testSetSize || i <= 0){
			System.out.println("Incorrect i -  Max: " + (testSetSize - 1) + " Min: 1");	
			return null;
		}
		
		if(method < 1 || method > 4){
			System.out.println("Please enter a valid method 1-4");
			return null;
		}
		
		//Set the sample as the given 12 hour values
		for(int x = 0; x < noDimensions; x++)
			unstandardisedSampleData[x] = testData[x];
		
		/*All mean and standard deviation calculations and scaling should have be done 
		 * using the full test set. 
		 */
		
		//Run the classification (KNN-i classifier)
		return classify(dimensions, i,method);
	}
	
	/**
	 * Classify the 12 hour period currently stored by this classifier. 
	 * @param dimensions A true value indiciates this dimension should be included in the classification process
	 * 0-Mean Depth, 1-Median Depth, 2-SD Depth, 3-IQR Depth, 4-Mean Temp, 5-Median Temp, 6-SD Temp, 7-IQR Temp.
	 * @param i KNN-i
	 * @param method
	 * 1-Normal KNN,  2-Weighted KNN,  3-Weighted(Squared) KNN, 4-Bayes Naive Classifier
	 * @return A String representation of the classification assigned
	 */
	private String classify(boolean[] dimensions, int i, int method){
		//Scale the sample we are testing
		standardiseSample();
		
		//Stores the i closest neighbours
		DistClass[] neighbours = new DistClass[i];
		//Initialise list of distances with value likely to never be exceeded
		for(int x = 0; x < i; x++)
			neighbours[x] = new DistClass(1000,"S");
		
		
		//Stores the distance between the sample point and a classified point
		double distance;
		//For each of the classified points
		for(int y = 0; y < testSetSize; y++){
			distance = 0;
			//For each dimension we are including
			for(int n = 0; n < noDimensions; n++){
				if(dimensions[n])
				//Q = Classified point of data y. 
				//Add the distance between the sample point and the known point (Squared) in dimension n. (e.g + (Pn - Qn) ^2) 
				distance += Math.pow(Math.abs(standardisedSampleData[n] - standardisedTestSet.get(n).get(y)), 2);
			}
			
			//If one of the i closest points
			if(distance < neighbours[i-1].distance){
				int x = i-1;
				//Loop through and find which position it belongs in. 
				while(x > 0){
					//If the next distance is smaller, then x is the position for new distance
					if(distance >= neighbours[x-1].distance)
						break;
					else
						x--;
				}
				
				//Move all neighbours further away than the one just found one place out
				for(int j = i - 1; j > x; j--)
					neighbours[j] = neighbours[j-1];
				
				//Add new neighbour in correct position
				neighbours[x] = new DistClass(distance, classification.get(y));
				
			}
			
			/*Another method is to add all distances to an arraylist and sort 
			 * the arraylist after calculating all the distances. This
			 * took slightly longer than the used method when timed. 
			 * The necessary code for this has been commented out below
			 *
			 *Add total difference and classification to the list of neighbours
			 *neighbours.add(new DistClass(distance,classification.get(y)));
			 */
		}
				
		/*
		Collections.sort(neighbours, new Comparator<DistClass>() {
			@Override
			public int compare(DistClass o1, DistClass o2) {
				if(o1.distance > o2.distance)
					return 1;
				else
					if(o2.distance > o1.distance)
						return -1;
					else
						return 0;
			}
        });
		 */
		
		int maxScore = i;
		
		if(i == 1)	
			//If we are just finding the nearest value, return the nearest value
			return neighbours[0].classification;
		else{
			//Need to run a poll of the nearest i classified points
		
			int[] tally = new int[5]; //Array S, T, U, V, TX
			
			for(int x = 0; x < i; x++){
				//Get the xth nearest classified set's classification
				String result = neighbours[x].classification;
				
				/*Add to the tally for the classification of the xth nearest set's classification
				 * Bayes Naive also needs frequency of occurence so adds 1 to the relevant count*/
				 
				if(result.matches("S")){
					switch(method){
					case 4:
					case 1: tally[0]++; break;
					case 2: tally[0] += (maxScore - x); break;
					case 3: tally[0] += Math.pow(maxScore - x, 2); break;
					}
					continue;
				}
				
				if(result.matches("T")){
					switch(method){
					case 4:
					case 1: tally[1]++; break;
					case 2: tally[1] += (maxScore - x); break;
					case 3: tally[1] += Math.pow(maxScore - x, 2); break;
					}
					continue;
				}
				
				if(result.matches("U")){
					switch(method){
					case 4:
					case 1: tally[2]++; break;
					case 2: tally[2] += (maxScore - x); break;
					case 3: tally[2] += Math.pow(maxScore - x, 2); break;
					}
					continue;
				}
				
				if(result.matches("V")){
					switch(method){
					case 4:
					case 1: tally[3]++; break;
					case 2: tally[3] += (maxScore - x); break;
					case 3: tally[3] += Math.pow(maxScore - x, 2); break;
					}
					continue;
				}
				
				if(result.matches("TX")){
					switch(method){
					case 4:
					case 1: tally[4]++; break;
					case 2: tally[4] += (maxScore - x); break;
					case 3: tally[4] += Math.pow(maxScore - x, 2); break;
					}
					continue;
				}
			}

			int winner; 
	
			if(method == 4){
					
				tallyOverTotal = new double[5];
				//Frequency of classification * (1/Frequency of classification in dataset)
				for(int x = 0; x < 5; x++)
					tallyOverTotal[x] = (double) tally[x] / noOfEachClass[x];

				winner = arrayWinner(tallyOverTotal);
				
			}else{
				//Check if there is an outright winner in the tally
				winner = arrayWinner(tally);
			}
			
			//i-1 is the index of the last neighbour we checked
			int x = i - 1;
			
			//While there is not an outright winner
			while(winner == -1){
					
				/*Note: If KNN-i classifier, we decrement so that we will
				 * have a guaranteed winner (At KNN-1 if necessary). 
				 * (Reversing the process in which the tallys were increased). 
				 * If we were to increment we may still end with a draw 
				 * even when all testset points have been included */
				
				//Get the next classification to be 'un-counted'
				String result = neighbours[x].classification;
								
				if(result.matches("S"))
					switch(method){
					case 4: tallyOverTotal[0] = (double) (--tally[0] / noOfEachClass[0]); break;
					case 1: tally[0]--; break;
					case 2: tally[0] -= (maxScore - x); break;
					case 3: tally[0] -= Math.pow(maxScore - x, 2); break;
					}
				
				if(result.matches("T"))
					switch(method){
					case 4: tallyOverTotal[1] = (double) (--tally[1] / noOfEachClass[1]); break;
					case 1: tally[1]--; break;
					case 2: tally[1] -= (maxScore - x); break;
					case 3: tally[1] -= Math.pow(maxScore - x, 2); break;
					}
				
				if(result.matches("U"))
					switch(method){
					case 4: tallyOverTotal[2] = (double) (--tally[2] / noOfEachClass[2]); break;
					case 1: tally[2]--; break;
					case 2: tally[2] -= (maxScore - x); break;
					case 3: tally[2] -= Math.pow(maxScore - x, 2); break;
					}
				
				
				if(result.matches("V"))
					switch(method){
					case 4: tallyOverTotal[3] = (double) (--tally[3] / noOfEachClass[3]); break;
					case 1: tally[3]--; break;
					case 2: tally[3] -= (maxScore - x); break;
					case 3: tally[3] -= Math.pow(maxScore - x, 2); break;
					}
				
				if(result.matches("TX"))
					switch(method){
					case 4: tallyOverTotal[4] = (double) (--tally[4] / noOfEachClass[4]); break;
					case 1: tally[4]--; break;
					case 2: tally[4] -= (maxScore - x); break;
					case 3: tally[4] -= Math.pow(maxScore - x, 2); break;
					}	
				
				//See if we now have an outright winner
				if(method == 4)
					winner = arrayWinner(tallyOverTotal);
				else
					winner = arrayWinner(tally);
						
				x--;
			}
			
			//We have an outright winner, return that classification
			switch(winner){
			case 0: return "S";
			case 1: return "T";
			case 2: return "U";
			case 3: return "V";
			case 4: return "TX";
			}
			
		}
		return null; //Java insists on having it here as the switch statement only covers 0-4
	}
	
	/**
	 * @author Ben
	 * Stores the distance between the sample point and a classified point and the classification 
	 * of the classified point. 
	 */
	private class DistClass{
		double distance;
		String classification;
	
		DistClass(double d, String c){
			distance = d;
			classification = c;
		}
	}

	/**
	 * Returns the index of the array location with the highest int value. -1 if two values
	 * share the highest magnitude
	 * @param tally The array of int to be polled
	 * @return Index of the winning location. -1 for a draw.
	 */
	private int arrayWinner(int[] tally){
		//Check if the tally has an outright winner
		
		//True if the highest value occurs more than once
		boolean equalHighestCount = false;
		
		//Assume S has the highest number of votes
		int winner = 0;
		
		//Check if T, U, V, TX have a higher number of votes
		for(int x = 1; x < 5; x++){
			if(tally[x] > tally[winner]){
				winner = x;
				//We have a current outright winner
				equalHighestCount = false;
			}else
				if(tally[x] == tally[winner])
					//The highest value so far is duplicated
					equalHighestCount = true;
		}
		
		if(equalHighestCount)
			//No outright highest winner
			return -1;
		else
			//Else we have an outright winner
			return winner;
	}
	
	/**
	 * Returns the index of the array location with the highest double value. (And
	 * hence probability)  
	 * -1 if two values share the highest magnitude
	 * @param tally The double array to be polled
	 * @return Index of the winning location. -1 for a draw.
	 */
	private int arrayWinner(double[] tally){
		
		//True if the highest probability occurs more than once
		boolean equalHighestProbability = false;
		
		//Assume S has the highest probability
		int winner = 0;
		
		//Check if T, U, V, TX have a higher probability
		for(int x = 1; x < 5; x++){
			if(tally[x] > tally[winner]){
				winner = x;
				//We have a current outright winner
				equalHighestProbability = false;
			}else
				if(tally[x] == tally[winner])
					//The highest probability so far is duplicated
					equalHighestProbability = true;
		}
		
		if(equalHighestProbability)
			//No outright highest winner
			return -1;
		else
			//Else we have an outright winner
			return winner;
	}
		
	/**
	 * Runs all possible combinations of dimensions for KNN-iMin to KNN-iMax using the chosen method. Prints out
	 * the best percentage along with the configuration that produced it. 
	 * @param iMin i value to test up from and including
	 * @param iMax i value to test up to and including
	 * @param method
	 * 1-Normal KNN,  2-Weighted KNN,  3-Weighted(Squared) KNN, 4-Bayes Naive Classifier
	 */
	public void findBestAccuracy(int iMin, int iMax, int method){
		/*Tests the KNN-i classifier to find the optimal solution (Both i and the combination
		 * of dimensions that should be set to true). 
		 * 
		 * For each i:
		 * Checks every combination of dimension selection. (2^n -1 tests as all false excluded).
		 * 
		 * It puts every result into a tree map, then returns the highest value. (Best percentage)
		 * 
		 * i * (2^n - 1) classifications are performed. 
		 */
			
		//Input sanitation
		if(iMin > iMax){
			System.out.println("iMin should be <= to iMax");		
			return;
		}
		
		if(iMin <= 0){
			System.out.println("iMin should be >= 1");
			return;
		}
		
		if(iMax > (testSetSize - 1)){
			System.out.println("iMax should be <= " + (testSetSize - 1));
			return;
		}
		
		if(method < 1 || method > 4){
			System.out.println("Please enter a valid method 1-4");
			return;
		}
		
		
		//Do not reset classifier after each one left out test (No point in doing so
		//as next iteration will change them again)
		reset = false;
		
		//Stores the percentage accuracy, along with the i and an index which has
		//a one-to-one mapping with a configuration of the dimensions.
		double percentage = 0;
		int index = 0;
		int KNN = 0;
		
		//Total number of configurations of the dimensions
		int noCombinations = (int) Math.pow(2, noDimensions);
		
		for(int i = iMin; i <= iMax; i++){
			
			//This combination is not tested as last dimension is changed before first run
			boolean[] dimensions = {false,false,false,false,false,false,false,false};
			
			//Runs through all 2^n combinations - 1 (Excluding all false)
 			for(int x = 1; x <= noCombinations - 1; x++){	
				
 				/*
				for(int y = 0; y < noDimensions; y++){				
					if(x % (noCombinations/ Math.pow(2,(y+1))) == 0)
						dimensions[y] = !dimensions[y];
				}
				
				 * This for loop is a loop representation of the code below, 
				 * but adapted to be dynamic based on the number of dimensions being tested.
				 * If you know the number of dimensions, the code below is less computationally
				 * intensive, however would have to be amended if the number of dimensions was not
				 * 8).
 				*/
				
				//Tests all possible combinations of dimension selection				
				if(x == 128)
					dimensions[0] = true;
				
				if(x % 64 == 0)
					dimensions[1] = !dimensions[1];
				
				if(x % 32 == 0)
					dimensions[2] = !dimensions[2];
				
				if(x % 16 == 0)
					dimensions[3] = !dimensions[3];
				
				if(x % 8 == 0)
					dimensions[4] = !dimensions[4];
				
				if(x % 4 == 0)
					dimensions[5] = !dimensions[5];
				
				if(x % 2 == 0)
					dimensions[6] = !dimensions[6];
				
				dimensions[7] = !dimensions[7];
				
				//Calculate for this i and x
				/*Mean/Sd/Scaled data does not need to be reset as we will not be doing 
				a classification and altering the test set before the next classification */
				double accuracy = oneLeftOutTest(dimensions, i,method);
				if(accuracy > percentage){
					percentage = accuracy;
					index = x;
					KNN = i;
				}
			}
		}
		
		//Put together the list of dimensions that produced the optimal solution
		String optimalDimensions = "Dimensions: ";

		if(index >= 127) //Mean depth set to true on 128
			optimalDimensions += " Mean Depth/";

		if((index / 64) % 2 != 0) //Median depth switched on multiples of 64.
			optimalDimensions += " Median Depth/";

		if((index / 32) % 2 != 0)
			optimalDimensions += "SD Depth/";

		if((index / 16) % 2 != 0)
			optimalDimensions += "IQR Depth/";

		if((index / 8) % 2 != 0)
			optimalDimensions += "Mean Temp/";

		if((index / 4) % 2 != 0)
			optimalDimensions += "Median Temp/";

		if((index / 2) % 2 != 0)
			optimalDimensions += "SD Temp/";

		if(index % 2 != 0) //IQR temp switched every iteration
			optimalDimensions += "IQR Temp/";

		//Print out the highest percentage, along with the associated i
		System.out.printf("Best accuracy achieved: %.2f \n",percentage);
		System.out.println("KNN-" + KNN);

		//Print out the associated dimension combination
		System.out.println(optimalDimensions);

		//The values should be reset after a call to one left out test from now on
		reset = true;
				
		resetClassifierAllDimensions();
	}
		
	/**
	 * Runs all possible configurations for KNN-i using the chosen method. Prints out
	 * the average percentage.
	 * @param iMax The i value to test
	 * @param method
	 * 1-Normal KNN,  2-Weighted KNN,  3-Weighted(Squared) KNN, 4-Bayes Naive Classifier
	 */
	public void findAverageAccuracy(int i, int method){
		/*Tests the KNN-i classifier to find the average percentage
		 * 
		 * For i:
		 * Checks every combination of dimension selection. (2^n -1 tests as all false excluded).
		 * 
		 * It puts every result into an ArrayList, then returns the average percentage.
		 * 
		 * (2^n - 1) classifications are performed. 
		 */
		
		//Input sanitation
		if(i > testSetSize || i <= 0){
			System.out.println("Incorrect i -  Max: " + (testSetSize - 1) + " Min: 1");	
			return;
		}
		
		if(method < 1 || method > 4){
			System.out.println("Please enter a valid method 1-4");
			return;
		}
		
		//Do not rescale the data after each one left out test (No point in doing so
		//as next iteration will change them again)
		reset = false;

		//Stores the percentages
		ArrayList<Double> percentages = new ArrayList<Double>();

		//No dimensions tested (This combination is not tested as last dimension
		//is changed before first run
		boolean[] dimensions = {false,false,false,false,false,false,false,false};

		//Total number of configurations of the dimensions
		int noCombinations = (int) Math.pow(2, noDimensions);

		//Runs through all 2^n combinations (Excluding all false)
		for(int x = 1; x <= noCombinations - 1; x++){	

			/*
					for(int y = 0; y < noDimensions; y++){				
						if(x % (noCombinations/ Math.pow(2,(y+1))) == 0)
							dimensions[y] = !dimensions[y];
					}

			 * This for loop is a loop representation of the code below, 
			 * but adapted to be dynamic based on the number of dimensions being tested.
			 * If you know the number of dimensions, the code below is less computationally
			 * intensive, however would have to be amended if the number of dimensions was not
			 * 8).
			 */

			//Tests all possible combinations of dimension selection				
			if(x == 128)
				dimensions[0] = true;

			if(x % 64 == 0)
				dimensions[1] = !dimensions[1];

			if(x % 32 == 0)
				dimensions[2] = !dimensions[2];

			if(x % 16 == 0)
				dimensions[3] = !dimensions[3];

			if(x % 8 == 0)
				dimensions[4] = !dimensions[4];

			if(x % 4 == 0)
				dimensions[5] = !dimensions[5];

			if(x % 2 == 0)
				dimensions[6] = !dimensions[6];

			dimensions[7] = !dimensions[7];

			//Add the accuracy to the arraylist
			percentages.add(oneLeftOutTest(dimensions, i,method));

		}

		//Print out the average percentage
		System.out.printf("Average percentage for KNN-%d Method: %d is %.2f \n", i, method,ArrayListMath.mean(percentages));

		//The classifier should be reset after each call to one left out test from now on
		reset = true;
		
		resetClassifierAllDimensions();
		
	}
	
	
	
	//---- ONE LEFT OUT TESTING ----
	
	/**Checks the number of correct classifications produced by the current configuration for the 
	 * given i.
	 * @param dimensions A true value indicates that this dimension will be included. 
	 * 0-Mean Depth, 1-Median Depth, 2-SD Depth, 3-IQR Depth, 4-Mean Temp, 5-Median Temp, 6-SD Temp, 7-IQR Temp.
	 * @param i KNN-i
	 * @param method
	 * 1-Normal KNN,  2-Weighted KNN,  3-Weighted(Squared) KNN, 4-Bayes Naive Classifier
	 * @return Accuracy of the current dimension selection and i value
	 */
	public double oneLeftOutTest(boolean[] dimensions, int i, int method){

		//Number of correct classifications
	    int correct = 0;
	    
	    //For every classified sample we have in the testset
		for(int sampleNo = 0; sampleNo < testSetSize; sampleNo++){
	    	
			//Get the classification we expect for this sample
			String expectedResult = classification.get(sampleNo);
			
			//Assign this sample as the values we wish to test
			getSample(sampleNo);
			
			//Remove it from the testset, as this is the one left out
			removeSample(sampleNo);
			
			/*Work out the mean/Sd values of the testset and scale the testset (For selected dimensions)
			  now the sample has been removed*/
			calculateMeanAndSDValues(dimensions);
			standardiseTestSet(dimensions);
						
			//If correct, increment the correct counter.
			if(classify(dimensions,i,method).matches(expectedResult))
				correct++;

			//Put the previously removed sample values back into the testset
			replaceSampleValues(sampleNo, expectedResult);
		}
		
		
		if(reset)	
			//Return classifier to previous state 
			resetClassifierAllDimensions();
		
		//Return the percentage of correct classifications we achieved
		return ((double) correct / unstandardisedTestSet.get(0).size()) * 100;
	}
	
	/**
	 * Assigns the sample from the test set to the double[] for the sample data
	 * @param sampleNo The test set index of the sample to be set as sample data
	 */
	private void getSample(int sampleNo){
		for(int x = 0; x < 8; x++)
			unstandardisedSampleData[x] = unstandardisedTestSet.get(x).get(sampleNo);
	}
	
	/**
	 * Removes the sample from the testset.
	 * @param sampleNo The index of the sample to be removed
	 */
	private void removeSample(int sampleNo){
		for(int x = 0; x < unstandardisedTestSet.size(); x++)
			unstandardisedTestSet.get(x).remove(sampleNo);
		
		String classif = classification.get(sampleNo);
		
		classification.remove(sampleNo);
		
		//One left out on each iteration
	    testSetSize --;
		
		//Maintain count of each classification 
		if(classif.matches("S")){
			noOfEachClass[0] --;
			return;
		}
		
		if(classif.matches("T")){
			noOfEachClass[1] --;
			return;
		}
		
		if(classif.matches("U")){
			noOfEachClass[2] --;
			return;
		}
		
		if(classif.matches("V")){
			noOfEachClass[3] --;
			return;
		}
		
		if(classif.matches("TX")){
			noOfEachClass[4] --;
			return;
		}
		
		
	}
	
	/**
	 * Returns the sample to the test set in the specified index
	 * @param sampleNo The index where the sample shall be returned to within the tes tset
	 * @param expectedResult The expert classification for this sample
	 */
	private void replaceSampleValues(int sampleNo, String expectedResult){
		//Replace values at index they were removed from
		for(int x = 0; x < noDimensions; x++)
			unstandardisedTestSet.get(x).add(sampleNo, unstandardisedSampleData[x]);
	
		//Replace the classification at the same index
		classification.add(sampleNo, expectedResult);	
		
		//Back to original number of classified points
		testSetSize++;
				
		//Maintain count of each classification 
		if(expectedResult.matches("S")){
			noOfEachClass[0] ++;
			return;
		}
		
		if(expectedResult.matches("T")){
			noOfEachClass[1] ++;
			return;
		}
		
		if(expectedResult.matches("U")){
			noOfEachClass[2] ++;
			return;
		}
		
		if(expectedResult.matches("V")){
			noOfEachClass[3] ++;
			return;
		}
		
		if(expectedResult.matches("TX")){
			noOfEachClass[4] ++;
			return;
		}
	}
	
	/**
	 * Update the arrays holding Mean and Standard Deviation for the testSet
	 * that has been read in from file and classified by experts. Only done 
	 * in dimensions selected
	 * @param dimensions A boolean array indicating (By being true) which dimensions should be standardised.
	 */
	private void calculateMeanAndSDValues(boolean[] dimensions){
		//Calculate the mean and standard deviation for each dimension
		for(int x = 0; x < noDimensions; x++){
			//No need to do the calculations for dimensions we are not concerned about
			if(dimensions[x]){
				meanValues[x] = ArrayListMath.mean(unstandardisedTestSet.get(x));
				sdValues[x] = ArrayListMath.standardDeviation(unstandardisedTestSet.get(x), meanValues[x]);
			}
		}
	}
	
	/**
	 * Update the standardised values being stored in selected dimensions (Does not calculate mean/sd, uses stored values) 
	 * @param dimensions A boolean array indicating (By being true) which dimensions should be standardised.
	 */
	private void standardiseTestSet(boolean[] dimensions){
		//Scale the data for each dimension by the method of standard score
		
		//New list of arraylists created so unstandardised double objects are not affected and still accessible
		standardisedTestSet = new ArrayList<ArrayList<Double>>();
		
		//Scale each list of data 
		for(int x = 0; x < noDimensions; x++)
			//No need to do the calculations for dimensions we are not concerned about
			if(dimensions[x])
				standardisedTestSet.add(ArrayListMath.scaleDataByStandardScore(unstandardisedTestSet.get(x), meanValues[x], sdValues[x]));
			else
				standardisedTestSet.add(new ArrayList<Double>());
	}
	
	//---- END OF ONE LEFT OUT METHODS ----
	
}


		
