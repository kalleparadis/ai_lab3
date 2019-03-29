package decisionTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import dataRecording.DataTuple;

public class DecisionTree {
	private final String TARGET_ATTRIBUTE;
	private final String[] ATTRIBUTE_ORDER;
	private final LinkedHashMap<String, String[]> FULL_ATTRIBUTE_LIST;
	private final double GLOBAL_ENTROPY;
	
	// Define the selected attributes and their possible values (attributeList)
	private final String[] MOVE_VALUES = {"UP", "RIGHT", "DOWN", "LEFT", "NEUTRAL"};
	private final String[] DISCRETE_TAG_VALUES = {"VERY_LOW", "LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NONE"};
	private final String[] BOOLEAN_VALUES = {"true", "false"};
	
//	Only used by ExampleTest
//	private final String[] AGE_VALUES = {"YOUTH", "MIDDLE_AGED", "SENIOR"};
//	private final String[] INCOME_VALUES = {"LOW", "MEDIUM", "HIGH"};
//	private final String[] CREDIT_RATING_VALUES = {"FAIR", "EXCELLENT"};
	
	private Node root;
	private Random rand = new Random();
	
	public DecisionTree(LinkedList<String[]> trainingData) {
		
		// Define the selected attributes
		FULL_ATTRIBUTE_LIST = new LinkedHashMap<>();
//		-------------------------------------------------------------------------------
//		ExampleText
//		FULL_ATTRIBUTE_LIST.put("age", AGE_VALUES);
//		FULL_ATTRIBUTE_LIST.put("income", INCOME_VALUES);
//		FULL_ATTRIBUTE_LIST.put("student", BOOLEAN_VALUES);
//		FULL_ATTRIBUTE_LIST.put("creditRating", CREDIT_RATING_VALUES);
//		FULL_ATTRIBUTE_LIST.put("buyComputer", BOOLEAN_VALUES);
//		
//		this.TARGET_ATTRIBUTE = "buyComputer";
//		-------------------------------------------------------------------------------
		FULL_ATTRIBUTE_LIST.put("directionChosen", MOVE_VALUES);			// MOVE
		FULL_ATTRIBUTE_LIST.put("pacmanPosition", DISCRETE_TAG_VALUES);		// DiscreteTag
		FULL_ATTRIBUTE_LIST.put("blinkyDist", DISCRETE_TAG_VALUES);
		FULL_ATTRIBUTE_LIST.put("inkyDist", DISCRETE_TAG_VALUES);
		FULL_ATTRIBUTE_LIST.put("pinkyDist", DISCRETE_TAG_VALUES);
		FULL_ATTRIBUTE_LIST.put("sueDist", DISCRETE_TAG_VALUES);
		FULL_ATTRIBUTE_LIST.put("isBlinkyEdible", BOOLEAN_VALUES);			// Boolean
		FULL_ATTRIBUTE_LIST.put("isInkyEdible", BOOLEAN_VALUES);
		FULL_ATTRIBUTE_LIST.put("isPinkyEdible", BOOLEAN_VALUES);
		FULL_ATTRIBUTE_LIST.put("isSueEdible", BOOLEAN_VALUES);
		FULL_ATTRIBUTE_LIST.put("blinkyDir", MOVE_VALUES);					// MOVE
		FULL_ATTRIBUTE_LIST.put("inkyDir", MOVE_VALUES);
		FULL_ATTRIBUTE_LIST.put("pinkyDir", MOVE_VALUES);
		FULL_ATTRIBUTE_LIST.put("sueDir", MOVE_VALUES);
		FULL_ATTRIBUTE_LIST.put("dangerLevel", DISCRETE_TAG_VALUES);		// DiscreteTag
		
		// Save the attribute order for later use, and a full copy of the attribute names and possible values.
		this.ATTRIBUTE_ORDER = FULL_ATTRIBUTE_LIST.keySet().toArray(new String[0]);
		
		// Set the class that the tree is going to predict
		this.TARGET_ATTRIBUTE = "directionChosen";
		
		// Calculate global entropty for target attribute
		this.GLOBAL_ENTROPY = getEntropy(trainingData, TARGET_ATTRIBUTE);
		System.out.println("GLOBAL_ENTROPY: " + GLOBAL_ENTROPY);
		
		// Make a copy of the attribute list that will be manipulated when building the tree
		LinkedHashMap<String, String[]> attributeList = new LinkedHashMap<String, String[]>(this.FULL_ATTRIBUTE_LIST);
		
		// Generate tree
		this.root = generateTree(trainingData, attributeList);
	}
	
