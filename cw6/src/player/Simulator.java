package player;

import scotlandyard.*;
import graph.*;

import java.io.IOException;
import java.util.*;

public class Simulator extends ScotlandYard {
    private ScotlandYardView view;
    private String graphFilename;
    public HashSet<Integer> mrXPossibleLocations;
    private int justUsedDouble;
    private List<Boolean> rounds;
    private int currentRound;
    private int distances[][];
    private boolean occupiedNodes[];
	private int[][] generatedMoves;
	private int[][] movesValid;
    private int[] onlyTaxiLinks;
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

    /**
     * Constructs a new Simulator object which extends ScotlandYard.
     *
     * @param numberOfDetectives the number of detectives in the game.
     * @param rounds a list of booleans representing reveal rounds with true.
     * @param graph a ScotlandYardGraph to represent the board.
     * @param queue the Queue used to put pending moves onto.
     * @param gameId the id of this game.
     */
    public Simulator(Integer numberOfDetectives,
                    List<Boolean> rounds,
                    ScotlandYardGraph graph,
                    MapQueue<Integer, Token> queue,
                    Integer gameId) {
        super(numberOfDetectives, rounds, graph, queue, gameId);
    }

    /**
     * Initialises the simulator.
     *
     * @param view the ScotlandYardView that provides game information.
     * @param graphFilename the name of the file containing the graph components.
     * @param distances two dimensional array containing distances from each node to every other node on the graph.
     * @param generatedMoves two dimensional array storing generated moves at each level of the minimax.
     * @param onlyTaxiLinks an array indexed by graph nodes containing 1 if the node has just taxi links and 0 otherwise.
     */
    public void setSimulator(ScotlandYardView view,
                             String graphFilename,
                             int distances[][],
                             int generatedMoves[][],
                             int onlyTaxiLinks[]) {
        this.view = view;
        this.graphFilename = graphFilename;
		this.rounds = view.getRounds();
		this.currentRound = view.getRound();
        this.mrXPossibleLocations = new HashSet<Integer>();
        this.distances = distances;
        this.justUsedDouble = 0;
        this.generatedMoves = generatedMoves;
        this.occupiedNodes = new boolean[201];
		this.movesValid = new int[20][500];
        this.onlyTaxiLinks = onlyTaxiLinks;
        for (Colour p : playerColours) {
            Map<Ticket, Integer> tickets = new HashMap<Ticket, Integer>();
            for(int i = 0; i < ticketType.length; ++i) {
                if (p == Colour.Black) {
                    tickets.put(ticketType[i], mrXTicketNumbers[i]);
                } else {
                    tickets.put(ticketType[i], detectiveTicketNumbers[i]);
                }
            }
            if (p == Colour.Black) {
                join(new SimulatedPlayer(), p, 0, tickets);
            } else {
                join(new SimulatedPlayer(), p, 0, tickets);
            }
        }
        this.currentPlayer = getPlayer(Colour.Black);
    }

    /**
     * Sets the locations of the detectives.
     *
     */
    public void setLocations() {
        for (PlayerData p : players) {
            if (p.getColour() == Colour.Black) continue;
            p.setLocation(view.getPlayerLocation(p.getColour()));
            occupiedNodes[view.getPlayerLocation(p.getColour())] = true;
        }
    }

    /**
     * Sets currentPlayer to be a previous player.
     *
     */
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

    /**
     * Reverses the game to the state before the given move was played.
     *
     * @param move a single move played.
     * @param previousLocation a location of the player before the move was made.
     */
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

    /**
     * Reverses the game to the state before the given move was played.
     *
     * @param move a double move played.
     * @param previousLocation a location of the player before the move was made.
     */
    private void reverseDoubleMove(int move, int previousLocation) {
		int move1 = move / 100000;
		int move1Target = decodeDestination(move1);
		int move2 = move - move1 * 100000;
        reversePlay(move2, move1Target);
        reversePlay(move1, previousLocation);
        getPlayer(Colour.Black).addTicket(Ticket.Double);
    }

    /**
     * Reverses the game to the state before the given move was played.
     *
     * @param move a move played.
     * @param previousLocation a location of the player before the move was made.
     */
    private void reversePlay(int move, int previousLocation) {
        if (isMoveTicket(move)) {
            reverseTicketMove(move, previousLocation);
        } else if (isMoveDouble(move)) {
            reverseDoubleMove(move, previousLocation);
        } else if (isMovePass(move)) {
            return;
        }
    }

