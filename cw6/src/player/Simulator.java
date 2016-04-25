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

    private static Ticket TAXI = Ticket.fromTransport(Transport.Taxi);
    private static Ticket BUS = Ticket.fromTransport(Transport.Bus);
    private static Ticket UG = Ticket.fromTransport(Transport.Underground);
    private static MoveTicket DUMMYMOVE = MoveTicket.instance(Colour.Black, Ticket.Taxi, 42);

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
            System.out.println("\nI am updating now!");
            System.out.println("Printing old locations:");
            for (Integer loc : this.mrXPossibleLocations) {
                System.out.print(loc + ", ");
            }
            System.out.println("\n");
            updatePossibleLocations(encodeMove(move), this.mrXPossibleLocations);
            System.out.println("Printing updated locations:");
            for (Integer loc : this.mrXPossibleLocations) {
                System.out.print(loc + ", ");
            }
            System.out.println("\n");
            play(move);
            //System.out.println("\nCurrent Round: " + currentRound + "\n");
        }
    }

    private void notifySpectators(Move move) {

    }

    // TEST IF CONFIGURATION SCORES ARE COMPUTED CORRECTLY
    public Move minimax(Colour player,
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
            getPlayer(Colour.Black).setLocation(location);
        }
        // if (isGameOver()) {
        //     if (getWinningPlayers().size() == 1) {
        //         currentConfigurationScore[0] = 99999;// Mr.X won
        //     } else {
        //         currentConfigurationScore[0] = 0;// detectives won
        //     }
        //     return MoveTicket.instance(player, Ticket.Taxi, 69);
        //     //int doesn't even matter
        // }
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
        currentConfigurationScore[0] = getNodeRank(getPlayer(Colour.Black).getLocation()) * 100;
        /* if the minimum distance to a detective is zero, it means that Mr X
         * was caught so we can end this branch of the minimax
         */
        if (currentConfigurationScore[0] == 0) {
            return DUMMYMOVE;
        }
        Integer bestScore = 0;
        if ((level == 1) || (currentRound == 22 && player == Colour.Black)) {
            // if (currentConfigurationScore[0] != 0) {
            currentConfigurationScore[0] += mrXOldLocations.size();
            // }
            // System.out.print("\nConfiguration updated ");
            // System.out.println(currentConfigurationScore[0]);
            return DUMMYMOVE;
        }
        List<Move> moves = validMoves(player);
        MoveTicket bestMove = DUMMYMOVE;
        int[] nextScore = new int[1];
        nextScore[0] = 0;
        if (player == Colour.Black)
            bestScore = Integer.MIN_VALUE;
        else
            bestScore = Integer.MAX_VALUE;
        HashSet<Integer> mrXNewLocations = new HashSet<Integer>();
        for (Move move : moves) {
            if (move instanceof MoveDouble) continue;
            //if (move instanceof MoveTicket) continue;
            if (move instanceof MovePass) continue;
            MoveTicket currentMove = (MoveTicket) move;

            //if (currentMove.ticket == Ticket.Secret) continue;
            /********************************/
            /**********TREE PRUNING**********/
            /********************************/
            // if (player == Colour.Black) {
            //     if (bestScore > previousScore) {
            //         System.out.println("\nPruning the tree.");
            //         System.out.println("Updating best move.");
            //         System.out.println("bestScore: " + bestScore + " previousScore: " + previousScore);
            //         System.out.println("Target: " + currentMove.target);
            //         currentConfigurationScore[0] = bestScore;
            //         return bestMove;
            //     }
            // } else {
            //     if (bestScore < previousScore) {
            //         System.out.println("\nPruning the tree.");
            //         System.out.println("Updating best move.");
            //         System.out.println("bestScore: " + bestScore + " previousScore: " + previousScore);
            //         System.out.println("Target: " + currentMove.target);
            //         currentConfigurationScore[0] = bestScore;
            //         return bestMove;
            //     }
            // }
            mrXNewLocations.clear();
            mrXNewLocations.addAll(mrXOldLocations);
            updatePossibleLocations(encodeMove(move), mrXNewLocations);
            play(move);
            //System.out.println("Moving to " + currentMove.target);
            nextPlayer();
            /*************************************/
            minimax(currentPlayer.getColour(),
                    currentPlayer.getLocation(),
                    level + 1,
                    nextScore,
                    bestScore,
                    mrXNewLocations);

            System.out.print("nextScore: " +nextScore[0]);
            System.out.println("Target: " + currentMove.target);
            /*************************************/
            if (player == Colour.Black) {
                if (bestScore < nextScore[0]) {
                    if (level == 4) {
                        System.out.println("\nUpdating best move.");
                        System.out.println("bestScore: " + bestScore + " nextScore: " + nextScore[0]);
                        System.out.println("Target: " + currentMove.target);
                    }
                    bestScore = nextScore[0];
                    bestMove = (MoveTicket) move;
                }
            } else {
                if (bestScore > nextScore[0]) {
                    if (level == 4) {
                        System.out.println("\nUpdating best move.");
                        System.out.println("bestScore: " + bestScore + " nextScore: " + nextScore[0]);
                        System.out.println("Target: " + currentMove.target);
                    }
                    bestScore = nextScore[0];
                    bestMove = (MoveTicket) move;
                }
            }
            previousPlayer();
            reversePlay(move, location);
        }
        //System.out.println("\nbest score after: " + bestScore);
        return bestMove;
    }

    // get distance to closest detective; from detective to mr X.
	private int getNodeRank(int location) {
		Dijkstra dijkstra = new Dijkstra(graphFilename);
		List<Integer> route = new ArrayList<Integer>();
		int bestScore = 666013;
        int score = 0;
		for (Colour p : playerColours){
			if (p == Colour.Black) continue;
			Map<Transport, Integer> tickets = new HashMap<Transport, Integer>();
			tickets.put(Transport.Bus, getPlayerTickets(p, Ticket.fromTransport(Transport.Bus)));
			tickets.put(Transport.Taxi, getPlayerTickets(p, Ticket.fromTransport(Transport.Taxi)));
			tickets.put(Transport.Underground, getPlayerTickets(p, Ticket.fromTransport(Transport.Underground)));
            // System.out.println("Detective Location: " + view.getPlayerLocation(p));
			// System.out.println("Destination: " + location);
			// System.out.println("Tickets Bus: " + tickets.get(Transport.Bus));
			// System.out.println("Tickets Taxi: " + tickets.get(Transport.Taxi));
			// System.out.println("Tickets UG: " + tickets.get(Transport.Underground));
			// System.out.println("");

            route = dijkstra.getRoute(getPlayerLocation(p), location, tickets, p);
            score = distancesByTickets[getPlayerLocation(p)]
                                      [location]
                                      [getPlayerTickets(p, TAXI)]
                                      [getPlayerTickets(p, BUS)]
                                      [getPlayerTickets(p, UG)];
            System.out.println("From " + p + " at " + getPlayerLocation(p) + " to Black at " + location + " the distance is " + score);
            System.out.println("Dijkstra score: " + route.size());
            bestScore = Math.min(bestScore, score);
		}
		return bestScore;
	}

    // private void filterMoves(int m, int moveMade, HashSet<Integer> locations) {
	// 	// checks if two colour and ticket match and if so it adds the location
	// 	// to the possibleMoves. Modulo quicker then parsing!
	// 	if (moveMade - m > -201 || moveMade - m < 201) {
	// 		locations.add(m % 1000)
	// 	}
    // }
	//
	// public static int firstDigit(int n) {
 // 		while (n < -9 || 9 < n) n /= 10;
 //  		return Math.abs(n);
	// }
	//
    // private void filterMoves(MoveDouble moveDouble, MoveDouble moveMade, HashSet<Integer> locations) {
    //     if (moveDouble.move1.ticket == moveMade.move1.ticket && !occupiedNodes[moveDouble.move1.target]){
    //         if (moveDouble.move2.ticket == moveMade.move2.ticket && !occupiedNodes[moveDouble.move2.target]){
    //             locations.add(moveDouble.move2.target);
    //         }
    //     }
    // }

    private void updatePossibleLocations(int moveMade, HashSet<Integer> locations) {
        if (getCurrentPlayer() == Colour.Black && rounds.get(view.getRound()) == true) {
            //clearing global list of Mr X possible locations
            mrXLocation = view.getPlayerLocation(Colour.Black);
            locations.clear();
            locations.add(mrXLocation);
            // System.out.println("\nPOSSIBLE LOCATIONS RESTARTED");
        } else {
            // moveMade.colour == Colour.Black
            if ((moveMade / 10000) % 10 == 1) {
                // System.out.println("\nI am black!");
                HashSet<Integer> newLocations = new HashSet<Integer>();
                // System.out.println("I have " + newLocations.size() + "locations. And I should have: " + locations.size());
                // System.out.println("Printing possible locations:");
                // for (Integer loc : newLocations) {
                //     System.out.print(loc + ", ");
                // }
                // System.out.println();
                for (Integer location : locations) {
                    int[] possibleMoves = generateMoves(Colour.Black, location);
                    //possibleMoves = graph.generateMoves(Colour.Black, location);
                    int m = 0;
                    for (int i = 1; i <= possibleMoves[0]; ++i) {
                        m = possibleMoves[i];
						// if both moves are single
                        if (m < 100000 && moveMade < 100000) {
							// checks if colours and tickets match and if so it adds the location
							// to the possibleMoves. Modulo quicker then parsing!
                            int destination = getDestination(m);
							if (Math.abs(moveMade - m) < 201 && !occupiedNodes[destination]) {
                                // System.out.print("\nColour: " + movePrint.colour);
                                // System.out.print(" Ticket: " + movePrint.ticket);
                                // System.out.println(" Target: " + movePrint.target);
                                // System.out.println("m = " + m + " moveMade = " + moveMade);
                                newLocations.add(destination);
							}
						// if both moves are double
                        } else if (m > 100000 && moveMade > 100000) {
							//Probably parse and separate moves and then apply
							//technique above.
                            int m1 = m / 100000;
                            int m2 = m - m1 * 100000;
                            int moveMade1 = moveMade / 100000;
                            int moveMade2 = moveMade - moveMade1 * 100000;
                            int destinationM1 = getDestination(m1);
                            int destinationM2 = getDestination(m2);
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
                // System.out.println("Printing updated locations:");
                // for (Integer loc : newLocations) {
                //     System.out.print(loc + ", ");
                // }
                // System.out.println("\n");
            } else {
                // System.out.println("\nI am a detective!");
                int destination = getDestination(moveMade);
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

    private int getDestination(int move) {
        return move - (move / 1000) * 1000;
    }


   //  public int[] validMoves(Colour player) {
   //     int[] listOfValidMoves = new int[30];
   //     PlayerData playerAux = getPlayer(player);
   //     generateMoves(playerAux, listOfValidMoves);
   //     if(listOfValidMoves.length() == 0 && player != Colour.Black){
   //         listOfValidMoves.add(MovePass.instance(player));
   //     }
   //     return listOfValidMoves;
   //
   // }

   /**
    * Returns true if target node is occupied
    *
    * @param edge The edge we are using to test if the target node is occupied.
    * @return true if target occupied, false otherwise.
    */
   // private boolean isOccupied(Edge<Integer, Transport> edge) {
   //     for (PlayerData p : playersInGame) {
   //         if (p.getColour() != Colour.Black && p.getLocation() == (Integer) edge.getTarget().getIndex()) {
   //             return true;
   //         }
   //     }
   //     return false;
   // }

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

	// public int decodeTicket(int move) {
	//
	// }

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

    // Generates all possible moves(encoded form) for a player given a location.
    private int[] generateMoves(Colour colour, int location) {
        int[] generatedMoves = new int[500];
        int player = encodeColour(colour);
        int normalTicket = 0;
        int secretTicket = encodeTicket(Ticket.Secret);
        int normalMove = 0;
        int secretMove = 0;
        Node<Integer> nodeLocation = graph.getNode(location);
        // System.out.println("\nGenerating moves.");
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
           //TODO: lookup for Transport to Ticket
           normalTicket = encodeTicket(Ticket.fromTransport(e.getData()));
           normalMove = player + normalTicket + e.getTarget().getIndex();
           //System.out.println("Adding normalMove: " + normalMove);
           addElementToArray(generatedMoves, normalMove);
           if (colour == Colour.Black) {
        	   secretMove = player + secretTicket + e.getTarget().getIndex();
        	   addElementToArray(generatedMoves, secretMove);
        	   generateDoubleMoves(player, e, normalMove, generatedMoves);
        	   generateDoubleMoves(player, e, secretMove, generatedMoves);
           }
        }
        // System.out.println("All generated moves: ");
        // for (int i = 0; i < generatedMoves[0]; ++i) {
        //     System.out.print(generatedMoves[i] + " ");
        // }
        // System.out.println("");
        return generatedMoves;
    }

    // Generates all double moves given a player, previous edge and previous move.
    // Double moves are two single moves concatenated.
    private void generateDoubleMoves(int player, Edge previousEdge, int movePrevious, int[] generatedMoves) {
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
           normalMove = player + normalTicket + e.getTarget().getIndex();
           normalDouble = movePrevious * 100000 + normalMove;
           addElementToArray(generatedMoves, normalDouble);
           secretMove = player + secretTicket + e.getTarget().getIndex();
           secretDouble = movePrevious * 100000 + secretMove;
           addElementToArray(generatedMoves, secretDouble);
        }
        // System.out.println("All double generated moves: ");
        // for (int i = 0; i < generatedMoves[0]; ++i) {
        //    System.out.print(generatedMoves[i] + " ");
        // }
        // System.out.println("");
    }
}
