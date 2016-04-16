package player;

import scotlandyard.*;

import java.util.*;
import java.io.IOException;

/**
 * The RandomPlayer class is an example of a very simple AI that
 * makes a random move from the given set of moves. Since the
 * RandomPlayer implements Player, the only required method is
 * notify(), which takes the location of the player and the
 * list of valid moves. The return value is the desired move,
 * which must be one from the list.
 */
public class AIPlayer implements Player {
	private ScotlandYardView view;
	private List<Colour> players;
	private List<Boolean> rounds;
	private int currentRound;
	private String graphFilename;
    private Simulator simulator;
    private Colour player;

    public AIPlayer(ScotlandYardView view, String graphFilename, Colour player) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
		this.view = view;
		this.players = view.getPlayers();
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
		this.graphFilename = graphFilename;
        simulator = new Simulator(view, graphFilename);
        //simulator = new ScotlandYardGraph();
        // ScotlandYardGraphReader graphRead = new ScotlandYardGraphReader();
        // try {
        //     simulator = (Simulator) graphRead.readGraph(graphFilename);
        // } catch(IOException e) {}
        // simulator.setSimulator(view, graphFilename);
        this.player = player;
    }

    @Override
    public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
        //TODO: Some clever AI here ...
		// System.out.println("Getting intelligent move");
		// /*for each accesible node in moves compute score and choose best*/
		// int destination = 0;
		// int bestScore = -10;
		// int destinationScore = 0;

        //make changes to the Simulator, send view to Simulator
        //ask simulator for the next move

		//Move bestMove = getMove(location, moves);




        // for (Move m : moves) {
		// 	if (m instanceof MoveTicket) destination = setDestination((MoveTicket) m);
		// 	else destination = setDestination((MoveDouble) m);
		// 	destinationScore = score(destination);
		// 	if (bestScore < destinationScore) {
		// 		bestScore = destinationScore;
		// 		bestMove = m;
		// 	}
		// }
        // Collections.shuffle(moves);
        // Move bestMove = moves.get(0);
        Move bestMove = simulator.getMrXMove(location, moves);
        System.out.println("Playing intelligent move" + bestMove);
        receiver.playMove(bestMove, token);
    }

    // @Override
    // public void notify(Move move) {
    //     if () {
    //
    //     }
    // }

}
