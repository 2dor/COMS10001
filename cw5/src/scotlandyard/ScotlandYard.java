package scotlandyard;

import java.io.IOException;
import java.util.*;
import graph.*;
/**
 * A class to perform all of the game logic.
 */

public class ScotlandYard implements ScotlandYardView, Receiver {

    protected MapQueue<Integer, Token> queue;
    protected Integer gameId;
    protected Random random;
    protected ScotlandYardGraph graph;
    protected List<Boolean> rounds;
    protected Integer numberOfDetectives;
    protected List<PlayerData> playersInGame;
    private Integer mrXLastLocation;
    private Integer currentRound;
    private Colour currentPlayer;

    /**
     * Constructs a new ScotlandYard object. This is used to perform all of the game logic.
     *
     * @param numberOfDetectives the number of detectives in the game.
     * @param rounds the List of booleans determining at which rounds Mr X is visible.
     * @param graph the graph used to represent the board.
     * @param queue the Queue used to put pending moves onto.
     * @param gameId the id of this game.
     */
    public ScotlandYard(Integer numberOfDetectives, List<Boolean> rounds, ScotlandYardGraph graph, MapQueue<Integer, Token> queue, Integer gameId) {
        this.queue = queue;
        this.gameId = gameId;
        this.random = new Random();
        this.graph = graph;
        this.rounds = rounds;
        this.numberOfDetectives = numberOfDetectives;
        this.playersInGame = new ArrayList<PlayerData>();
        this.currentRound = 0;
        this.mrXLastLocation = 0;
        this.currentPlayer = Colour.Black;
    }

    /**
     * Starts playing the game.
     */
    public void startRound() {
        if (isReady() && !isGameOver()) {
            turn();
        }
    }

    /**
     * Notifies a player when it is their turn to play.
     */
    public void turn() {
        Integer token = getSecretToken();
        queue.put(gameId, new Token(token, getCurrentPlayer(), System.currentTimeMillis()));
        notifyPlayer(getCurrentPlayer(), token);

    }

    /**
     * Plays a move sent from a player.
     *
     * @param move the move chosen by the player.
     * @param token the secret token which makes sure the correct player is making the move.
     */
    public void playMove(Move move, Integer token) {
        Token secretToken = queue.get(gameId);
        if (secretToken != null && token == secretToken.getToken()) {
            queue.remove(gameId);
            play(move);
            nextPlayer();
            startRound();
        }
    }

    /**
     * Returns a random integer. This is used to make sure the correct player
     * plays the move.
     * @return a random integer.
     */
    private Integer getSecretToken() {
        return random.nextInt();
    }

    /**
     * Notifies a player with the correct list of valid moves.
     *
     * @param colour the colour of the player to be notified.
     * @param token the secret token for the move.
     */
    private void notifyPlayer(Colour colour, Integer token) {
		PlayerData player = getPlayerData(colour);
        player.getPlayer().notify(player.getLocation(), validMoves(colour), token, this);
    }

    /**
     * Passes priority onto the next player whose turn it is to play.
     */
    protected void nextPlayer() {
        int newIndex = 0;
        for (int i = 0; i < playersInGame.size(); ++i) {
            if (playersInGame.get(i).getColour() == currentPlayer) {
                newIndex = (i + 1) % playersInGame.size();
                break;
            }
        }
        currentPlayer = playersInGame.get(newIndex).getColour();
    }

    /**
     * Allows the game to play a given move.
     *
     * @param move the move that is to be played.
     */
    protected void play(Move move) {
        if (move instanceof MoveTicket) play((MoveTicket) move);
        else if (move instanceof MoveDouble) play((MoveDouble) move);
        else if (move instanceof MovePass) play((MovePass) move);
    }

	// Given a colour this function returns player data.
	private PlayerData getPlayerData(Colour colour){
		PlayerData player = playersInGame.get(0);
		for (PlayerData p : playersInGame) {
			if (p.getColour() == colour) {
				player = p;
				break;
			}
		}
		return player;
	}

    /**
     * Plays a MoveTicket.
     *
     * @param move the MoveTicket to play.
     */
    protected void play(MoveTicket move) {
        Colour colour = move.colour;
		Ticket ticket = move.ticket;
		int target = move.target;
		if (getCurrentPlayer() == colour) {
			PlayerData player = getPlayerData(colour);
			player.removeTicket(ticket);
			player.setLocation(target);
			if (colour != Colour.Black) {
				PlayerData mrX = getPlayerData(Colour.Black);
				mrX.addTicket(ticket);
			} else {
				currentRound++;
			}
		}
    }

