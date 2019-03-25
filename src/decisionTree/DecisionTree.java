package decisionTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import dataRecording.DataTuple;
import dataRecording.DataTuple.DiscreteTag;
import pacman.game.Constants.MOVE;

public class DecisionTree {
	private Node root;
	
	public DecisionTree(DataTuple[] allData) {
		// Partition dataset in:
		// 80% training data (Used to build the decision tree)
		// 20% test data (Used to test the final accuracy of the tree)
		LinkedList<DataTuple> trainingData = new LinkedList<>();	// Data training set
		LinkedList<DataTuple> testData = new LinkedList<>();		// Test dataset
		for (int i = 0; i < allData.length; i++) {
			if (i > (int)(allData.length * 0.2)) {
				trainingData.add(allData[i]);
			} else {
				testData.add(allData[i]);
			}
		}
		
		TreeSet<String> attributeList = new TreeSet<>();
		attributeList.add("DirectionChosen");
		attributeList.add("pacmanPosition");
		attributeList.add("blinkyDist");
		attributeList.add("inkyDist");
		attributeList.add("pinkyDist");
		attributeList.add("sueDist");
		attributeList.add("isBlinkyEdible");
		attributeList.add("isInkyEdible");
		attributeList.add("isPinkyEdible");
		attributeList.add("isSueEdible");
		attributeList.add("blinkyDir");
		attributeList.add("inkyDir");
		attributeList.add("pinkyDir");
		attributeList.add("sueDir");
		attributeList.add("dangerLevel");
		
		// discretize -> list of String-arrays (filteredData)
		LinkedList<String[]> filteredData = new LinkedList<>();
		for (DataTuple tupleRow : trainingData) {
			String[] filteredRow = new String[15];
			
			filteredRow[0] = tupleRow.DirectionChosen.toString();
			filteredRow[1] = tupleRow.discretizePosition(tupleRow.pacmanPosition).toString();
			
			filteredRow[2] = tupleRow.discretizeDistance(tupleRow.blinkyDist).toString();
			filteredRow[3] = tupleRow.discretizeDistance(tupleRow.inkyDist).toString();
			filteredRow[4] = tupleRow.discretizeDistance(tupleRow.pinkyDist).toString();
			filteredRow[5] = tupleRow.discretizeDistance(tupleRow.sueDist).toString();
			
			filteredRow[6] = Boolean.toString(tupleRow.isBlinkyEdible);
			filteredRow[7] = Boolean.toString(tupleRow.isInkyEdible);
			filteredRow[8] = Boolean.toString(tupleRow.isPinkyEdible);
			filteredRow[9] = Boolean.toString(tupleRow.isSueEdible);
			
			filteredRow[10] = tupleRow.blinkyDir.toString();
			filteredRow[11] = tupleRow.inkyDir.toString();
			filteredRow[12] = tupleRow.pinkyDir.toString();
			filteredRow[13] = tupleRow.sueDir.toString();
			
			// Define "dangerLevel" based on proximity of closest non-edible ghost.
			ArrayList<Integer> ghostDist = new ArrayList<>();
			if (!tupleRow.isBlinkyEdible) ghostDist.add(tupleRow.blinkyDist);
			if (!tupleRow.isInkyEdible) ghostDist.add(tupleRow.inkyDist);
			if (!tupleRow.isPinkyEdible) ghostDist.add(tupleRow.pinkyDist);
			if (!tupleRow.isSueEdible) ghostDist.add(tupleRow.sueDist);
			int closestGhostDist = Integer.MAX_VALUE;
			if (!ghostDist.isEmpty()) {
				for (Integer dist : ghostDist) {
					if (dist < closestGhostDist) closestGhostDist = dist;
				}
			} else {
				closestGhostDist = -1;
			}
			filteredRow[14] = tupleRow.discretizeDistance(closestGhostDist).toString(); // "dangerLevel"
			
			filteredData.add(filteredRow);
		}
		
		this.root = generateTree(filteredData, attributeList);
	}
	
