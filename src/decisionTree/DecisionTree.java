package decisionTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import dataRecording.DataTuple;
import pacman.game.Constants.MOVE;

public class DecisionTree {
	private final String[] ATTRIBUTE_ORDER;
	private final LinkedHashMap<String, String[]> FULL_ATTRIBUTE_LIST;
	private final double GLOBAL_ENTROPY;
	
	// Define the selected attributes and their possible values (attributeList)
	private final String[] MOVE_VALUES = {"UP", "RIGHT", "DOWN", "LEFT", "NEUTRAL"};
	private final String[] DISCRETE_TAG_VALUES = {"VERY_LOW", "LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NONE"};
	private final String[] BOOLEAN_VALUES = {"true", "false"};
	
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
		
		LinkedHashMap<String, String[]> attributeList = new LinkedHashMap<>();
		attributeList.put("directionChosen", MOVE_VALUES);			// MOVE
		attributeList.put("pacmanPosition", DISCRETE_TAG_VALUES);		// DiscreteTag
		attributeList.put("blinkyDist", DISCRETE_TAG_VALUES);
		attributeList.put("inkyDist", DISCRETE_TAG_VALUES);
		attributeList.put("pinkyDist", DISCRETE_TAG_VALUES);
		attributeList.put("sueDist", DISCRETE_TAG_VALUES);
		attributeList.put("isBlinkyEdible", BOOLEAN_VALUES);			// Boolean
		attributeList.put("isInkyEdible", BOOLEAN_VALUES);
		attributeList.put("isPinkyEdible", BOOLEAN_VALUES);
		attributeList.put("isSueEdible", BOOLEAN_VALUES);
		attributeList.put("blinkyDir", MOVE_VALUES);					// MOVE
		attributeList.put("inkyDir", MOVE_VALUES);
		attributeList.put("pinkyDir", MOVE_VALUES);
		attributeList.put("sueDir", MOVE_VALUES);
		attributeList.put("dangerLevel", DISCRETE_TAG_VALUES);		// DiscreteTag
		
		// Save the attribute order for later use, and a full copy of the attribute names and possible values.
		this.ATTRIBUTE_ORDER = attributeList.keySet().toArray(new String[0]);
		this.FULL_ATTRIBUTE_LIST = new LinkedHashMap<String, String[]>(attributeList);
		
		// Gather, filter, discretize data -> list of String-arrays (filteredData)
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
			
			// Preprocessor: Define "dangerLevel" based on proximity of closest non-edible ghost.
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
		
		// Calculate global entropty for targetClass "directionChosen"
		this.GLOBAL_ENTROPY = getEntropy(filteredData, "directionChosen");
		
