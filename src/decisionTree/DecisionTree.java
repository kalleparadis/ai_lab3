package decisionTree;

import java.util.LinkedList;

import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import pacman.game.Constants.MOVE;

public class DecisionTree {
	
	private DataTuple[] data;						// Data training set
	private LinkedList<DataTuple> attributeList;	// attribute list
	
	public void buildTree() {
		data = DataSaverLoader.LoadPacManData();
		MOVE directionChosen = data[0].DirectionChosen;
	}
	
	/*
	 * function for calculating max benefit (attribute selection method)
	 */
	private double S(DataTuple[] data) {
		return 0;
	}

}
