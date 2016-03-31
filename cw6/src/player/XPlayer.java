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
	private ScotlandYardView view;
	private List<Colour> players;
	private List<Boolean> rounds;
	private int currentRound;
	private String graphFilename;

    public XPlayer(ScotlandYardView view, String graphFilename) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
		this.view = view;
		this.players = view.getPlayers();
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
		this.graphFilename = graphFilename;
    }

    @Override
    public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
        //TODO: Some clever AI here ...
		System.out.println("Getting intelligent move");
		/*for each accesible node in moves compute score and choose best*/
		int destination = 0;
		int bestScore = -10;
		int destinationScore = 0;
		Move bestMove = moves.get(0);
		for (Move m : moves) {
			if (m instanceof MoveTicket) destination = setDestination((MoveTicket) m);
			else destination = setDestination((MoveDouble) m);
			destinationScore = score(destination);
			if (bestScore < destinationScore) {
				bestScore = destinationScore;
				bestMove = m;
			}
		}
        //Collections.shuffle(moves);
        System.out.println("Playing intelligent move" + bestMove);
        receiver.playMove(bestMove, token);
    }

	private int setDestination(MoveTicket move) {
		return move.target;
	}

	private int setDestination(MoveDouble move) {
		return move.move2.target;
	}

	private int score(int location) {
		Dijkstra dijkstra = new Dijkstra(graphFilename);
		List<Integer> route = new ArrayList<Integer>();
		int score = 6660000;
		for (Colour p : players){
			Map<Transport, Integer> tickets = new HashMap<Transport, Integer>();
			tickets.put(Transport.Bus, view.getPlayerTickets(p, Ticket.fromTransport(Transport.Bus)));
			tickets.put(Transport.Taxi, view.getPlayerTickets(p, Ticket.fromTransport(Transport.Taxi)));
			tickets.put(Transport.Underground, view.getPlayerTickets(p, Ticket.fromTransport(Transport.Underground)));
			route = dijkstra.getRoute(location, view.getPlayerLocation(p), tickets);
			score = Math.min(score, route.size() * 10);
		}
		return score;
	}
}
