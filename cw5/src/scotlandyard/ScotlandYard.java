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
    private List<Spectator> spectatorsInGame;
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

    /**
     * Given a player's colour, returns player data.
     *
     * @param colour the colour of the player.
     * @return player's data with corresponding colour.
     */
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
		PlayerData player = getPlayerData(colour);
		player.removeTicket(ticket);
		player.setLocation(target);
        //Give ticket to mrX
		if (colour != Colour.Black) {
			PlayerData mrX = getPlayerData(Colour.Black);
			mrX.addTicket(ticket);
		} else {
			currentRound++;
		}
    }

    /**
     * Plays a MoveDouble.
     *
     * @param move the MoveDouble to play.
     */
    protected void play(MoveDouble move) {
		play(move.move1);
		play(move.move2);
    }

    /**
     * Plays a MovePass.
     *
     * @param move the MovePass to play.
     */
    protected void play(MovePass move) {
        // DONE;
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

    /**
     * Returns true if target node is occupied
     *
     * @param edge The edge we are using to test if the target node is occupied.
     * @return true if target occupied, false otherwise.
     */
    private boolean isOccupied(Edge<Integer, Transport> edge) {
        for (PlayerData p : playersInGame) {
            if (p.getColour() != Colour.Black && p.getLocation() == (Integer) edge.getTarget().getIndex()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds single valid moves to listOfValidMoves, and calls mrXDoubleMoves()
     *
     * @param playerAux current player's data.
     * @param listOfValidMoves list of generated valid moves for current player.
     */
    private void generateMoves(PlayerData playerAux, List<Move> listOfValidMoves) {
        boolean flagUnreachable = false;
        Ticket currentTicket;
        Node<Integer> nodeLocation = graph.getNode(playerAux.getLocation());
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            currentTicket = Ticket.fromTransport(e.getData());
            flagUnreachable = isOccupied(e);
            if (!playerAux.hasTickets(currentTicket)) {
                flagUnreachable = true;
            }
            if (!flagUnreachable) {
                MoveTicket ticketNormal = MoveTicket.instance(playerAux.getColour(), currentTicket, e.getTarget().getIndex());
                listOfValidMoves.add(ticketNormal);
                if (playerAux.getColour() == Colour.Black) {
                    if (playerAux.hasTickets(Ticket.Secret)) {
                        MoveTicket ticketSecret = MoveTicket.instance(playerAux.getColour(), Ticket.Secret, e.getTarget().getIndex());
                        listOfValidMoves.add(ticketSecret);
                        if (playerAux.hasTickets(Ticket.Double)) {
                            playerAux.removeTicket(currentTicket);
                            mrXDoubleMoves(playerAux, e, ticketSecret, listOfValidMoves);
        					playerAux.addTicket(currentTicket);
                        }
                    }
                    if (playerAux.hasTickets(Ticket.Double)) {
                        playerAux.removeTicket(currentTicket);
                        mrXDoubleMoves(playerAux, e, ticketNormal, listOfValidMoves);
                        playerAux.addTicket(currentTicket);
                    }
                }
            }
        }
    }

    /**
     * Adds double valid moves to listOfValidMoves in the case of mrX
     *
     * @param playerAux current player's data.
     * @param previousEdge the edge used in the previous move
     * @param ticketPrevious previous ticket
     * @param listOfValidMoves list of generated valid moves for current player.
     */
    private void mrXDoubleMoves(PlayerData playerAux, Edge previousEdge, MoveTicket ticketPrevious, List<Move> listOfValidMoves) {
        boolean flagUnreachable = false;
        Ticket currentTicket;
        Integer middle = (Integer) previousEdge.getTarget().getIndex();
        Node<Integer> nodeLocation = graph.getNode(middle);
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
            currentTicket = Ticket.fromTransport(e.getData());
            flagUnreachable = isOccupied(e);
            if (!playerAux.hasTickets(currentTicket)){
                flagUnreachable = true;
            }
            if (!flagUnreachable) {
                MoveTicket ticketNormal = MoveTicket.instance(playerAux.getColour(), currentTicket, e.getTarget().getIndex());
                listOfValidMoves.add(MoveDouble.instance(playerAux.getColour(), ticketPrevious, ticketNormal));
                if (playerAux.hasTickets(Ticket.Secret)) {
                    MoveTicket ticketSecret = MoveTicket.instance(playerAux.getColour(), Ticket.Secret, e.getTarget().getIndex());
                    listOfValidMoves.add(MoveDouble.instance(playerAux.getColour(), ticketPrevious, ticketSecret));
                }
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
        spectatorsInGame.add(spectator);
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
        int playerLocation = 0;
		PlayerData player = getPlayerData(colour);
		playerLocation = player.getLocation();
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
    public boolean isGameOver() {/*
        PlayerData mrX = getPlayerData(Colour.Black);
        //Detectives Win!
        for (PlayerData p : playersInGame) {
            if (p.getColour() != Colour.Black && p.getLocation() == mrX.getLocation()) {
                return true;
            }
        }
            //Problem because currentPlayer is notified after we check isGameOver.
            //Suggestion: maybe make a previousPlayer function?
        if (currentPlayer == Colour.Black && listOfValidMoves.size() == 0) {
            return true;
        }
        //mrx Wins!
        if (currentRound == 23) {
            return true;
        }*/
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
