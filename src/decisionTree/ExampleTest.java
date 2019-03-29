package decisionTree;

import java.util.Arrays;
import java.util.LinkedList;

public class ExampleTest {

	public static void main2(String[] args) {

		// Full dataset
		LinkedList<String[]> allData = getAllExampleData();
		
		DecisionTree dt;
		LinkedList<String[]> trainingData = new LinkedList<>();	// Data training set
		LinkedList<String[]> testData = new LinkedList<>();		// Test dataset
		
		// Partition dataset in:
		// 80% training data (Used to build the decision tree)
		// 20% test data (Used to test the final accuracy of the tree)
		for (int i = 0; i < allData.size(); i++) {
			if (i > (int)(allData.size() * 0.2)) {
				trainingData.add(allData.get(i));
			} else {
				testData.add(allData.get(i));
			}
		}
		
		dt = new DecisionTree(trainingData); // Build the tree
		
		// Calculate accuracy
		System.out.println("Accuracy(training dataset): " + dt.getAccuracy(trainingData));
		System.out.println("Final accuracy(test dataset): " + dt.getAccuracy(testData));
		
		dt.printTree();
		
//		-------------------------------------------------------------------------------
		
		// Get current game state
		String[] gameStateData = {"YOUTH", "HIGH", "false", "FAIR", "false"};
		
		// Send attributes of current game state to decision tree and receive target class prediction.
		String predictedClass = dt.predictMove(gameStateData);
		
		System.out.println(Arrays.deepToString(gameStateData) + " ---> " + predictedClass);
	}
	
	public static LinkedList<String[]> getAllExampleData() {
		LinkedList<String[]> allData = new LinkedList<>();
		allData.add(new String[] {"YOUTH", "HIGH", "false", "FAIR", "false"});
		allData.add(new String[] {"YOUTH", "HIGH", "false", "EXCELLENT", "false"});
		allData.add(new String[] {"MIDDLE_AGED", "HIGH", "false", "FAIR", "true"});
		allData.add(new String[] {"SENIOR", "MEDIUM", "false", "FAIR", "true"});
		allData.add(new String[] {"SENIOR", "LOW", "true", "FAIR", "true"});
		allData.add(new String[] {"SENIOR", "Low", "true", "EXCELLENT", "false"});
		allData.add(new String[] {"MIDDLE_AGED", "LOW", "true", "EXCELLENT", "true"});
		allData.add(new String[] {"YOUTH", "MEDIUM", "false", "FAIR", "false"});
		allData.add(new String[] {"YOUTH", "LOW", "true", "FAIR", "true"});
		allData.add(new String[] {"SENIOR", "MEDIUM", "true", "FAIR", "true"});
		allData.add(new String[] {"YOUTH", "MEDIUM", "true", "EXCELLENT", "true"});
		allData.add(new String[] {"MIDDLE_AGED", "MEDIUM", "false", "FAIR", "true"});
		allData.add(new String[] {"MIDDLE_AGED", "HIGH", "true", "FAIR", "true"});
		allData.add(new String[] {"SENIOR", "MEDIUM", "false", "EXCELLENT", "false"});
		return allData;
	}

}
