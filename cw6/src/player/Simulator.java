package player;

import scotlandyard.*;
import graph.*;

import java.io.IOException;
import java.util.*;

public class Simulator extends ScotlandYard {
    private ScotlandYardView view;
    private String graphFilename;
    private Set<Integer> mrXPossibleLocations;
    private List<Boolean> rounds;
    private int currentRound;
    private final static Colour[] playerColours = {
        Colour.Black,
        Colour.Blue,
        Colour.Green,
        Colour.Red,
        Colour.White,
        Colour.Yellow
    };
    public final static Ticket[] ticketType = {
        Ticket.Taxi,
        Ticket.Bus,
        Ticket.Underground,
        Ticket.Double,
        Ticket.Secret
    };
    public final static int[] mrXTicketNumbers = {
        4,
        3,
        3,
        2,
        5
    };
    public final static int[] detectiveTicketNumbers = {
        11,
        8,
        4,
        0,
        0
    };
    public Simulator(Integer numberOfDetectives,
                    List<Boolean> rounds,
                    ScotlandYardGraph graph,
                    MapQueue<Integer, Token> queue,
                    Integer gameId) {
        super(numberOfDetectives, rounds, graph, queue, gameId);
    }

    public void setSimulator(ScotlandYardView view, String graphFilename) {
        for (Colour c : playerColours){
            System.out.println(c);
        }
        this.view = view;
        this.graphFilename = graphFilename;
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
        this.mrXPossibleLocations = new HashSet<Integer>();

        for (Colour p : playerColours) {
            Map<Ticket, Integer> tickets = new HashMap<Ticket, Integer>();
            for(int i = 0; i < ticketType.length; ++i)
            if (p == Colour.Black) {
                tickets.put(ticketType[i], mrXTicketNumbers[i]);
            } else {
                tickets.put(ticketType[i], detectiveTicketNumbers[i]);
            }
        }
        this.currentPlayer = getPlayer(Colour.Black);
    }

    private int setDestination(MoveTicket move) {
		return move.target;
	}

	private int setDestination(MoveDouble move) {
		return move.move2.target;
	}

    private void previousPlayer() {
        int newIndex = 0;
        for (int i = 0; i < noOfPlayers; ++i) {
            if (playerColours[i] == currentPlayer.getColour()) {
                newIndex = ((i - 1) + noOfPlayers) % noOfPlayers;
                break;
            }
        }
        currentPlayer = getPlayer(playerColours[newIndex]);
    }

    private void reversePlay(MoveTicket move, int previousLocation) {
        currentPlayer.setLocation(previousLocation);
        currentPlayer.addTicket(move.ticket);
        if (currentPlayer.getColour() == Colour.Black) {
            --round;
        } else {
            getPlayer(Colour.Black).removeTicket(move.ticket);
        }
    }

    private void reversePlay(MoveDouble move, int previousLocation) {
        reversePlay(move.move2, move.move1.target);
        reversePlay(move.move1, previousLocation);
        getPlayer(Colour.Black).addTicket(Ticket.Double);
    }

    private void reversePlay(Move move, int previousLocation) {
        if (move instanceof MoveTicket) {
            reversePlay((MoveTicket) move, previousLocation);
        } else if (move instanceof MoveDouble) {
            reversePlay((MoveDouble) move, previousLocation);
        } else if (move instanceof MovePass) {
            // We do not need to do anything in here
        }
    }
    public Move minimax(Colour player, int location, int level, int currentConfigurationScore) {
        List<Move> moves = validMoves(player);
        Integer bestScore = 0;
        if (player == Colour.Black) {
            bestScore = Integer.MIN_VALUE;
            currentConfigurationScore = getNodeRank(location);
        } else {
            bestScore = Integer.MAX_VALUE;
            currentConfigurationScore = getDetectiveScore(location, validMoves(player));
        }
        if (level == 5) return moves.get(0);
        Move bestMove = moves.get(0);
        Integer nextScore = 0;
        for (Move move : moves) {
            play(move);//TODO: manually implement this method
            nextPlayer();
            minimax(currentPlayer.getColour(), currentPlayer.getLocation(), level + 1, nextScore);
            if (player == Colour.Black) {
                if (bestScore < nextScore) {
                    bestScore = nextScore;
                    bestMove = move;
                }
            } else {
                if (bestScore > nextScore) {
                    bestScore = nextScore;
                    bestMove = move;
                }
            }
            previousPlayer();
            reversePlay(move, location);
        }
        return bestMove;
    }

