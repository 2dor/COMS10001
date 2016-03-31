package player;

import scotlandyard.*;

import java.util.*;

/**
 * The RandomPlayer class is an example of a very simple AI that
 * makes a random move from the given set of moves. Since the
 * RandomPlayer implements Player, the only required method is
 * notify(), which takes the location of the player and the
 * list of valid moves. The return value is the desired move,
 * which must be one from the list.
 */
public class XPlayer implements Player {

    public XPlayer(ScotlandYardView view, String graphFilename) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
    }

    @Override
    public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
        //TODO: Some clever AI here ...
        System.out.println("Getting intelligent move");
		/*for each accesible node in moves compute score and choose best*/
        //Collections.shuffle(moves);
        System.out.println("Playing intelligent move" + moves.get(0));
        receiver.playMove(moves.get(0), token);
    }

	private int score(Node<Integer> node) {
		for (all colours){
			Dijkstra.getRoute(node.getIndex(),that coulor current location, that coulour tickets);
			scotlandyard.ScotlandYard.getPlayerLocation(colour);
			update minimum;
		}
		return minimum;
	}
}
