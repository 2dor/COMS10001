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
public class AIPlayer implements Player, Spectator {
	private ScotlandYardView view;
	private List<Colour> players;
	private List<Boolean> rounds;
	private int currentRound;
    private Simulator simulator;
    private Colour player;

    public AIPlayer(ScotlandYardView view,
                    String graphFilename,
                    Colour player,
                    int distances[][],
                    int generatedMoves[][]) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
		this.view = view;
		this.players = view.getPlayers();
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
        ScotlandYardGraph graph = makeGraph(graphFilename);
        MapQueue<Integer,Token> queue = new ScotlandYardMapQueue<Integer,Token>();
        simulator = new Simulator(5, this.rounds, graph, queue, 42);
        simulator.setSimulator(view, graphFilename, distances, generatedMoves);
        this.player = player;
    }

    private ScotlandYardGraph makeGraph(String graphFilename) {
        ScotlandYardGraphReader graphRead = new ScotlandYardGraphReader();
        ScotlandYardGraph graph = new ScotlandYardGraph();
        try {
            graph = graphRead.readGraph(graphFilename);
        } catch(IOException e) {
            //TODO maybe: handle exception
        }
        return graph;
    }

    @Override
    public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
        //TODO: Some clever AI here ...
		// System.out.println("Getting intelligent move");
        int[] currentConfigurationScore = new int[1];
        currentConfigurationScore[0] = 0;
        int level = 0;
        System.out.println("\nConfiguration before");
        System.out.println(currentConfigurationScore[0]);
        for (Integer i : simulator.mrXPossibleLocations) {
            System.out.print(i + " ");
        }
        System.out.println("");
        int bestScore = 0;
        /* We set these values because the first ply is a special case where
         * you should NOT be allowed to prune. Therefore, you can only prune
         * after this first ply.
         */
        if (player == Colour.Black)
            bestScore = Integer.MAX_VALUE;
        else
            bestScore = Integer.MIN_VALUE;
        int move = simulator.minimax(Colour.Black, location, level, currentConfigurationScore, bestScore, simulator.mrXPossibleLocations);
        System.out.println("Printing locations after minimax:");
        for (Integer loc : simulator.mrXPossibleLocations) {
            System.out.print(loc + ", ");
        }
        Move bestMove = simulator.decodeMove(move);
        System.out.println("\nConfiguration score after");
        System.out.println(currentConfigurationScore[0]);
        // Collections.shuffle(moves);
        // Move bestMove = moves.get(0);
        System.out.println("Playing intelligent move" + bestMove);
        receiver.playMove(bestMove, token);
    }

    @Override
    public void notify(Move move) {
        //System.out.println("I get the move!");
        simulator.sendMove(move);
    }

}