    /**
     * Plays a MoveDouble.
     *
     * @param move the MoveDouble to play.
     */
    protected void play(MoveDouble move) {
        MoveTicket move1 = move.move1;
		MoveTicket move2 = move.move2;
		play(move1);
		play(move2);
    }

    /**
     * Plays a MovePass.
     *
     * @param move the MovePass to play.
     */
    protected void play(MovePass move) {
		Colour playerColour = move.colour;
		if (playerColour == Colour.Black) {
			currentRound++;
		}
    }

    /**
     * Returns the list of valid moves for a given player.
     *
     * @param player the player whose moves we want to see.
     * @return the list of valid moves for a given player.
     */
     public List<Move> validMoves(Colour player) {
        List<Move> listOfValidMoves = new ArrayList<Move>();
        PlayerData playerAux = getPlayerData(player);
        generateMoves(playerAux, listOfValidMoves);
        if(listOfValidMoves.size() == 0 && player != Colour.Black){
            listOfValidMoves.add(MovePass.instance(player));
        }
        return listOfValidMoves;

    }


    private void generateMoves(PlayerData playerAux, List<Move> listOfValidMoves) {
        boolean flagUnreachable = false;
        Node<Integer> nodeLocation = graph.getNode(playerAux.getLocation());
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            flagUnreachable = false;
            for (PlayerData p : playersInGame) {
                // Occupied node (if the player is not mrX and one of
                // the detective is on the destination node then mark it as unreachable)
                if (p.getColour() != Colour.Black && p.getLocation() == (Integer) e.getTarget().getIndex()) {
                    flagUnreachable = true;
                    break;
                }
            }
            // No tickets (if a player has no tickers to get to the destination node
            // then mark it as unreachable)
            if (playerAux.getTickets().get(Ticket.fromTransport(e.getData())) == 0){
                flagUnreachable = true;
            }

            if (flagUnreachable == false) {
                MoveTicket ticket1 = MoveTicket.instance(playerAux.getColour(), Ticket.fromTransport(e.getData()), e.getTarget().getIndex());
                listOfValidMoves.add(ticket1);
                if (playerAux.getColour() == Colour.Black && playerAux.getTickets().get(Ticket.Double) > 0) {
						playerAux.removeTicket(Ticket.fromTransport(e.getData()));
                        mrXDoubleMoves(playerAux, e, ticket1, listOfValidMoves);
						playerAux.addTicket(Ticket.fromTransport(e.getData()));
                }
            }

            if (flagUnreachable == false && playerAux.getColour() == Colour.Black && playerAux.getTickets().get(Ticket.Secret) != 0) {
                MoveTicket ticket1 = MoveTicket.instance(playerAux.getColour(), Ticket.Secret, e.getTarget().getIndex());
                listOfValidMoves.add(ticket1);
                if (playerAux.getTickets().get(Ticket.Double) > 0) {
					playerAux.removeTicket(Ticket.fromTransport(e.getData()));
                    mrXDoubleMoves(playerAux, e, ticket1, listOfValidMoves);
					playerAux.addTicket(Ticket.fromTransport(e.getData()));
                }
            }
        }
    }

    private void mrXDoubleMoves(PlayerData playerAux, Edge previousEdge, MoveTicket ticket1, List<Move> listOfValidMoves) {
        boolean flagUnreachable = false;
        Integer middle = (Integer) previousEdge.getTarget().getIndex();
        Node<Integer> nodeLocation = graph.getNode(middle);
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            flagUnreachable = false;
            for (PlayerData p : playersInGame) {
                // Occupied node (if the player is not mrX and one of
                // the detective is on the destination node then mark it as unreachable)
                if (p.getColour() != Colour.Black && p.getLocation() == (Integer) e.getTarget().getIndex()) {
                    flagUnreachable = true;
                    break;
                }
            }
            // No tickets (if a player has no tickers to get to the destination node
            // then mark it as unreachable)
            if (playerAux.getTickets().get(Ticket.fromTransport(e.getData())) == 0){
                flagUnreachable = true;
            }

            // Previous ticket + previous with normal ticket
            if (flagUnreachable == false) {
                MoveTicket ticket2 = MoveTicket.instance(playerAux.getColour(), Ticket.fromTransport(e.getData()), e.getTarget().getIndex());
                listOfValidMoves.add(MoveDouble.instance(playerAux.getColour(), ticket1, ticket2));
            }
            // Previous ticket + ticket with secret move
            if (flagUnreachable == false && playerAux.getColour() == Colour.Black && playerAux.getTickets().get(Ticket.Secret) != 0) {
                MoveTicket ticket2 = MoveTicket.instance(playerAux.getColour(), Ticket.Secret, e.getTarget().getIndex());
                listOfValidMoves.add(MoveDouble.instance(playerAux.getColour(), ticket1, ticket2));
            }
        }
    }

    /**
     * Allows spectators to join the game. They can only observe as if they
     * were a detective: only MrX's revealed locations can be seen.
     *
     * @param spectator the spectator that wants to be notified when a move is made.
     */
    public void spectate(Spectator spectator) {
        //TODO:
    }

    /**
     * Allows players to join the game with a given starting state. When the
     * last player has joined, the game must ensure that the first player to play is Mr X.
     *
     * @param player the player that wants to be notified when he must make moves.
     * @param colour the colour of the player.
     * @param location the starting location of the player.
     * @param tickets the starting tickets for that player.
     * @return true if the player has joined successfully.
     */
    public boolean join(Player player, Colour colour, int location, Map<Ticket, Integer> tickets) {
        for (PlayerData p : playersInGame) {
            if (colour == p.getColour()) return false;
        }
        PlayerData joinPlayer = new PlayerData(player, colour, location, tickets);
        playersInGame.add(joinPlayer);
        return true;
    }

    /**
     * A list of the colours of players who are playing the game in the initial order of play.
     * The length of this list should be the number of players that are playing,
     * the first element should be Colour.Black, since Mr X always starts.
     *
     * @return The list of players.
     */
    public List<Colour> getPlayers() {
        List<Colour> playerColours = new ArrayList<Colour>();
        for (PlayerData p : playersInGame) {
            playerColours.add(p.getColour());
        }
        return playerColours;
    }

    /**
     * Returns the colours of the winning players. If Mr X it should contain a single
     * colour, else it should send the list of detective colours
     *
     * @return A set containing the colours of the winning players
     */
    public Set<Colour> getWinningPlayers() {
        //TODO:
        return new HashSet<Colour>();
    }

    /**
     * The location of a player with a given colour in its last known location.
     *
     * @param colour The colour of the player whose location is requested.
     * @return The location of the player whose location is requested.
     * If Black, then this returns 0 if MrX has never been revealed,
     * otherwise returns the location of MrX in his last known location.
     * MrX is revealed in round n when {@code rounds.get(n)} is true.
     */
    public int getPlayerLocation(Colour colour) {
		int mrXLastLocation = 0;
		PlayerData player = getPlayerData(colour);
		int playerLocation = player.getLocation();
        // Mr X's last available position
        if (colour == Colour.Black) {
            if (rounds.get(currentRound) == false) {
                return mrXLastLocation;
            } else {
                mrXLastLocation = playerLocation;
                return mrXLastLocation;
            }
        }
        return playerLocation;
    }

    /**
     * The number of a particular ticket that a player with a specified colour has.
     *
     * @param colour The colour of the player whose tickets are requested.
     * @param ticket The type of tickets that is being requested.
     * @return The number of tickets of the given player.
     */
    public int getPlayerTickets(Colour colour, Ticket ticket) {
		PlayerData player = getPlayerData(colour);
        Integer tickets = player.getTickets().get(ticket);
        return tickets;
    }

    /**
     * The game is over when MrX has been found or the agents are out of
     * tickets. See the rules for other conditions.
     *
     * @return true when the game is over, false otherwise.
     */
    public boolean isGameOver() {
        //TODO:
        return false;
    }

    /**
     * A game is ready when all the required players have joined.
     *
     * @return true when the game is ready to be played, false otherwise.
     */
    public boolean isReady() {
        if (numberOfDetectives + 1 == playersInGame.size())
            return true;
        return false;
    }

    /**
     * The player whose turn it is.
     *
     * @return The colour of the current player.
     */
    public Colour getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * The round number is determined by the number of moves MrX has played.
     * Initially this value is 0, and is incremented for each move MrX makes.
     * A double move counts as two moves.
     *
     * @return the number of moves MrX has played.
     */
    public int getRound() {
        return currentRound;
    }

    /**
     * A list whose length-1 is the maximum number of moves that MrX can play in a game.
     * The getRounds().get(n) is true when MrX reveals the target location of move n,
     * and is false otherwise.
     * Thus, if getRounds().get(0) is true, then the starting location of MrX is revealed.
     *
     * @return a list of booleans that indicate the turns where MrX reveals himself.
     */
    public List<Boolean> getRounds() {
        return rounds;
    }



}