		// Generate tree
		this.root = generateTree(filteredData, attributeList);
	}
	
	private double getEntropy(LinkedList<String[]> data, String attribute) {
		double entropy = 0d;
		TreeMap<String, Integer> attributeValueFrequencies = getAttributeValueFrequencies(data, attribute);
		double nbrOfTuples = (double)data.size(); 							// Total number of data rows
		String[] attributeValues = this.FULL_ATTRIBUTE_LIST.get(attribute);	// Get possible attributeValues for attribute
		for (String attributeValue : attributeValues) {
			double frequency = (double)attributeValueFrequencies.get(attributeValue) / (double)nbrOfTuples;
			entropy -= frequency * Math.log(frequency) / Math.log(2);
		}
		return entropy;
	}
	
	/**
	 * Count the frequency of each value for the attribute
	 */
	private TreeMap<String, Integer> getAttributeValueFrequencies(LinkedList<String[]> data, String attribute) {
		TreeMap<String, Integer> attrValueFrequencies = new TreeMap<String, Integer>();
		String[] attributeValues = this.FULL_ATTRIBUTE_LIST.get(attribute);
		
	    // Initialize map with all possible values of attribute and set counters to 0.
	    for (String attrValue : attributeValues) {
	    	attrValueFrequencies.put(attrValue, 0);
	    }
	    
	    int attributePosition = getAttributePosInRow(attribute);
		for (String[] dataRow : data) {
			for (int j = 0; j < attributeValues.length; j++) {
				String attrValue = dataRow[attributePosition];
				if (attrValue.equals(attributeValues[j])) {
					attrValueFrequencies.put(attrValue, attrValueFrequencies.get(attrValue) + 1);
				}
			}
		}
		return attrValueFrequencies;
	}
	
	public Node generateTree(LinkedList<String[]> data, LinkedHashMap<String, String[]> remainingAttributesList) {
//		 1. Create node N.
		Node N = new Node();
		
		// Count frequency of targetAttribute (classes) for all tuples. Will return <MOVE, counter>. targetClass == "directionChosen"
		TreeMap<String, Integer> classCounter = getAttributeValueFrequencies(data, "directionChosen");
		
		// Is D is all the same class
		boolean isAllSameClass = true, trigger = false;
		String allSameClass = "";
		for (String move : classCounter.keySet()) {
			if (classCounter.get(move) > 0 && trigger == false) {
				trigger = true;
				allSameClass = move;
			} else if (classCounter.get(move) > 0 && trigger == true) {
				isAllSameClass = false;
				break;
			}
		}
		
		// Get majority class in D
		String majorityClass = "";
		int maxValue = Integer.MIN_VALUE;
		for (String move : classCounter.keySet()) {
			if (classCounter.get(move).intValue() > maxValue) {
				majorityClass = move;
			}
		}
		
//		 2. If every tuple in D has the same class C, return N as a leaf node labeled as C.
		if (isAllSameClass == true) {
			N.isLeaf = true;
			N.leafClass = MOVE.valueOf(allSameClass);
			return N;
		} else if (remainingAttributesList.isEmpty()) {
//			3. Otherwise, if the attribute list is empty, return N as a leaf node labeled with the majority class in D.
			N.isLeaf = true;
			N.leafClass = MOVE.valueOf(majorityClass);
			return N;
		} else {
//			4. Otherwise:
//			1. Call the attribute selection method on D and the attribute list, in order to choose the current attribute A: S(D, attribute list) -> A.
			String attributeNameA = S(data, remainingAttributesList);
//			2. Label N as A and remove A from the attribute list.
			N.attributeName = attributeNameA;
			N.isLeaf = false;
			remainingAttributesList.remove(attributeNameA);
//			3. For each value Aj in attribute A:
//				a) Separate all tuples in D so that attribute A takes the value Aj, creating the subset Dj.
//				b) If Dj is empty, add a child node to N labeled with the majority class in D.
//				c) Otherwise, add the resulting node from calling Generate_Tree(Dj, attribute) as a child node to N.
			for (String Aj : remainingAttributesList.get(attributeNameA)) {
				LinkedList<String[]> Dj = partitionData(data, attributeNameA, Aj);
				if (Dj.isEmpty()) {
					Node childNode = new Node();
					childNode.isLeaf = true;
					childNode.leafClass = MOVE.valueOf(majorityClass); // Is it majorityClass of D or Dj?
					N.branches.put(Aj, childNode);
				} else {
					Node childNode = generateTree(Dj, remainingAttributesList);
					N.branches.put(Aj, childNode);
				}
			}
		}

//		4. Return N.
		return N;
	}
	
	
	/**
	 * Function for calculating max benefit (attribute selection method) by Information Gain.
	 * @return The max benefit AttributeName
	 */
	private String S(LinkedList<String[]> data, LinkedHashMap<String, String[]> remainingAttributesList) {
		String mostGainAttribute = "";
		double highestGain = Double.MIN_VALUE;
		for (String attribute : remainingAttributesList.keySet()) {
			double gain = calculateGain(data, attribute); 
			if (gain > highestGain) {
				mostGainAttribute = attribute;
				highestGain = gain;
			}
		}
		return mostGainAttribute;
	}
	
	/*
	 * Gain(A) = Info(D) - InfoA(D)
	 * Info(D) = -Emi=1pilog2(pi)					-> The expected information of the dataset (the average information)
	 * InfoA(D) = Evj=1 (|Dj|/|D|) * Info(Dj)		-> The expected information of the attribute when you divide D in relation to A.
	 */
	private double calculateGain(LinkedList<String[]> data, String attribute) {
		// Count the frequency of each value for the attribute
		TreeMap<String, Integer> attrValuesFrequencies = getAttributeValueFrequencies(data, attribute);
		
		// Calculate the gain
		double sum = 0;
		double totalNbrOfTuples = (double)data.size(); // Total number of data rows: |D| the number of tuples in D.
		for (Entry<String, Integer> attrValueFrequency : attrValuesFrequencies.entrySet()) {
			sum += attrValueFrequency.getValue() / totalNbrOfTuples * getEntropy(data, attribute);
		}
		return GLOBAL_ENTROPY - sum;
	}
	
	/**
	 * A helper method that subdivide (partition) datasets based on attributeValue. Test this good!
	 */
	private LinkedList<String[]> partitionData(LinkedList<String[]> data, String attributeName, String attributeValue) {
		LinkedList<String[]> partitionedData = new LinkedList<>();

		// For every dataRow in dataSet, if the attributeValue for A equals Aj, copy the row to new dataSet
		for (String[] dataRow : data) {
			int attrPos = getAttributePosInRow(attributeName);
			if (dataRow[attrPos].equals(attributeValue)) {
				partitionedData.add(dataRow);
			}
		}
		return partitionedData;
	}
	
	/**
	 * Go from name of an attribute to it's position in a data row.
	 */
	private int getAttributePosInRow(String attributeName) {
		int attributePos = -1;
		for (int i = 0; i < this.ATTRIBUTE_ORDER.length; i++) {
			if (ATTRIBUTE_ORDER[i].equals(attributeName)) {
				attributePos = i;
				break;
			}
		}
		if (attributePos == -1) throw new RuntimeException("getAttributePosInRow() Attribute doesn't exist: " + attributeName);
		return attributePos;
	}

	// Print visual representation of the tree in console
	public void printTree() {
		// TODO: Implement this
		System.out.println("A tree");
	}

	public MOVE predictMove(DataTuple data) {
		// TODO: Implement this
		// Extract attribute values
		// Traverse tree from this.root
		return MOVE.NEUTRAL;
	}
	
	private MOVE traverse(Node currentNode, String[] attributeValues) {
		// TODO: Implement this
		if (currentNode.isLeaf == true) {
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
		public TreeMap<String, Node> branches = new TreeMap<>();	// <attrValue, childNode>
		
		public Node getChild(String attrValue) {
			if (!this.isLeaf) throw new RuntimeException("getChild(): Node is a leaf!");
			Node childNode = branches.get(attrValue);
			if (childNode == null) throw new RuntimeException("getChild(): there is no branch for attrValue: " + attrValue);
			return childNode;
		}
	}

}