    /**
     * Plays a given move and updates mrX's possible locations.
     *
     * @param move a move to be played.
     * @param locations a HashSet of mrX's possible locations.
     */
    protected void play(int move, HashSet<Integer> locations) {
        if (isMoveTicket(move)) playTicket(move, locations);
        else if (isMoveDouble(move)) playDouble(move, locations);
        else if (isMovePass(move)) return;
    }

    /**
     * Plays a given single move and updates mrX's possible locations.
     *
     * @param move a single move to be played.
     * @param locations a HashSet of mrX's possible locations.
     */
    protected void playTicket(int move, HashSet<Integer> locations) {
        updatePossibleLocations(move, locations);
        Colour colour = decodeColour(move);
		Ticket ticket = decodeTicket(move);
		int target = decodeDestination(move);
		PlayerData player = getPlayer(colour);
		player.removeTicket(ticket);
        occupiedNodes[player.getLocation()] = false;
		player.setLocation(target);
		if (colour != Colour.Black) {
			PlayerData mrX = getPlayer(Colour.Black);
			mrX.addTicket(ticket);
            occupiedNodes[target] = true;
		} else {
			currentRound++;
		}
    }

    /**
     * Plays a given double move and updates mrX's possible locations.
     *
     * @param move a double move to be played.
     * @param locations a HashSet of mrX's possible locations.
     */
    protected void playDouble(int move, HashSet<Integer> locations) {
		int move1 = move / 100000;
		int move2 = move - move1 * 100000;
		Colour colour = decodeColour(move1);
		playTicket(move1, locations);
		playTicket(move2, locations);
        PlayerData mrX = getPlayer(colour);
        mrX.removeTicket(Ticket.Double);
    }

    /**
     * Updates a simulator with the move received from the server.
     *
     * @param moveMade a move to be played.
     */
    public void receiveMove(Move moveMade) {
		int move = encodeMove(moveMade);
        if (isMoveDouble(move)) {
            justUsedDouble = 2;
        } else {
            if (justUsedDouble == 2) {
                --justUsedDouble;
                return;
            } else if (justUsedDouble == 1) {
                --justUsedDouble;
                return;
            }
        }
        play(move, this.mrXPossibleLocations);
    }

