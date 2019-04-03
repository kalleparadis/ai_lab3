package pacman.entries.pacman;

import java.util.LinkedList;

import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import decisionTree.DecisionTree;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class MyPacMan extends Controller<MOVE>
{
	private DecisionTree dt;
	private LinkedList<String[]> trainingData = new LinkedList<>();	// Data training set
	private LinkedList<String[]> testData = new LinkedList<>();		// Test dataset
	
	public MyPacMan() {
		super();
		
		// Full dataset
		DataTuple[] allTuples = DataSaverLoader.LoadPacManData();
		
		// Partition dataset in:
		// 80% training data (Used to build the decision tree)
		// 20% test data (Used to test the final accuracy of the tree)
		for (int i = 0; i < allTuples.length; i++) {
			if (i > (int)(allTuples.length * 0.2)) {
				trainingData.add(DecisionTree.getFilteredDataRow(allTuples[i]));
			} else {
				testData.add(DecisionTree.getFilteredDataRow(allTuples[i]));
			}
		}
		
		dt = new DecisionTree(trainingData); // Build the tree
		
		// Calculate accuracy
		System.out.println("Accuracy(training dataset): " + dt.getAccuracy(trainingData));
		System.out.println("Final accuracy(test dataset): " + dt.getAccuracy(testData));
		
//		dt.printTree();
	}
	
	public MOVE getMove(Game game, long timeDue) 
	{
		// Get current game state
		DataTuple gameStateData = new DataTuple(game, game.getPacmanLastMoveMade());
		
		// Send attributes of current game state to decision tree and receive MOVE
		String[] dataRow = DecisionTree.getFilteredDataRow(gameStateData);
		String predictedMove = dt.predictMove(dataRow);
		MOVE move = MOVE.valueOf(predictedMove);
		
//		System.out.println(gameStateData.getSaveString() + " ---> " + predictedMove);
//		System.out.println(move);
		
		return move;
	}
}