	public Node generateTree(LinkedList<String[]> data, LinkedHashMap<String, String[]> remainingAttributesList) {
	//		 1. Create node N.
			Node N = new Node();
			
			// Count frequency of target attribute for all tuples. Will return <String, frequency>.
			TreeMap<String, Integer> classCounter = getAttributeValueFrequencies(data, TARGET_ATTRIBUTE);
			
			// Is D is all the same class
			boolean isAllSameClass = true, trigger = false;
			String allSameClass = "";
			for (Entry<String, Integer> attrValueFreq : classCounter.entrySet()) {
				if (attrValueFreq.getValue() > 0) {
					if (trigger == false) {
						trigger = true;
						allSameClass = attrValueFreq.getKey();
					} else {
						isAllSameClass = false;
						allSameClass = "";
						break;
					}
				}
			}
			
			// Get majority class in D
			String majorityClass = getMajorityAttributeValue(classCounter);
			
	//		 2. If every tuple in D has the same class C, return N as a leaf node labeled as C.
			if (isAllSameClass == true) {
				N.isLeaf = true;
				N.leafClass = allSameClass;
				return N;
			} else if (remainingAttributesList.isEmpty()) {
	//			3. Otherwise, if the attribute list is empty, return N as a leaf node labeled with the majority class in D.
				N.isLeaf = true;
				N.leafClass = majorityClass;
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
				String[] attributeValues = getPossibleValuesOfAttribute(attributeNameA);
				for (String Aj : attributeValues) {
					LinkedList<String[]> Dj = partitionData(data, attributeNameA, Aj);
					if (Dj.isEmpty()) {
						Node childNode = new Node();
						childNode.isLeaf = true;
						childNode.leafClass = majorityClass; // Is it majorityClass of D (whole data) or Dj (partitioned data)?
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

	public double getAccuracy(LinkedList<String[]> filteredData) {
		int totalNbrOfTuples = filteredData.size();
		int nbrTuplesClassifiedCorrectly = 0;
		for (String[] dataRow : filteredData) {
			String correctMove = dataRow[getAttributePosInRow(TARGET_ATTRIBUTE)];
			String predictedMove = predictMove(dataRow);
			if (predictedMove.equals(correctMove)) {
				nbrTuplesClassifiedCorrectly++;
			}
		}
		return nbrTuplesClassifiedCorrectly / (double)totalNbrOfTuples;
	}
	
	/*
	 * Gather, filter, discretize data row
	 */
	public static String[] getFilteredDataRow(DataTuple tupleRow) {
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
		if (!tupleRow.isBlinkyEdible)
			ghostDist.add(tupleRow.blinkyDist);
		if (!tupleRow.isInkyEdible)
			ghostDist.add(tupleRow.inkyDist);
		if (!tupleRow.isPinkyEdible)
			ghostDist.add(tupleRow.pinkyDist);
		if (!tupleRow.isSueEdible)
			ghostDist.add(tupleRow.sueDist);
		int closestGhostDist = Integer.MAX_VALUE;
		if (!ghostDist.isEmpty()) {
			for (Integer dist : ghostDist) {
				if (dist < closestGhostDist)
					closestGhostDist = dist;
			}
		} else {
			closestGhostDist = -1;
		}
		filteredRow[14] = tupleRow.discretizeDistance(closestGhostDist).toString(); // "dangerLevel"
		
		return filteredRow;
	}
	
	private double getEntropy(LinkedList<String[]> data, String attribute) {
		double entropy = 0d;
		TreeMap<String, Integer> attrValueFrequencies = getAttributeValueFrequencies(data, attribute);
		double nbrOfTuples = (double)data.size(); 							// Total number of data rows
		String[] attributeValues = getPossibleValuesOfAttribute(attribute);
		for (String attributeValue : attributeValues) {
			int attrValueFreq = attrValueFrequencies.get(attributeValue);
			if (attrValueFreq > 0) {
				double frequency = (double)attrValueFreq / (double)nbrOfTuples;
				entropy -= frequency * Math.log(frequency) / Math.log(2);
			}
		}
		return entropy;
	}
	
	/**
	 * Count the frequency of each value for the attribute
	 */
	private TreeMap<String, Integer> getAttributeValueFrequencies(LinkedList<String[]> data, String attribute) {
		TreeMap<String, Integer> attrValueFrequencies = new TreeMap<String, Integer>();
		String[] attributeValues = getPossibleValuesOfAttribute(attribute);
		
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
	
	private String getMajorityAttributeValue(TreeMap<String, Integer> attrValueFrequencies) {
		String majorityAttrValue = "noMajorityExists";
		int maxFrequency = 0;
		int frequency;
		for (Entry<String, Integer> move : attrValueFrequencies.entrySet()) {
			frequency = move.getValue();
			if (frequency > maxFrequency) {
				majorityAttrValue = move.getKey();
				maxFrequency = frequency;
			}
		}
		// Return a random attribute if no majority exists
		if (majorityAttrValue.equals("noMajorityExists")) {
			Set<String> keys = attrValueFrequencies.keySet();
			int randomItemIndex = rand.nextInt(keys.size());
			int i = 0;
			for (String attrValue : attrValueFrequencies.keySet()) {
				if (i == randomItemIndex) {
					return attrValue;
				} else {
					i++;
				}
			}
		}
		return majorityAttrValue;
	}
	
	/**
	 * Function for calculating max benefit (attribute selection method) by Information Gain.
	 * @return The max benefit AttributeName
	 */
	private String S(LinkedList<String[]> data, LinkedHashMap<String, String[]> remainingAttributesList) {
		String mostGainAttribute = "S() no-attribute chosen";
		double highestGain = Double.NEGATIVE_INFINITY;
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
	
	private String[] getPossibleValuesOfAttribute(String attribute) {
		String[] possibleAttrValues = this.FULL_ATTRIBUTE_LIST.get(attribute);
		if (possibleAttrValues == null) {
			throw new RuntimeException("getPossibleValuesOfAttribute() null for attribute: " + attribute);
		}
		return possibleAttrValues;
	}

	// Print visual representation of the tree in console
	public void printTree() {
		print(root, " ", "");
	}
	
	private void print(Node node, String indent, String value) {
		String newIndent = indent + "    ";
		if (node.isLeaf == true) {
			System.out.println(indent + value + " = "+ node.leafClass);
		} else {
			System.out.println(indent + node.attributeName + "->");
			for (Entry<String, Node> s : node.branches.entrySet()) {
				print(s.getValue(), newIndent, s.getKey());
			}
		}
	}

	public String predictMove(String[] dataRow) {
		return traverse(root, dataRow);
	}
	
	private String traverse(Node currentNode, String[] attributeValues) {
		if (currentNode.isLeaf == true) {
			return currentNode.leafClass;
		} else {
			int attrPos = getAttributePosInRow(currentNode.attributeName);
			String attrValue = attributeValues[attrPos];
			return traverse(currentNode.getChild(attrValue), attributeValues);
		}
	}
	
	private class Node {
		public boolean isLeaf = false;
		public String leafClass;									// Class/Label/MOVE (Leaf nodes only)
		
		// Only relevant if isLeaf == false
		public String attributeName;								// pacmanPosition, isBlinkyEdible, inkyDist
		public TreeMap<String, Node> branches = new TreeMap<>();	// <attrValue, childNode>
		
		public Node getChild(String attrValue) {
			if (this.isLeaf) throw new RuntimeException("getChild(): Node has no children. Is's a leaf!");
			Node childNode = branches.get(attrValue);
			if (childNode == null) throw new RuntimeException("getChild(): there is no branch for attrValue: " + attrValue);
			return childNode;
		}
	}

}
