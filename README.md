About:

data.txt holds 75 data samples, each representing a 12 hour period of activity for a tuna.

Dimensions: MeanDepth, MedianDepth, SDDepth, IQRDepth, MeanTemp, MedianTemp, SDTemp, IQRTemp.

Classification methods:
1 - KNN (1 vote per nearest neighbour)
2 - Weighted KNN (Nearer neighbours given higher vote)
3 - Squared Weighted KNN (Same as 2 except vote is squared)
4 - Bayes Naive Classifier

See report for full details

Accuracy tested by training on the training set (with one data point removed) then attempting to classifiy that data point.

----------------------------------------------------------------------

Instructions:

To classify a new data point:

TunaKNNClassifier.classify(boolean[] dimensions, double[] testData, int i, int method)

Please note that this overloads TunaKNNClassifier.classify(boolean[] dimensions, int i, int method) used for testing purposes only. 

Classification can also be done using the GUI:

Run Test class in order to use the GUI.
-GUI instructions can be found in the report. 


-------------------------------------------------

Testing Options (Class Test):

printOutBest():
Prints out the best accuracy found for each method

printOutBestsPerI():
Prints out the best accuracy found for each i/method combination and
the dimensions subset producing each accuracy.

printOutAverages():
Averages the accuracy of all the different dimension subsets for each
i/method combination.