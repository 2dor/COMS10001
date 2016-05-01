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
    private int distances[][];
    private int simulatedMoves;
    private boolean occupiedNodes[];
	private int[][] generatedMoves;
	private int[][] movesValid;
    private int[] onlyTaxiLinks;

    private static Ticket TAXI = Ticket.fromTransport(Transport.Taxi);
    private static Ticket BUS = Ticket.fromTransport(Transport.Bus);
    private static Ticket UG = Ticket.fromTransport(Transport.Underground);
    private static int DUMMYMOVE = 11042;

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
        Ticket.Secret,
        Ticket.Double
    };
    public final static int[] mrXTicketNumbers = {
        4,
        3,
        3,
        5,
        2
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
                             int distances[][],
                             int generatedMoves[][],
                             int onlyTaxiLinks[]) {
        System.out.println("\nSetting simulator\n");
        simulatedMoves = 0;
        this.view = view;
        this.graphFilename = graphFilename;
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
        this.mrXPossibleLocations = new HashSet<Integer>();
        this.distances = distances;
        this.generatedMoves = generatedMoves;
        this.occupiedNodes = new boolean[201];
		this.movesValid = new int[20][500];
        this.onlyTaxiLinks = onlyTaxiLinks;
        for (Colour p : playerColours) {
            // if (p != Colour.Black)
            //     System.out.println(view.getPlayerLocation(p));
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
                join(new SimulatedPlayer(), p, 0, tickets);
            }
        }
        this.currentPlayer = getPlayer(Colour.Black);
    }

    public void setLocations() {
        for (PlayerData p : players) {
            if (p.getColour() == Colour.Black) continue;
            p.setLocation(view.getPlayerLocation(p.getColour()));
            occupiedNodes[view.getPlayerLocation(p.getColour())] = true;
            System.out.println(p.getColour() + " " + p.getLocation());
        }
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

    private void reverseTicketMove(int move, int previousLocation) {
		Ticket ticket = decodeTicket(move);
		int target = decodeDestination(move);
        currentPlayer.setLocation(previousLocation);
        currentPlayer.addTicket(ticket);
        occupiedNodes[target] = false;
        if (currentPlayer.getColour() == Colour.Black) {
            --currentRound;
        } else {
            getPlayer(Colour.Black).removeTicket(ticket);
            occupiedNodes[previousLocation] = true;
        }
    }

    private void reverseDoubleMove(int move, int previousLocation) {
		int move1 = move / 100000;
		int move1Target = decodeDestination(move1);
		int move2 = move - move1 * 100000;
        reversePlay(move2, move1Target);
        reversePlay(move1, previousLocation);
        getPlayer(Colour.Black).addTicket(Ticket.Double);
    }

    private void reversePlay(int move, int previousLocation) {
        if (isMoveTicket(move)) {
            reverseTicketMove(move, previousLocation);
        } else if (isMoveDouble(move)) {
            reverseDoubleMove(move, previousLocation);
        } else if (isMovePass(move)) {
            return;
        }
    }

    protected void play(int move) {
        if (isMoveTicket(move)) playTicket(move);
        else if (isMoveDouble(move)) playDouble(move);
        else if (isMovePass(move)) return;
    }

    protected void playTicket(int move) {
        Colour colour = decodeColour(move);
		Ticket ticket = decodeTicket(move);
		int target = decodeDestination(move);
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
    }

    protected void playDouble(int move) {
		int move1 = move / 100000;
		int move2 = move - move1 * 100000;
		Colour colour = decodeColour(move1);
		playTicket(move1);
		playTicket(move2);
        PlayerData mrX = getPlayer(colour);
        // System.out.println("\nDOUBLE MOVE");
        // System.out.println(getPlayerTickets(Colour.Black, Ticket.Double));
        mrX.removeTicket(Ticket.Double);
        // System.out.println(getPlayerTickets(Colour.Black, Ticket.Double));
    }

    public void sendMove(Move moveMade) {
		int move = encodeMove(moveMade);
        System.out.println("\nI am updating now!");
        System.out.println("Printing old locations:");
        for (Integer loc : this.mrXPossibleLocations) {
            System.out.print(loc + ", ");
        }
        System.out.println("\n");
        updatePossibleLocations(move, this.mrXPossibleLocations);
        System.out.println("Printing updated locations:");
        for (Integer loc : this.mrXPossibleLocations) {
            System.out.print(loc + ", ");
        }
        System.out.println("\n");
        play(move);
    }

    // TEST IF CONFIGURATION SCORES ARE COMPUTED CORRECTLY
    public int minimax(Colour player,
                        int location,
                        int level,
                        int[] currentConfigurationScore,
                        int previousScore,
                        HashSet<Integer> mrXOldLocations) {
        // update Mr X's position, since this is the only time we have access to it.
        // ++simulatedMoves;
        // System.out.println("Simulated moves: " + simulatedMoves);

        if (player == Colour.Black) {
            //mrXLocation = location;
            // System.out.println("Printing mr x location");
            getPlayer(Colour.Black).setLocation(location);
            // System.out.println(getPlayer(Colour.Black).getLocation());
            if (view.getRound() == 0) {
                mrXOldLocations.add(location);
                // mrXOldLocations.add(35);
                // mrXOldLocations.add(127);
                // mrXOldLocations.add(106);
                // mrXOldLocations.add(166);
                // for (Colour p : playerColours) {
                //     if (p == Colour.Black) continue;
                //     mrXOldLocations.remove(getPlayer()
                // }
            }
        }
        // System.out.println("\nAfter calling minimax, we have players: ");
        // for (PlayerData p : players) {
        //     System.out.println("\n");
        //     System.out.println(p.getColour());
        //     System.out.println(p.getLocation());
        // }
        // if (level == 0) {
        //     for (PlayerData p : players) {
        //         System.out.println("player: " + p.getColour() + " location: " + p.getLocation());
        //     }
        // }
        //System.out.println("Black is at: " + getPlayer(Colour.Black).getLocation());
        currentConfigurationScore[0] = getNodeRank(getPlayer(Colour.Black).getLocation()) * 100;
        // if (currentConfigurationScore[0] == 0) {
        //     //System.out.println("currentConfigurationScore " + currentConfigurationScore[0]);
        //     //System.out.println("currentConfigurationScore is zero at level " + level);
        //     //System.out.println("Black");
        // }
        /* if the minimum distance to a detective is zero, it means that Mr X
         * was caught so we can end this branch of the minimax
         */
        if (currentConfigurationScore[0] == 0) {
            // System.out.println(getPlayer(Colour.Black).getLocation());
            // System.out.println("111 Level " + level + " " + player + " " + getPlayer(Colour.Black).getLocation());
            // System.out.println(occupiedNodes[getPlayer(Colour.Black).getLocation()]);
            return DUMMYMOVE;
        }
        Integer bestScore = 0;
        if ((level == 6) || (currentRound == 24 && player == Colour.Black)) {
            currentConfigurationScore[0] += mrXOldLocations.size();
            currentConfigurationScore[0] *= 1000;
            currentConfigurationScore[0] += getDetectiveScore(mrXOldLocations);
            // System.out.print(player + " on location " + getPlayerLocation(player));
            // System.out.print("\nConfiguration updated ");
            // System.out.println(currentConfigurationScore[0]);
            //System.out.println("222 Level " + level + " " + player + " " + getPlayer(Colour.Black).getLocation());
            return DUMMYMOVE;
        }
        // System.out.println("Current player " + player + " at level " + level);
        // for (int i = 1; i <= movesValid[level][0]; ++i) {
        //     if (isMoveTicket(movesValid[level][i]))
        //         System.out.print(movesValid[level][i] + " ");
        // }
        // System.out.println("");
        int xScore = currentConfigurationScore[0];
        if (player == Colour.Black) {
            if (getRound() >= 3 && view.getRounds().get(getRound()) && mrXOldLocations.size() < 5 && xScore < 300 && onlyTaxiLinks[location] == 0) {
                validMoves(player, level, false, true);
            } else {
                validMoves(player, level, false, false);
            }
            // if (xScore >= 300) {
            //     validMoves(player, level, false, false);
            // } else if (xScore >= 200) {
            //     validMoves(player, level, false, true);
            // } else {
            //     validMoves(player, level, true, true);
            // }
        } else {
            validMoves(player, level, false, false);
        }
        int bestMove = DUMMYMOVE;
        int[] nextScore = new int[1];
        nextScore[0] = 0;
        if (player == Colour.Black)
            bestScore = Integer.MIN_VALUE;
        else
            bestScore = Integer.MAX_VALUE;
        HashSet<Integer> mrXNewLocations = new HashSet<Integer>();
        // System.out.println("Number of generated moves: " + movesValid[level][0]);
        // for (int i = 1; i <= movesValid[level][0]; ++i) {
        //     System.out.println(movesValid[level][i] + " ");
        // }
        // if (player == Colour.Black) {
        //     System.out.println("Printing moves for mr x");
        // }
        for (int i = 1; i <= movesValid[level][0]; ++i) {
            if (isMovePass(movesValid[level][i])) {
                System.out.println("\n\nIn minimax, out of moves " + player + " " + encodeColour(player));
                System.out.println("MovePass is " + movesValid[level][i]);
            }
            // if (player == Colour.Black)  {
            //     System.out.println(movesValid[level][i]);
            // }
            //if (isMoveDouble(movesValid[level][i])) continue;
            //MoveTicket currentMove = (MoveTicket) move;
            //if (isMoveTicket(movesValid[level][i]) && decodeTicket(movesValid[level][i]) == Ticket.Secret) continue;
            // System.out.print("Current bestScore " + bestScore + " ");
            // System.out.println("Current player " + player + " playing move " + movesValid[level][i] + " at level " + level);
            if (player == Colour.Black) {
                if (previousScore != Integer.MAX_VALUE && getXScore(bestScore) > getXScore(previousScore)) {
                    // System.out.println("Pruning!");
                    // System.out.println("bestScore: " + bestScore + " previousScore: " + previousScore);
                    // System.out.println("Target: " + decodeDestination(movesValid[level][i]));
                    currentConfigurationScore[0] = bestScore;
                    return bestMove;
                }
            } else {
                if (player == Colour.Blue) { // AND previous player is Black
                    if (previousScore != Integer.MIN_VALUE && getDScore(bestScore) < getDScore(previousScore)) {
                        // System.out.println("Pruning!");
                        // System.out.println("bestScore: " + bestScore + " previousScore: " + previousScore);
                        // System.out.println("Target: " + decodeDestination(movesValid[level][i - 1]));
                        currentConfigurationScore[0] = bestScore;
                        return bestMove;
                    }
                }
            }
            mrXNewLocations.clear();
            mrXNewLocations.addAll(mrXOldLocations);
            updatePossibleLocations(movesValid[level][i], mrXNewLocations);
            // System.out.println("Printing updated locations IN MINIMAX:");
            // for (Integer loc : mrXNewLocations) {
            //     System.out.print(loc + ", ");
            // }
            // System.out.println("\n");
            play(movesValid[level][i]);
            //System.out.println("Moving to " + currentMove.target);
            nextPlayer();
            /*************************************/
            minimax(currentPlayer.getColour(),
                    currentPlayer.getLocation(),
                    level + 1,
                    nextScore,
                    bestScore,
                    mrXNewLocations);

            // System.out.print("nextScore: " +nextScore[0]);
            // System.out.println("Target: " + currentMove.target);
            /*************************************/
            if (player == Colour.Black) {
                if (bestScore == Integer.MIN_VALUE || getXScore(bestScore) < getXScore(nextScore[0])) {
                    // System.out.println("bestScore: " + bestScore + " nextScore: " + nextScore[0]);
                    bestScore = nextScore[0];
                    bestMove = movesValid[level][i];
                }
            } else {
                if (bestScore == Integer.MAX_VALUE || getDScore(bestScore) > getDScore(nextScore[0])) {
                    // System.out.println("bestScore: " + bestScore + " nextScore: " + nextScore[0]);
                    bestScore = nextScore[0];
                    bestMove = movesValid[level][i];
                }
            }
            previousPlayer();
            reversePlay(movesValid[level][i], location);
        }
        // if mrx has no valid moves then set both scores to 0 meaning really bad for him
        if (bestScore == Integer.MIN_VALUE) {
            currentConfigurationScore[0] = 0;
        // if detective plays move pass then increase mrx's score by 20
        } else if (isMovePass(movesValid[level][1])) {
            currentConfigurationScore[0] = (getXScore(currentConfigurationScore[0]) + 20) * 1000
                                          + getDScore(currentConfigurationScore[0]) + 10;
        } else {
            currentConfigurationScore[0] = bestScore;
        }
        // System.out.println("\nbest move at the end of minimax: " + bestMove);
        return bestMove;
    }

    private int getXScore(int score) {
        return score / 1000;
    }

    private int getDScore(int score) {
        return score - (score / 1000) * 1000;
    }
    // get distance to closest detective; from detective to mr X.
	private int getNodeRank(int location) {
		// Dijkstra dijkstra = new Dijkstra(graphFilename);
		// List<Integer> route = new ArrayList<Integer>();
		int bestScore = 666013;
        int score = 0;
		for (Colour p : playerColours){
			if (p == Colour.Black) continue;
            score = distances[getPlayerLocation(p)][location];
            bestScore = Math.min(bestScore, score);
		}
        // if (bestScore == 0) {
        //     System.out.println("Mrx " + location);
        //     for (PlayerData p : players) {
        //         System.out.println(p.getColour() + " " + p.getLocation() + " " + getPlayerLocation(p.getColour()));
        //     }
        // }
		return bestScore;
	}

    /*
     * Computes a score for the detectives = the sum of shortest distances from each
     * detective to all possible locations
     */
    private int getDetectiveScore(HashSet<Integer> mrXPossibleLocations) {
        int detectiveLocation = 0;
        int score = 0;
        for (int i = 1; i < 6; ++i) {
            detectiveLocation = getPlayerLocation(playerColours[i]);
            // System.out.println("From detective " + playerColours[i] + " on " + detectiveLocation);
            for (Integer destination : mrXPossibleLocations) {
                int scoreAux = distances[detectiveLocation][destination];
                // System.out.println("Updating score by " + scoreAux + " for a destination " + destination);
                // System.out.println("Total score " + score);
                score += distances[detectiveLocation][destination];
            }
        }
        return score;
    }

    private void updatePossibleLocations(int moveMade, HashSet<Integer> locations) {
        // System.out.println(moveMade);
        // System.out.println("\nLocations right at the top of updatePossibleLocations");
        // for (Integer loc : locations) {
        //     System.out.print(loc + ", ");
        // }
        Colour colour = Colour.Black;
        if (isMoveDouble(moveMade)) {
            int move1 = moveMade / 100000;
            colour = decodeColour(move1);
        } else {
		    colour = decodeColour(moveMade);
        }
        // System.out.println("Updating possible locations.....");
        // System.out.println("Colour: " + colour + " currentRound: " + currentRound);
        if (colour == Colour.Black && rounds.get(currentRound + 1) == true) {
            //clearing global list of Mr X possible locations
            //mrXLocation = getPlayerLocation(Colour.Black);
            // System.out.println("Current round: " + (currentRound + 1));
            mrXLocation = decodeDestination(moveMade);
            locations.clear();
            locations.add(mrXLocation);
            // System.out.println("\nPOSSIBLE LOCATIONS RESTARTED");
        } else {
            if (colour == Colour.Black) {
                //MoveTicket movePrint = (MoveTicket) move;
                // System.out.println("\nI am black!");
                HashSet<Integer> newLocations = new HashSet<Integer>();
                // System.out.println("I have " + newLocations.size() + "locations. And I old have: " + locations.size());
                // System.out.println("Printing locations before update:");
                // for (Integer loc : locations) {
                //     System.out.print(loc + ", ");
                // }
                // System.out.println();
                for (Integer location : locations) {
                    //generatedMoves = graph.generateMoves(Colour.Black, location);
                    int m = 0;
                    // System.out.println("For location " + location);
                    for (int i = 1; i <= generatedMoves[location][0]; ++i) {
                        m = generatedMoves[location][i];
                        // System.out.println(generatedMoves[location][i]);
						// if both moves are single
                        if (isMoveTicket(m) && isMoveTicket(moveMade)) {
							// checks if colours and tickets match and if so it adds the location
							// to the generatedMoves. Modulo quicker then parsing!
                            int destination = decodeDestination(m);
							if (Math.abs(moveMade - m) < 201 && !occupiedNodes[destination]) {
                                // System.out.print("\nColour: " + colour);
                                // System.out.print(" Ticket: " + decodeTicket(moveMade));
                                // System.out.println(" Target: " + decodeDestination(moveMade));
                                // System.out.println("m = " + m + " moveMade = " + moveMade);
                                newLocations.add(destination);
							}
						// if both moves are double
					    } else if (isMoveDouble(m) && isMoveDouble(moveMade)) {
							//Probably parse and separate moves and then apply
							//technique above.
                            int m1 = m / 100000;
                            int m2 = m - m1 * 100000;
                            int moveMade1 = moveMade / 100000;
                            int moveMade2 = moveMade - moveMade1 * 100000;
                            int destinationM1 = decodeDestination(m1);
                            int destinationM2 = decodeDestination(m2);
                            if (Math.abs(moveMade1 - m1) < 201 && !occupiedNodes[destinationM1]) {
                                if (Math.abs(moveMade2 - m2) < 201 && !occupiedNodes[destinationM2]) {
                                    newLocations.add(destinationM2);
                                }
                            }
						}
                    }
                }
                locations.clear();
                locations.addAll(newLocations);
                // System.out.println("\nNow I have " + newLocations.size() + "locations. And I should have: " + locations.size());
                // System.out.println("Printing local locations:");
                // for (Integer loc : locations) {
                //     System.out.print(loc + ", ");
                // }
                // System.out.println("\nPrinting global locations:");
                // for (Integer loc : mrXPossibleLocations) {
                //     System.out.print(loc + ", ");
                // }
                // System.out.println("\n");
            } else {
                // System.out.println("\nI am a detective!");
                int destination = decodeDestination(moveMade);
                // System.out.println("Printing possible locations:");
                // for (Integer loc : locations) {
                    // System.out.print(loc + ", ");
                // }
                // System.out.println();
                if (locations.contains(destination)) {
                    // System.out.println("Removing target: " + destination);
                    locations.remove(destination);
                    // System.out.println("Printing updated locations:");
                    // for (Integer loc : locations) {
                    //     System.out.print(loc + ", ");
                    // }
                    // System.out.println("\n");
                }
            }
        }
    }

    private int decodeDestination(int move) {
        return move - (move / 1000) * 1000;
    }

	private int getSingleTicket(int move) {
		return (move - (move / 10000) * 10000) - decodeDestination(move);
	}

	private boolean isMoveDouble(int move) {
		return move > 100000;
	}

	private boolean isMovePass(int move) {
		return ((move / 10000) * 10000) == move;
	}

	private boolean isMoveTicket(int move) {
		return !isMovePass(move) && !isMoveDouble(move);
	}

    // Converts a colour into the Move form.
    public int encodeColour(Colour colour) {
       for (int i = 0; i < 6; ++i) {
           if (playerColours[i] == colour)
                return (i + 1) * 10000;
       }
       return -1;
    }

    // Converts a ticket into the Move form.
    public int encodeTicket(Ticket ticket) {
        for (int i = 0; i < 5; ++i) {
            if (ticketType[i] == ticket)
                return (i + 1) * 1000;
        }
        return -1;
    }

	public Colour decodeColour(int move) {
		int colour = move / 10000;
		return playerColours[colour - 1];
	}

	public Ticket decodeTicket(int move) {
		int ticket = getSingleTicket(move) / 1000;
		return ticketType[ticket - 1];
	}

	public Move decodeMove(int move) {
		if (isMoveTicket(move)) {
			Move single = decodeMoveTicket(move);
			return single;
		} else if (isMoveDouble(move)) {
			int m1 = move / 100000;
			int m2 = move - m1 * 100000;
			MoveTicket move1 = decodeMoveTicket(m1);
			MoveTicket move2 = decodeMoveTicket(m2);
			Move dbl = MoveDouble.instance(decodeColour(m1), move1, move2);
			return dbl;
		}
		Move pass = MovePass.instance(decodeColour(move));
		return pass;
	}

	public MoveTicket decodeMoveTicket(int move) {
		return MoveTicket.instance(decodeColour(move),
									 decodeTicket(move),
									 decodeDestination(move));
	}

	public int encodeMove(Move move) {
		if (move instanceof MoveTicket) {
			MoveTicket moveSingle = (MoveTicket) move;
			return encodeMove(moveSingle);
		} else if (move instanceof MoveDouble) {
			MoveDouble moveDouble = (MoveDouble) move;
			return encodeMove(moveDouble);
    	} else if (move instanceof MovePass) {
			// If move pass then return move with colour and
			// 0's for ticket and location.
			MovePass movePass = (MovePass) move;
			return encodeColour(movePass.colour);
		}
		return -1;
	}

	public int encodeMove(MoveTicket move) {
		int player = encodeColour(move.colour);
		int ticket = encodeTicket(move.ticket);
		return player + ticket + move.target;
	}

	public int encodeMove(MoveDouble move) {
		int move1 = encodeMove(move.move1);
		int move2 = encodeMove(move.move2);
		return move1 * 100000 + move2;
	}

    // Takes an array and an element as parameter and adds the element
    // to the array.
    public void addElementToArray(int[] a, int e) {
       a[ a[0] + 1 ] = e;
       ++a[0];
    }

	public boolean hasTickets(PlayerData player, Ticket ticket) {
        if (player.getTickets().get(ticket) == 0)
            return false;
        return true;
    }

    public void validMoves(Colour col, int level, boolean dbl, boolean secret) {
		movesValid[level][0] = 0;
		PlayerData player = getPlayer(col);
        // System.out.println("In validMoves " + player.getColour() + " is at location " + player.getLocation());
        int colour = encodeColour(col);
        int normalTicket = 0;
        int secretTicket = encodeTicket(Ticket.Secret);
		int doubleTicket = encodeTicket(Ticket.Double);
        int normalMove = 0;
        int secretMove = 0;
        Node<Integer> nodeLocation = graph.getNode(player.getLocation());
        // System.out.println("\nGenerating moves.");
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            //TODO: lookup for Transport to Ticket
            normalTicket = encodeTicket(Ticket.fromTransport(e.getData()));
            normalMove = colour + normalTicket + e.getTarget().getIndex();
            if (hasTickets(player, Ticket.fromTransport(e.getData())) && !occupiedNodes[e.getTarget().getIndex()]) {
                addElementToArray(movesValid[level], normalMove);
            }
            if (col == Colour.Black) {
                secretMove = colour + secretTicket + e.getTarget().getIndex();
                if (secret && hasTickets(player, Ticket.Secret) && !occupiedNodes[e.getTarget().getIndex()]) {
                    addElementToArray(movesValid[level], secretMove);
                }
                if (dbl && hasTickets(player, Ticket.Double)) {
                    player.removeTicket(Ticket.fromTransport(e.getData()));
                    validDoubleMoves(player, e, normalMove, level);
                    player.addTicket(Ticket.fromTransport(e.getData()));
                    if (secret && hasTickets(player, Ticket.Secret)) {
                	    player.removeTicket(Ticket.Secret);
                	    validDoubleMoves(player, e, secretMove, level);
                	    player.addTicket(Ticket.Secret);
                    }
                }
            }
        }
        /* add movePass */
		if(movesValid[level][0] == 0 && col != Colour.Black){
            System.out.println("\nIn validMoves, out of moves " + col + " " + encodeColour(col));
            addElementToArray(movesValid[level], encodeColour(col));
        }
        // System.out.println("In validMoves " + player + " at level " + level + "we thse moves");
        // for (int i = 1; i <= movesValid[level][0]; ++i) {
        //     if (isMoveTicket(movesValid[level][i]))
        //         System.out.print(movesValid[level][i] + " ");
        // }
        // System.out.println("");
	}

	public void validDoubleMoves(PlayerData player, Edge<Integer, Transport> previousEdge, int movePrevious, int level) {
		int colour = encodeColour(player.getColour());
		int normalTicket = 0;
        int secretTicket = encodeTicket(Ticket.Secret);
        int normalMove = 0;
        int secretMove = 0;
        int normalDouble = 0;
        int secretDouble = 0;
		Integer middle = (Integer) previousEdge.getTarget().getIndex();
        Node<Integer> nodeLocation = graph.getNode(middle);
		for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            normalTicket = encodeTicket(Ticket.fromTransport(e.getData()));
            if (hasTickets(player, Ticket.fromTransport(e.getData())) && !occupiedNodes[middle] && !occupiedNodes[e.getTarget().getIndex()]) {
				normalMove = colour + normalTicket + e.getTarget().getIndex();
				normalDouble = movePrevious * 100000 + normalMove;
				addElementToArray(movesValid[level], normalDouble);
            }
			if (hasTickets(player, Ticket.Secret) && !occupiedNodes[middle] && !occupiedNodes[e.getTarget().getIndex()]) {
            	secretMove = colour + secretTicket + e.getTarget().getIndex();
            	secretDouble = movePrevious * 100000 + secretMove;
            	addElementToArray(movesValid[level], secretDouble);
        	}
		}
	}
}