	public Node generateTree(LinkedList<String[]> data, TreeSet<String> remainingAttributesList) {
//		 1. Create node N.
		Node N = new Node();
		
		//Count classes for all tuples <MOVE,counter>
		TreeMap<String, Integer> movesCounter = new TreeMap<String, Integer>() {{
	        put("UP", 0);
	        put("RIGHT", 0);
	        put("DOWN", 0);
	        put("LEFT", 0);
	        put("NEUTRAL", 0);
	    }};
		for (String[] dataRow : data) {
			switch (MOVE.valueOf(dataRow[0])) {
			case UP: 		movesCounter.put("UP", movesCounter.get("UP") + 1);				break;
			case RIGHT:		movesCounter.put("RIGHT", movesCounter.get("RIGHT") + 1);		break;
			case DOWN:		movesCounter.put("DOWN", movesCounter.get("DOWN") + 1);			break;
			case LEFT:		movesCounter.put("LEFT", movesCounter.get("LEFT") + 1);			break;
			case NEUTRAL:	movesCounter.put("NEUTRAL", movesCounter.get("NEUTRAL") + 1);	break;
			default: 		System.out.println("Error counting tuples"); 					break;
			}
		}
		
//		 2. If every tuple in D has the same class C, return N as a leaf node labeled as C.
		boolean allSameClass = true, trigger = false;
		String leafMove = "";
		for (String move : movesCounter.keySet()) {
			if (movesCounter.get(move) > 0 && trigger == false) {
				trigger = true;
				leafMove = move;
			} else if (movesCounter.get(move) > 0 && trigger == true) {
				allSameClass = false;
				break;
			}
		}
		if (allSameClass == true) {
			N.isLeaf = true;
			N.leafClass = MOVE.valueOf(leafMove);
			return N;
		} else if (remainingAttributesList.isEmpty()) {
//			3. Otherwise, if the attribute list is empty, return N as a leaf node labeled with the majority class in D.
			String majorityClass = "";
			int maxValue = Integer.MIN_VALUE;
			for (String move : movesCounter.keySet()) {
				if (movesCounter.get(move).intValue() > maxValue) {
					majorityClass = move;
				}
			}
			N.isLeaf = true;
			N.leafClass = MOVE.valueOf(majorityClass);
			return N;
		} else {
//			4. Otherwise:
//			1. Call the attribute selection method on D and the attribute list, in order to choose the current attribute A: S(D, attribute list) -> A.
			String A = S(data, remainingAttributesList);
//			2. Label N as A and remove A from the attribute list.
			N.attributeName = A;
			N.isLeaf = false;
			remainingAttributesList.remove(A);
//			3. For each value aj in attribute A:
//				a) Separate all tuples in D so that attribute A takes the value aj, creating the subset Dj.
//				b) If Dj is empty, add a child node to N labeled with the majority class in D.
//				c) Otherwise, add the resulting node from calling Generate_Tree(Dj, attribute) as a child node to N.
			for (String Aj : A.possibleValues) {
				LinkedList<String[]> Dj = partitionData(A);
				if (Dj.isEmpty()) {
					Node child = new Node();
					N.branches.put(null, child);
				} else {
					Node child = generateTree(Dj, remainingAttributesList);
					N.branches.put(null, child);
				}
			}
		}

//		4. Return N.
		return N;
	}
	
	/*
	 * function for calculating max benefit (attribute selection method)
	 */
	private String S(LinkedList<String[]> data, TreeSet<String> attributeList) {
		return null;
	}

	// Print visual representation of the tree in console
	public void printTree() {
		System.out.println("A tree");
	}
	
	
	// A helper method that subdivide (partition) datasets based on attributes value. Test this good!
	private LinkedList<String[]> partitionData(String attribute) {
		return null;
	}
	
	public MOVE predictMove(DataTuple data) {
		// Extract attribute values
		// Traverse tree from this.root
		return MOVE.NEUTRAL;
	}
	
	private MOVE traverse(Node currentNode, String[] attributeValues) {
		if (currentNode.isLeaf == true){
			return currentNode.leafClass;
		} else {
//			String value = attributeValues[currentNode.attribute];
//			for (int i=0; i< currentNode.attributeValues.length; i++){
//				if(currentNode.attributeValues[i].equals(value)){
//					return predict(currentNode.nodes[i], attributeValues);
//				}
//			}
		}
		return null;
	}
	
	private class Node {
		public boolean isLeaf = false;
		public MOVE leafClass;			// Class/Label/MOVE (Leaf nodes only)
		
		// Only relevant if isLeaf == false
		public String attributeName;								// pacmanPosition, isBlinkyEdible, inkyDist
		public String[] possibleAttributeValues;					// boolean, MOVE, DiscreteTag
		private TreeMap<String,Node> branches = new TreeMap<>();	// <attrValue,childNode>
		
		public Node getChild(String attrValue) {
			if (!this.isLeaf) {
				throw new RuntimeException("getChild(): Node is leaf!");
			}
			return branches.get(attrValue);
		}
	}

}
