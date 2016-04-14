package player;

import scotlandyard.*;
import graph.*;

import java.io.IOException;
import java.util.*;

public class Simulator extends ScotlandYardGraph {
    private ScotlandYardView view;
    private String graphFilename;
    private List<Colour> players;

    public Simulator(ScotlandYardView view, String graphFilename) {
        this.view = view;
        this.graphFilename = graphFilename;
        this.players = view.getPlayers();
    }

    private int setDestination(MoveTicket move) {
		return move.target;
	}

	private int setDestination(MoveDouble move) {
		return move.move2.target;
	}

	public Move getMove(int location, List<Move> moves) {
		System.out.println("Getting intelligent move");
		/*for each accesible node in moves compute score and choose best*/
		int destination = 0;
		int bestScore = -10;
		int destinationScore = 0;
		Move bestMove = moves.get(0);

		//List<Transport> allTransports = new ArrayList<Transport>();
		// 0 Taxi, 1 Bus, 2 Underground

		/*int[] allTransports = new int[3];
		for (Move m : moves) {
			if (m instanceof MoveTicket) {
                destination = setDestination((MoveTicket) m);
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
                    default:
                        break;
    			}
            } else if (m instanceof MoveDouble) {
                destination = setDestination((MoveDouble) m);
                MoveDouble move = (MoveDouble) m;
            }
			else continue;

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
        */
	    for (Move m : moves) {
			MoveTicket move = MoveTicket.instance(Colour.Black, Ticket.Bus, 69);//random init
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
				// if (move.ticket == bestTicket) {
				// 	bestMove = m;
				// }
			}
		}
		return bestMove;
	}

	// get distance to closest detective; from detective to mr X.
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
