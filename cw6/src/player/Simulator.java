package player;

import scotlandyard.*;
import graph.*;

import java.io.IOException;
import java.util.*;

public class Simulator extends ScotlandYard {
    private ScotlandYardView view;
    private String graphFilename;
    public HashSet<Integer> mrXPossibleLocations;

    private List<Boolean> rounds;
    private int currentRound;
    private int distancesByTickets[][][][][];
    private int simulatedMoves;
    private boolean occupiedNodes[];

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

    public void setSimulator(ScotlandYardView view,
                             String graphFilename,
                             int distancesByTickets[][][][][]) {
        System.out.println("\nSetting simulator\n");
        for (Colour c : playerColours){
            System.out.println(c);
        }
        simulatedMoves = 0;
        this.view = view;
        this.graphFilename = graphFilename;
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
        this.mrXPossibleLocations = new HashSet<Integer>();
        this.mrXPossibleLocations.add(35);
        this.distancesByTickets = distancesByTickets;
        this.occupiedNodes = new boolean[201];
        System.out.println("\nPRINTING SOME DISTANCE");
        System.out.println(this.distancesByTickets[1][2][3][2][1]);

        for (Colour p : playerColours) {
            Map<Ticket, Integer> tickets = new HashMap<Ticket, Integer>();
            for(int i = 0; i < ticketType.length; ++i) {
                if (p == Colour.Black) {
                    tickets.put(ticketType[i], mrXTicketNumbers[i]);
                } else {
                    tickets.put(ticketType[i], detectiveTicketNumbers[i]);
                }
            }
            // let's assume that we're setting only Black as an AI for now
            // because we can't access other players' locations then, since they
            // are not in the list of players in the view
            if (p == Colour.Black) {
                join(new SimulatedPlayer(), p, 0, tickets);
            } else {
                join(new SimulatedPlayer(), p, view.getPlayerLocation(p), tickets);
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
        occupiedNodes[move.target] = false;
        if (currentPlayer.getColour() == Colour.Black) {
            --currentRound;
        } else {
            getPlayer(Colour.Black).removeTicket(move.ticket);
            occupiedNodes[previousLocation] = true;
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

    @Override
    protected void play(Move move) {
        if (move instanceof MoveTicket) play((MoveTicket) move);
        else if (move instanceof MoveDouble) play((MoveDouble) move);
        else if (move instanceof MovePass) play((MovePass) move);
    }

    @Override
    protected void play(MoveTicket move) {
        Colour colour = move.colour;
		Ticket ticket = move.ticket;
		int target = move.target;
		PlayerData player = getPlayer(colour);
		player.removeTicket(ticket);
        occupiedNodes[player.getLocation()] = false;
		player.setLocation(target);
        //if (currentPlayer != Colour.Black || (rounds.get(currentRound) && ticket != Ticket.Secret))
        //Give ticket to mrX
		if (colour != Colour.Black) {
			PlayerData mrX = getPlayer(Colour.Black);
			mrX.addTicket(ticket);
            occupiedNodes[target] = true;
		} else {
			currentRound++;
		}
        notifySpectators(move);
    }

    @Override
    protected void play(MoveDouble move) {
        notifySpectators(move);
		play(move.move1);
		play(move.move2);
        PlayerData mrX = getPlayer(move.colour);
        mrX.removeTicket(Ticket.Double);
    }

    /**
     * Plays a MovePass.
     *
     * @param move the MovePass to play.
     */
    @Override
    protected void play(MovePass move) {
        notifySpectators(move);
    }

    public void sendMove(Move move) {
        if (move instanceof MovePass) {
            return;
        } else {
            System.out.println("I am updating now!");
            updatePossibleLocations(move, this.mrXPossibleLocations);
            play(move);
        }
    }

    private void notifySpectators(Move move) {

    }

    // TEST IF CONFIGURATION SCORES ARE COMPUTED CORRECTLY
    public Move minimax(Colour player,
                        int location,
                        int level,
                        int[] currentConfigurationScore,
                        HashSet<Integer> mrXOldLocations) {
        // update Mr X's position, since this is the only time we have access to it.
        // ++simulatedMoves;
        // System.out.println("Simulated moves: " + simulatedMoves);

        if (player == Colour.Black) {
            //mrXLocation = location;
            getPlayer(Colour.Black).setLocation(location);
        }
        if (isGameOver()) {
            if (getWinningPlayers().size() == 1) {
                currentConfigurationScore[0] = 100;// Mr.X won
            } else {
                currentConfigurationScore[0] = 0;// detectives won
            }
            return MoveTicket.instance(player, Ticket.Taxi, 69);
            //int doesn't even matter
        }
        // System.out.println("\nAfter calling minimax, we have players: ");
        // for (PlayerData p : players) {
        //     System.out.println("\n");
        //     System.out.println(p.getColour());
        //     System.out.println(p.getLocation());
        // }
        if (level == 0) {
            for (PlayerData p : players) {
                System.out.println("player: " + p.getColour() + " location: " + p.getLocation());
            }
        }
        List<Move> moves = validMoves(player);
        Integer bestScore = 0;
        if ((level == 5) || (currentRound == 22 && player == Colour.Black)) {
            if (player == Colour.Black) {
                currentConfigurationScore[0] = getNodeRank(location);
                // System.out.println("\nConfiguration updated");
                // System.out.println(currentConfigurationScore[0]);
            } else {
                currentConfigurationScore[0] = mrXPossibleLocations.size();
                //getDetectiveScore(location, validMoves(player));
            }
            return MoveTicket.instance(player, Ticket.Taxi, 69);
        }
        Move bestMove = MoveTicket.instance(player, Ticket.Taxi, 69);
        int[] nextScore = new int[1];
        nextScore[0] = 0;
        if (player == Colour.Black)
            bestScore = Integer.MIN_VALUE;
        else
            bestScore = Integer.MAX_VALUE;
        //System.out.println("\nbest score before: " + bestScore);
        HashSet<Integer> mrXNewLocations = new HashSet<Integer>();
        for (Move move : moves) {
            if (move instanceof MoveDouble) continue;
            if (move instanceof MovePass) continue;
            MoveTicket currentMove = (MoveTicket) move;

            if (currentMove.ticket == Ticket.Secret) continue;

            mrXNewLocations.clear();
            mrXNewLocations.addAll(mrXOldLocations);
            updatePossibleLocations(move, mrXNewLocations);
            play(move);
            //System.out.println("Moving to " + currentMove.target);
            nextPlayer();
            /*************************************/
            minimax(currentPlayer.getColour(),
                    currentPlayer.getLocation(),
                    level + 1,
                    nextScore,
                    mrXNewLocations);
            /*************************************/
            if (player == Colour.Black) {
                if (bestScore < nextScore[0]) {
                    // System.out.println("\nUpdating best move.");
                    // System.out.println("bestScore: " + bestScore + " nextScore: " + nextScore[0]);
                    // System.out.println(currentMove.target);
                    bestScore = nextScore[0];
                    bestMove = move;
                }
            } else {
                if (bestScore > nextScore[0]) {
                    bestScore = nextScore[0];
                    bestMove = move;
                }
            }
            previousPlayer();
            reversePlay(move, location);
        }
        currentConfigurationScore[0] = bestScore;
        //System.out.println("\nbest score after: " + bestScore);
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
		// Dijkstra dijkstra = new Dijkstra(graphFilename);
		// List<Integer> route = new ArrayList<Integer>();
		int bestScore = 666013;
        int score = 0;
		for (Colour p : playerColours){
			if (p == Colour.Black) continue;
			// Map<Transport, Integer> tickets = new HashMap<Transport, Integer>();
			// tickets.put(Transport.Bus, getPlayerTickets(p, Ticket.fromTransport(Transport.Bus)));
			// tickets.put(Transport.Taxi, getPlayerTickets(p, Ticket.fromTransport(Transport.Taxi)));
			// tickets.put(Transport.Underground, getPlayerTickets(p, Ticket.fromTransport(Transport.Underground)));
			// System.out.println("Detective Location: " + view.getPlayerLocation(p));
			// System.out.println("Destination: " + location);
			// System.out.println("Tickets Bus: " + tickets.get(Transport.Bus));
			// System.out.println("Tickets Taxi: " + tickets.get(Transport.Taxi));
			// System.out.println("Tickets UG: " + tickets.get(Transport.Underground));
			// System.out.println("");

            //route = dijkstra.getRoute(getPlayerLocation(p), location, tickets, p);
            score = distancesByTickets[getPlayerLocation(p)]
                                      [location]
                                      [getPlayerTickets(p, Ticket.fromTransport(Transport.Taxi))]
                                      [getPlayerTickets(p, Ticket.fromTransport(Transport.Bus))]
                                      [getPlayerTickets(p, Ticket.fromTransport(Transport.Underground))];
            bestScore = Math.min(bestScore, score);
		}
		return bestScore;
	}

    private void filterMoves(List<Move> possibleMoves, MoveTicket moveSingle, MoveTicket moveMade, HashSet<Integer> locations) {
        if (moveSingle.ticket == moveMade.ticket && !occupiedNodes[moveSingle.target]){
            locations.add(moveSingle.target);
        }
    }

    private void updatePossibleLocations(Move move, HashSet<Integer> locations) {
        if (move instanceof MoveTicket) updatePossibleLocations((MoveTicket) move, locations);
        else if (move instanceof MoveDouble) updatePossibleLocations((MoveDouble) move, locations);
    }

    private void updatePossibleLocations(MoveTicket moveMade, HashSet<Integer> locations) {
        if (moveMade.colour == Colour.Black) {
            System.out.println("I am black!");
            List<Move> possibleMoves = new ArrayList<Move>();
            HashSet<Integer> newLocations = new HashSet<Integer>(locations);
            System.out.println("I have " + locations.size() + "locations.");
            for (Integer location : locations) {
                possibleMoves = graph.generateMoves(Colour.Black, location);
                for (Move m : possibleMoves) {
                    if (m instanceof MoveTicket) {
                        MoveTicket moveSingle = (MoveTicket) m;
                        filterMoves(possibleMoves, moveSingle, moveMade, newLocations);
                    } else if (m instanceof MoveDouble) {
                        MoveDouble moveDouble = (MoveDouble) m;
                        MoveTicket moveSingle = moveDouble.move1;
                        filterMoves(possibleMoves, moveSingle, moveMade, newLocations);
                        moveSingle = moveDouble.move2;
                        filterMoves(possibleMoves, moveSingle, moveMade, newLocations);
                    }
                }
            }
            locations = newLocations;
            System.out.println("Now I have " + locations.size() + "locations.");
        } else {
            if (locations.contains(moveMade.target)) {
                locations.remove(moveMade.target);
            }
        }
    }

    protected void updatePossibleLocations(MoveDouble move, HashSet<Integer> locations) {
        updatePossibleLocations(move.move1, locations);
        updatePossibleLocations(move.move2, locations);
    }


    /*public int getDetectiveScore(int location, List<Move> moves) {
        currentRound = view.getRound();//update the round every time
        if (currentRound > 2) {
            //System.out.println("\nMr X location revealed and updated.");
            mrXLocation = getPlayer(Colour.Black).getLocation();
        } else {
            return 18;//number of initial locations
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
    }*/

}
