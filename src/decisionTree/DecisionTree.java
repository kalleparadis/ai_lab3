package decisionTree;

import java.util.LinkedList;
import java.util.TreeMap;

import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import dataRecording.DataTuple.DiscreteTag;
import pacman.game.Constants.MOVE;

public class DecisionTree {
	private DataTuple[] data;						// Data training set
	private LinkedList<Attribute> attributeList;	// attribute list
	
	public DecisionTree() {
		data = DataSaverLoader.LoadPacManData();
		//generate_Tree();
	}
	
	public Node generateTree(DataTuple[] data, LinkedList<Attribute> attributeList) {
//		 1. Create node N.
		Node n = new Node(new Attribute<DiscreteTag>());
		
		//Count classes for all tuples
		TreeMap<String, Integer> movesCounter = new TreeMap<String, Integer>() {{
	        put("UP", 0);
	        put("RIGHT", 0);
	        put("DOWN", 0);
	        put("LEFT", 0);
	        put("NEUTRAL", 0);
	    }};
		for (int i = 0; i < data.length; i++) {
			switch (data[i].DirectionChosen) {
			case UP: 		movesCounter.put("UP", movesCounter.get("UP").intValue() + 1);				break;
			case RIGHT:		movesCounter.put("RIGHT", movesCounter.get("RIGHT").intValue() + 1);		break;
			case DOWN:		movesCounter.put("DOWN", movesCounter.get("DOWN").intValue() + 1);			break;
			case LEFT:		movesCounter.put("LEFT", movesCounter.get("LEFT").intValue() + 1);			break;
			case NEUTRAL:	movesCounter.put("NEUTRAL", movesCounter.get("NEUTRAL").intValue() + 1);	break;
			default: 		System.out.println("Error"); 												break;
			}
		}
		
//		 2. If every tuple in D has the same class C, return N as a leaf node labeled as C.
		boolean allSameClass = true, trigger = false;
		String leafMove = "";
		for (String move : movesCounter.keySet()) {
			if (movesCounter.get(move).intValue() > 0 && trigger == false) {
				trigger = true;
				leafMove = move;
			} else if (movesCounter.get(move).intValue() > 0 && trigger == true) {
				allSameClass = false;
				break;
			}
		}
		if (allSameClass == true) {
			n.isLeaf = true;
			n.move = MOVE.valueOf(leafMove);
			return n;
		} else if (attributeList.isEmpty()) {
//			3. Otherwise, if the attribute list is empty, return N as a leaf node labeled with the majority class in D.
			String majorityClass = "";
			int maxValue = Integer.MIN_VALUE;
			for (String move : movesCounter.keySet()) {
				if (movesCounter.get(move).intValue() > maxValue) {
					majorityClass = move;
				}
			}
			n.isLeaf = true;
			n.move = MOVE.valueOf(majorityClass);
			return n;
		} else {
//			4. Otherwise:
//			1. Call the attribute selection method on D and the attribute list, in order to choose the current attribute A: S(D, attribute list) -> A.
//			2. Label N as A and remove A from the attribute list.
//			3. For each value aj in attribute A:
//				a) Separate all tuples in D so that attribute A takes the value aj, creating the subset Dj.
//				b) If Dj is empty, add a child node to N labeled with the majority class in D.
//				c) Otherwise, add the resulting node from calling Generate_Tree(Dj, attribute) as a child node to N.
//			4. Return N.
		}

		
		return null;
	}
	
	/*
	 * function for calculating max benefit (attribute selection method)
	 */
	private double S(DataTuple[] data) {
		return 0;
	}

	// Print visual representation of the tree in console
	public void printTree() {
		System.out.println("A tree");
	}
	
	

	

	
	
	private class Attribute<T> {
		public String label;		// numOfPillsLeft, isBlinkyEdible, inkyDist
		public T value;				// int, boolean, MOVE, DiscreteTag
	}
	
	private class Node<T> {
		public Attribute value;
//		TreeMap<>;
		public LinkedList<Node<T>> children = new LinkedList<>();
		public boolean isLeaf; // Class/Label (MOVE)
		public MOVE move;
		
		public Node(Attribute value) {
	        this.value = value;
	    }
	}

}