    /**
     * Recursively explores the possible valid moves by taking into account the
     * current configuration of the board and scoring each further configuration.
     *
     * @param player a colour of the player for whom the moves are generated.
     * @param location a location of the player.
     * @param level a depth level of the minimax.
     * @param currentConfigurationScore a score of the current board configuration.
     * @param previousScore a score of the previous branch board configuration.
     * @param mrXOldLocations the possible locations of mrX in the previous configuration.
     * @return the best possible move available.
     */
    public int minimax(Colour player,
                        int location,
                        int level,
                        int[] currentConfigurationScore,
                        int previousScore,
                        HashSet<Integer> mrXOldLocations) {
        if (player == Colour.Black) {
            getPlayer(Colour.Black).setLocation(location);
            if (view.getRound() == 0) {
                mrXOldLocations.add(location);
            }
        }
        currentConfigurationScore[0] = getNodeRank(getPlayer(Colour.Black).getLocation()) * 100;
        /* if the minimum distance to a detective is zero, it means that Mr X
         * was caught so we can end this branch of the minimax
         */
        if (currentConfigurationScore[0] == 0) {
            return DUMMYMOVE;
        }
        Integer bestScore = 0;
        if ((level == 6) || (currentRound == 24 && player == Colour.Black)) {
            currentConfigurationScore[0] += mrXOldLocations.size();
            currentConfigurationScore[0] *= 1000;
            currentConfigurationScore[0] += getDetectiveScore(mrXOldLocations);
            return DUMMYMOVE;
        }
        int xScore = currentConfigurationScore[0];
        if (player == Colour.Black) {
            validMoves(player, level, false, xScore, location, mrXOldLocations);
            boolean useDoubles = true;
            for (int i = 1; i <= movesValid[level][0]; ++i) {
                int destination = decodeDestination(movesValid[level][i]);
                if (getNodeRank(destination) > 1) {
                    useDoubles = false;
                    break;
                }
            }
            if (hasTickets(getPlayer(player), Ticket.Double) && useDoubles) {
                validMoves(player, level, true, xScore, location, mrXOldLocations);
            }
        } else {
            validMoves(player, level, false, xScore, location, mrXOldLocations);
        }
        int bestMove = DUMMYMOVE;
        int[] nextScore = new int[1];
        nextScore[0] = 0;
        if (player == Colour.Black)
            bestScore = Integer.MIN_VALUE;
        else
            bestScore = Integer.MAX_VALUE;
        HashSet<Integer> mrXNewLocations = new HashSet<Integer>();
        for (int i = 1; i <= movesValid[level][0]; ++i) {
            if (player == Colour.Black) {
                if (previousScore != Integer.MAX_VALUE && getXScore(bestScore) > getXScore(previousScore)) {
                    currentConfigurationScore[0] = bestScore;
                    return bestMove;
                }
            } else {
                if (player == Colour.Blue) { // AND previous player is Black
                    if (previousScore != Integer.MIN_VALUE && getDScore(bestScore) < getDScore(previousScore)) {
                        currentConfigurationScore[0] = bestScore;
                        return bestMove;
                    }
                }
            }
            mrXNewLocations.clear();
            mrXNewLocations.addAll(mrXOldLocations);
            play(movesValid[level][i], mrXNewLocations);
            nextPlayer();
            /*************************************/
            minimax(currentPlayer.getColour(),
                    currentPlayer.getLocation(),
                    level + 1,
                    nextScore,
                    bestScore,
                    mrXNewLocations);
            /*************************************/
            if (player == Colour.Black) {
                if (bestScore == Integer.MIN_VALUE || getXScore(bestScore) < getXScore(nextScore[0])) {
                    bestScore = nextScore[0];
                    bestMove = movesValid[level][i];
                }
            } else {
                if (bestScore == Integer.MAX_VALUE || getDScore(bestScore) > getDScore(nextScore[0])) {
                    bestScore = nextScore[0];
                    bestMove = movesValid[level][i];
                }
            }
            previousPlayer();
            reversePlay(movesValid[level][i], location);
        }
        if (bestScore == Integer.MIN_VALUE) {
            currentConfigurationScore[0] = 0;
        } else if (isMovePass(movesValid[level][1])) {
            currentConfigurationScore[0] = (getXScore(currentConfigurationScore[0]) + 20) * 1000
                                          + getDScore(currentConfigurationScore[0]) + 10;
        } else {
            currentConfigurationScore[0] = bestScore;
        }
        return bestMove;
    }

    /**
     * Extracts mrX's score from the given encoded score.
     *
     * @param score the encoded score of a node.
     * @return mrX's score.
     */
    private int getXScore(int score) {
        return score / 1000;
    }

    /**
     * Extracts detective's score from the given encoded score.
     *
     * @param score the encoded score of a node.
     * @return detectives's score.
     */
    private int getDScore(int score) {
        return score - (score / 1000) * 1000;
    }

    /**
     * Produces a rank for a given node location.
     *
     * @param location a location on the graph for which the rank is to be computed.
     * @return a rank for given location on the graph.
     */
	private int getNodeRank(int location) {
		int bestScore = 666013;
        int score = 0;
		for (Colour p : playerColours){
			if (p == Colour.Black) continue;
            score = distances[getPlayerLocation(p)][location];
            bestScore = Math.min(bestScore, score);
		}
		return bestScore;
	}

    /**
    * Produces the sum of shortest distances from each detective to all possible locations of mrX.
    *
    * @param mrXPossibleLocations a HashSet of mrX possible locations.
    * @return the sum of the distances from each detective to all possible locations of mrX.
    */
    private int getDetectiveScore(HashSet<Integer> mrXPossibleLocations) {
        int detectiveLocation = 0;
        int score = 0;
        for (int i = 1; i < 6; ++i) {
            detectiveLocation = getPlayerLocation(playerColours[i]);
            for (Integer destination : mrXPossibleLocations) {
                int scoreAux = distances[detectiveLocation][destination];
                score += distances[detectiveLocation][destination];
            }
        }
        return score;
    }