	public Move getMrXMove(int location, List<Move> moves) {
		System.out.println("Getting intelligent move");
		/*for each accesible node in moves compute score and choose best*/
		int destination = 0;
		int bestScore = -10;
		int destinationScore = 0;
		Move bestMove = moves.get(0);
        int currentNodeRank = getNodeRank(location);
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
	    for (Move currentMove : moves) {
			MoveTicket move = MoveTicket.instance(Colour.Black, Ticket.Bus, 69);//random init
			if (currentMove instanceof MoveTicket) {
				destination = setDestination((MoveTicket) currentMove);
				move = (MoveTicket) currentMove;
			} else {
                destination = setDestination((MoveDouble) currentMove);
            }
            if (currentNodeRank > 4) {
                List<Move> mrXMoves = graph.generateMoves(Colour.Black, location);
                List<Move> possible = new ArrayList<Move>();
                for (Move a : mrXMoves) {
                    destinationScore = getNodeRank(destination);
                    if (a instanceof MoveTicket) {
                        MoveTicket m = (MoveTicket) a;
                        if (m.ticket == Ticket.Taxi && destinationScore < currentNodeRank) {
                            possible.add(m);
                        }
                        //newLocations.add(move.target);
                        //System.out.println(move.target);
                    }
                }
                if (possible.size() > 0) {
                    bestMove = possible.get(0);
                } else {
                    bestMove = moves.get(0);
                }
            } else {
        		destinationScore = getNodeRank(destination);
        		if (bestScore < destinationScore) {
        			bestScore = destinationScore;
        			bestMove = currentMove;
        		} else if (bestScore == destinationScore) {
        			// if (move.ticket == bestTicket) {
        			// 	bestMove = m;
        			// }
        		}
            }
		}
		return bestMove;
	}


    // get distance to closest detective; from detective to mr X.
	private int getNodeRank(int location) {
		Dijkstra dijkstra = new Dijkstra(graphFilename);
		List<Integer> route = new ArrayList<Integer>();
		int score = 666013;
		for (Colour p : playerColours){
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
			score = Math.min(score, route.size());
		}
		return score;
	}

    public int getDetectiveScore(int location, List<Move> moves) {
        currentRound = view.getRound();//update the round every time
        if (currentRound > 2) {
            //System.out.println("\nMr X location revealed and updated.");
            mrXLocation = view.getPlayerLocation(Colour.Black);
        }
        if (rounds.get(currentRound) == true) {
            // clearing global list of Mr X possible locations
            mrXPossibleLocations.clear();
            mrXPossibleLocations.add(mrXLocation);
            //System.out.println("\nPOSSIBLE LOCATIONS RESTARTED");
        } else {
            //System.out.println("\nTrying to expand Mr X list of locations.");
            Set<Integer> newLocations = new HashSet<Integer>();
            for (Integer loc : mrXPossibleLocations) {
                //System.out.println("\nNODES FROM:" + loc);
                List<Move> mrXMoves = graph.generateMoves(Colour.Black, loc);
                for (Move a : mrXMoves) {
                    if (a instanceof MoveTicket) {
                        MoveTicket move = (MoveTicket) a;
                        newLocations.add(move.target);
                        //System.out.println(move.target);
                    }
                    else if (a instanceof MoveDouble) {
                        MoveDouble move = (MoveDouble) a;
                        newLocations.add(move.move2.target);
                        //System.out.println(move.move2.target);
                    }
                }
            }
            for (Integer i : newLocations) {
                mrXPossibleLocations.add(i);
            }
            // System.out.println("\nPRINTING POSSIBLE LOCATIONS:");
            // for (Integer a : mrXPossibleLocations) {
            //     System.out.println(a);
            // }
        }
        return mrXPossibleLocations.size();
    }

}
