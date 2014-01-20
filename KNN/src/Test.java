
public class Test {

	TunaKNNClassifier knn = new TunaKNNClassifier("data.txt");
	
	
	public static void main(String[] args){

		Test t = new Test();
		//t.printOutBest();
		//t.printOutBestsPerI();
		//t.printOutAverages();
		GUI g = new GUI();
	}
	
	/**
	 *Prints out the average accuracy for each i and method specified.
	 *Averages the accuracy of all the different dimension subsets for each
	 *i/method combination.
	 */
	public void printOutAverages(){
		for(int i = 1; i <= 10; i++){
			knn.findAverageAccuracy(i, 1);
			knn.findAverageAccuracy(i, 2);
			knn.findAverageAccuracy(i, 3);
			knn.findAverageAccuracy(i, 4);
		}
	}
	
	/**
	 * Prints out the best accuracy found by any i/method combination
	 * between and including the two i values in the for loop.
	 */
	public void printOutBest(){
			knn.findBestAccuracy(1,10, 1);
			knn.findBestAccuracy(1,10, 2);
			knn.findBestAccuracy(1,10, 3);
			knn.findBestAccuracy(1,10, 4);
	}
	
	/**
	 * Prints out the best accuracy found for each i/method combination and
	 * the dimensions subset producing each accuracy.
	 */
	public void printOutBestsPerI(){
		for(int i = 1; i <= 10; i++){
			knn.findBestAccuracy(i,i, 1);
			knn.findBestAccuracy(i,i, 2);
			knn.findBestAccuracy(i,i, 3);
			knn.findBestAccuracy(i,i, 4);
		}
	}
}
