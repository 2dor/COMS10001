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
		// System.out.println("Getting intelligent move");
		// /*for each accesible node in moves compute score and choose best*/
		// int destination = 0;
		// int bestScore = -10;
		// int destinationScore = 0;
		Move bestMove = getMove(location, moves);
		// for (Move m : moves) {
		// 	if (m instanceof MoveTicket) destination = setDestination((MoveTicket) m);
		// 	else destination = setDestination((MoveDouble) m);
		// 	destinationScore = score(destination);
		// 	if (bestScore < destinationScore) {
		// 		bestScore = destinationScore;
		// 		bestMove = m;
		// 	}
		// }
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

	private Move getMove(int location, List<Move> moves) {
		System.out.println("Getting intelligent move");
		/*for each accesible node in moves compute score and choose best*/
		int destination = 0;
		int bestScore = -10;
		int destinationScore = 0;
		Move bestMove = moves.get(0);

		//List<Transport> allTransports = new ArrayList<Transport>();
		// 0 Taxi, 1 Bus, 2 Underground

		int[] allTransports = new int[3];
		for (Move m : moves) {
			if (m instanceof MoveTicket) destination = setDestination((MoveTicket) m);
			else continue;
			MoveTicket move = (MoveTicket) m;
			switch (move.ticket) {
				case Taxi:
					allTransports[0]++;
					break;
				case Bus:
					allTransports[1]++;
					break;
				case Underground:
					allTransports[2]++;
					break;
			}
		}
		Ticket bestTicket = Ticket.Taxi;
		int indexTransport = 0;
		if (allTransports[0] < allTransports[1]) {
			indexTransport = 1;
			bestTicket = Ticket.Bus;
		}
		if (allTransports[indexTransport] < allTransports[2]) {
			indexTransport = 2;
			bestTicket = Ticket.Underground;
		}

		for (Move m : moves) {
			MoveTicket move = MoveTicket.instance(Colour.Black, Ticket.Bus, 69);
			if (m instanceof MoveTicket) {
				destination = setDestination((MoveTicket) m);
				move = (MoveTicket) m;
			}
			else destination = setDestination((MoveDouble) m);
			destinationScore = getNodeRank(destination);
			if (bestScore < destinationScore) {
				bestScore = destinationScore;
				bestMove = m;
			} else if (bestScore == destinationScore) {
				if (move.ticket == bestTicket) {
					bestMove = m;
				}
			}
		}
		return bestMove;
	}

	// get distance to closest detective.
	private int getNodeRank(int location) {
		Dijkstra dijkstra = new Dijkstra(graphFilename);
		List<Integer> route = new ArrayList<Integer>();
		int score = 6660000;
		for (Colour p : players){
			if (p == Colour.Black) continue;
			Map<Transport, Integer> tickets = new HashMap<Transport, Integer>();
			tickets.put(Transport.Bus, view.getPlayerTickets(p, Ticket.fromTransport(Transport.Bus)));
			tickets.put(Transport.Taxi, view.getPlayerTickets(p, Ticket.fromTransport(Transport.Taxi)));
			tickets.put(Transport.Underground, view.getPlayerTickets(p, Ticket.fromTransport(Transport.Underground)));
			// System.out.println("Detective Location: " + view.getPlayerLocation(p));
			// System.out.println("Destination: " + location);
			// System.out.println("Tickets Bus: " + tickets.get(Transport.Bus));
			// System.out.println("Tickets Taxi: " + tickets.get(Transport.Taxi));
			// System.out.println("Tickets UG: " + tickets.get(Transport.Underground));
			// System.out.println("");
			route = dijkstra.getRoute(view.getPlayerLocation(p), location, tickets, p);
			score = Math.min(score, route.size() * 10);
		}
		return score;
	}

}
