package player;

import scotlandyard.*;

import java.util.*;
import java.io.IOException;

/**
 * The AIPlayer is a means of communication between the server and the Simulator,
 * which returns and intelligent move. In this class, we initialize the the
 * simulator and use its minimax() method to get the best move.
 */
public class AIPlayer implements Player, Spectator {
	private ScotlandYardView view;
	private List<Colour> players;
	private List<Boolean> rounds;
	private int currentRound;
    private Simulator simulator;
    private Colour player;

    /**
     * Constructs a new AIPlayer object which implements the Player and Spectator.
     * interfaces.
     *
     * @param view the ScotlandYardView that provides game information.
     * @param graphFilename the name of the file containing the graph components.
     * @param player the colour of this AI player
     * @param distances two dimensional array containing distances from each node to every other node on the graph.
     * @param generatedMoves two dimensional array storing generated moves at each level of the minimax.
     * @param onlyTaxiLinks an array indexed by graph nodes containing 1 if the node has just taxi links and 0 otherwise.
     */
    public AIPlayer(ScotlandYardView view,
                    String graphFilename,
                    Colour player,
                    int distances[][],
                    int generatedMoves[][],
                    int onlyTaxiLinks[]) {
		this.view = view;
		this.players = view.getPlayers();
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
        ScotlandYardGraph graph = makeGraph(graphFilename);
        MapQueue<Integer,Token> queue = new ScotlandYardMapQueue<Integer,Token>();
        simulator = new Simulator(5, this.rounds, graph, queue, 42);
        simulator.setSimulator(view, graphFilename, distances, generatedMoves, onlyTaxiLinks);
        this.player = player;
    }

    /**
     * Initialises the simulator.
     *
     * @param graphFilename the name of the file containing the graph components.
     * @return a ScotlandYardGraph generated from a local file.
     */
    private ScotlandYardGraph makeGraph(String graphFilename) {
        ScotlandYardGraphReader graphRead = new ScotlandYardGraphReader();
        ScotlandYardGraph graph = new ScotlandYardGraph();
        try {
            graph = graphRead.readGraph(graphFilename);
        } catch(IOException e) {
            System.out.println("Error creating ScotlandYardGraph from file.");
        }
        return graph;
    }

    /**
     * Used to call the minimax to get the best move and then send it to the server.
     */
    @Override
    public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
        if (view.getRound() == 0) {
            simulator.setLocations();
        }
        int[] currentConfigurationScore = new int[1];
        currentConfigurationScore[0] = 0;
        int level = 0;
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
        Move bestMove = simulator.decodeMove(move);
        receiver.playMove(bestMove, token);
    }

    @Override
    public void notify(Move move) {
        simulator.receiveMove(move);
    }

}