    /**
    * Updates mrX's possible locations given a move and the set of possible locations.
    *
    * @param moveMade a move made.
    * @param locations a HashSet of mrX possible locations.
    */
    private void updatePossibleLocations(int moveMade, HashSet<Integer> locations) {
        Colour colour = decodeColour(moveMade);
        if (colour == Colour.Black && rounds.get(currentRound + 1)) {
            //clearing global list of Mr X possible locations
            mrXLocation = decodeDestination(moveMade);
            locations.clear();
            locations.add(mrXLocation);
        } else {
            if (colour == Colour.Black) {
                HashSet<Integer> newLocations = new HashSet<Integer>();
                for (Integer location : locations) {
                    int m = 0;
                    for (int i = 1; i <= generatedMoves[location][0]; ++i) {
                        m = generatedMoves[location][i];
                        if (isMoveTicket(m) && isMoveTicket(moveMade)) {
                            int destination = decodeDestination(m);
							if (Math.abs(moveMade - m) < 201 && !occupiedNodes[destination]) {
                                newLocations.add(destination);
							}
					    } else if (isMoveDouble(m) && isMoveDouble(moveMade)) {
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
            } else {
                int destination = decodeDestination(moveMade);
                if (locations.contains(destination)) {
                    locations.remove(destination);
                }
            }
        }
    }

    /**
    * Extracts the destination from encoded move.
    *
    * @param move a move containing the destination.
    * @return the destination of a given move.
    */
    private int decodeDestination(int move) {
        return move - (move / 1000) * 1000;
    }

    /**
    * Extracts the ticket from encoded move.
    *
    * @param move a move containing the ticket.
    * @return the index of a ticket used in the given move.
    */
	private int getSingleTicket(int move) {
		return (move - (move / 10000) * 10000) - decodeDestination(move);
	}

    /**
    * Checks if the given move is a MoveDouble.
    *
    * @param move an encoded move.
    * @return true if move is MoveDouble and false otherwise.
    */
	private boolean isMoveDouble(int move) {
		return move > 100000;
	}

    /**
    * Checks if the given move is a MovePass.
    *
    * @param move an encoded move.
    * @return true if move is MovePass and false otherwise.
    */
	private boolean isMovePass(int move) {
		return ((move / 10000) * 10000) == move;
	}

    /**
    * Checks if the given move is a MoveTicket.
    *
    * @param move an encoded move.
    * @return true if move is MoveTicket and false otherwise.
    */
	private boolean isMoveTicket(int move) {
		return !isMovePass(move) && !isMoveDouble(move);
	}

    /**
    * Encodes the colour of the player into the move form.
    *
    * @param colour a colour of the player to be encoded.
    * @return a colour of the player in the int form.
    */
    public int encodeColour(Colour colour) {
       for (int i = 0; i < 6; ++i) {
           if (playerColours[i] == colour)
                return (i + 1) * 10000;
       }
       return -1;
    }

    /**
    * Encodes the colour into the move form.
    *
    * @param ticket a ticket to be encoded.
    * @return a ticket in the int form.
    */
    public int encodeTicket(Ticket ticket) {
        for (int i = 0; i < 5; ++i) {
            if (ticketType[i] == ticket)
                return (i + 1) * 1000;
        }
        return -1;
    }

    /**
    * Decodes the colour from the move.
    *
    * @param move a move from which the colour should be decoded.
    * @return a colour of the player who made the given move.
    */
	public Colour decodeColour(int move) {
		int colour = move / 10000;
		return playerColours[colour - 1];
	}

    /**
    * Decodes the ticket from the move.
    *
    * @param move a move from which the colour should be decoded.
    * @return a colour of the player who made the given move.
    */
	public Ticket decodeTicket(int move) {
		int ticket = getSingleTicket(move) / 1000;
		return ticketType[ticket - 1];
	}

	/**
    * Converts a move of type int into the move of type Move.
    *
    * @param move a int move which is to be coverted.
    * @return a converted move of type Move.
    */
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

	/**
    * Converts a single move of type int into the single move of type Move.
    *
    * @param move a single int move which is to be coverted.
    * @return a converted single move of type Move.
    */
	public MoveTicket decodeMoveTicket(int move) {
		return MoveTicket.instance(decodeColour(move),
									 decodeTicket(move),
									 decodeDestination(move));
	}

	/**
    * Converts a move of type Move into the move of type int.
    *
    * @param move a Move move which is to be coverted.
    * @return a converted move of type int.
    */
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

	/**
    * Converts a single move of type Move into the single move of type int.
    *
    * @param move a Move move which is to be coverted.
    * @return a converted move of type int.
    */
	public int encodeMove(MoveTicket move) {
		int player = encodeColour(move.colour);
		int ticket = encodeTicket(move.ticket);
		return player + ticket + move.target;
	}

	/**
    * Converts a double move of type Move into the double move of type int.
    *
    * @param move a Move move which is to be coverted.
    * @return a converted move of type int.
    */
	public int encodeMove(MoveDouble move) {
		int move1 = encodeMove(move.move1);
		int move2 = encodeMove(move.move2);
		return move1 * 100000 + move2;
	}

	/**
    * Adds a given element into the given array.
    *
    * @param a an array in which we are adding the element.
    * @return e an element to be added.
    */
    // public void addElementToArray(int[] a, int e) {
    //    a[ a[0] + 1 ] = e;
    //    ++a[0];
    // }

	/**
    * Checks if a given player has a given ticket.
    *
    * @param player a PlayerData object for which we are checking if he has tickets.
	* @param ticket a ticket type to be checked.
    * @return true if given player contains a given ticker and false otherwise.
    */
	public boolean hasTickets(PlayerData player, Ticket ticket) {
        if (player.getTickets().get(ticket) == 0)
            return false;
        return true;
    }

	/**
    * Generates the valid moves for a given player.
    *
    * @param col a colour of the player for which we are generating the moves.
	* @param level a level of depth in the minimax.
    * @param dbl a boolean which if true double moves are generated.
    * @param xScore mrX's current score.
    * @param location a location of the player.
    * @param mrXOldLocations a set of possible locations mrX could be at.
    */
    public void validMoves(Colour col, int level, boolean dbl, int xScore, int location, HashSet<Integer> mrXOldLocations) {
		movesValid[level][0] = 0;
		PlayerData player = getPlayer(col);
        int colour = encodeColour(col);
        int normalTicket = 0;
        int secretTicket = encodeTicket(Ticket.Secret);
		int doubleTicket = encodeTicket(Ticket.Double);
        int normalMove = 0;
        int secretMove = 0;
        Node<Integer> nodeLocation = graph.getNode(player.getLocation());
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            normalTicket = encodeTicket(Ticket.fromTransport(e.getData()));
            normalMove = colour + normalTicket + e.getTarget().getIndex();
            if (hasTickets(player, Ticket.fromTransport(e.getData())) && !occupiedNodes[e.getTarget().getIndex()]) {
                AIPlayerFactory.addElementToArray(movesValid[level], normalMove);
            }
            if (col == Colour.Black) {
                secretMove = colour + secretTicket + e.getTarget().getIndex();
                if (hasTickets(player, Ticket.Secret) && !occupiedNodes[e.getTarget().getIndex()]
                && getRound() >= 3 && !view.getRounds().get(getRound()) && mrXOldLocations.size() < 5
                && xScore < 300 && onlyTaxiLinks[location] == 0) {
                    AIPlayerFactory.addElementToArray(movesValid[level], secretMove);
                }
                if (dbl && hasTickets(player, Ticket.Double)) {
                    player.removeTicket(Ticket.fromTransport(e.getData()));
                    validDoubleMoves(player, e, normalMove, level);
                    player.addTicket(Ticket.fromTransport(e.getData()));
                    if (hasTickets(player, Ticket.Secret)
                    && getRound() >= 3 && !view.getRounds().get(getRound())
                    && mrXOldLocations.size() < 5 && xScore < 300 && onlyTaxiLinks[location] == 0) {
                	    player.removeTicket(Ticket.Secret);
                	    validDoubleMoves(player, e, secretMove, level);
                	    player.addTicket(Ticket.Secret);
                    }
                }
            }
        }
        // If a detective has no valid moves, add MovePass.
		if(movesValid[level][0] == 0 && col != Colour.Black){
            AIPlayerFactory.addElementToArray(movesValid[level], encodeColour(col));
        }
	}

	/**
	* Generates the valid double moves for a given player.
	*
	* @param player a player for which we are generating the moves.
	* @param previousEdge a graph edge of the previous move.
	* @param movePrevious an encoded previous move.
	* @param level a level of depth in the minimax.
	*/
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
				AIPlayerFactory.addElementToArray(movesValid[level], normalDouble);
            }
			if (hasTickets(player, Ticket.Secret) && !occupiedNodes[middle] && !occupiedNodes[e.getTarget().getIndex()]) {
            	secretMove = colour + secretTicket + e.getTarget().getIndex();
            	secretDouble = movePrevious * 100000 + secretMove;
            	AIPlayerFactory.addElementToArray(movesValid[level], secretDouble);
        	}
		}
	}
}
