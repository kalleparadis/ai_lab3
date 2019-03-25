package pacman.entries.pacman;

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
	// private MOVE myMove=MOVE.NEUTRAL;
	private DecisionTree dt;
	
	public MyPacMan() {
		super();
		dt = new DecisionTree(DataSaverLoader.LoadPacManData());
		dt.printTree();
	}
	
	public MOVE getMove(Game game, long timeDue) 
	{
		//Place your game logic here to play the game as Ms Pac-Man
		
		//Current state
		DataTuple data = new DataTuple(game, game.getPacmanLastMoveMade());
		
		// Send attributes of Current state to DT and receive move
		return dt.predictMove(data);
	}
}