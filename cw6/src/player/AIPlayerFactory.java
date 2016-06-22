package player;

import net.*;
import scotlandyard.*;
import graph.*;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The RandomPlayerFactory is an example of a PlayerFactory that
 * gives the AI server your AI implementation. You can also put any
 * code that you want to run before and after a game in the methods
 * provided here.
 */
public class AIPlayerFactory implements PlayerFactory {

    private String graphFilename;
    private List<Spectator> spectators = new ArrayList<Spectator>();
    private ScotlandYardView view;
    private ScotlandYardGraph graph;
    private int distances[][];
    private int generatedMoves[][];
    private int onlyTaxiLinks[];

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

    /**
     * Returns an AIPlayer for Mr. X and adds it to the list of spectators.
     *
     * @param colour the colour of the AIPlayer to be instantiated.
     * @param view the ScotlandYardView that provides game information.
     * @param graphFilename the name of the file containing the graph components.
     * @return an AIPlayer.
     */
    @Override
    public Player getPlayer(Colour colour, ScotlandYardView view, String mapFilename) {
        System.out.println(mapFilename);
        onlyTaxiLinks = new int[200];
        distances = new int[201][201];
        generatedMoves = new int[201][501];
        this.view = view;
        ready();
        gettingTaxiLinks();
        System.out.println("Creating " + colour + " intelligent player.\n");
        AIPlayer aiPlayer = new AIPlayer(view, mapFilename, colour, distances, generatedMoves, onlyTaxiLinks);
        addSpectator(aiPlayer);
        return aiPlayer;
    }
    /*
     * Parses the InputStream and returns an int
     *
     * @param in an InputStream
     * @return a parsed int
     */
    private static int readInt(InputStream in) throws IOException {
        int result = 0;
        boolean flagDigit = false;
        int c = 0;
        while ((c = in.read()) != -1) {
            if ('0' <= c && c <= '9') {
                flagDigit = true;
                result = result * 10 + (c - '0');
            } else {
                if (flagDigit)
                    break;
            }
        }

        return result;
    }

    /*
     * Used to read the precomputed distances and generated moves.
     */
    @Override
    public void ready() {
        this.graphFilename = "graph.txt";
        //File file = new File("lookup-node-rank.txt");
        File file = new File("lookup-node-distances.txt");
        // try {
        //     PrintWriter writer = new PrintWriter(file, "UTF-8");
        //     distances = new int[201][201];
        //     Dijkstra dijkstra = new Dijkstra(graphFilename);
        //     List<Integer> route = new ArrayList<Integer>();
        //     Map<Transport, Integer> tickets = new HashMap<Transport, Integer>();
        //     for (int source = 1; source < 200; ++source) {
        //         System.out.println("Computing Source: " + source);
        //         //writer.println("Source " + source);
        //         writer.print("\n");
        //         for (int destination = 1; destination < 200; ++destination) {
        // 			route = dijkstra.getRoute(source, destination, tickets, Colour.Blue);
        // 			distances[source][destination]
        //                 = route.size() - 1;
        //             writer.print(distances[source][destination]);
        //             writer.print(" ");
        //         }
        //         // if (source > 150) {
        //         //     try {
        //         //         Thread.sleep(1000);
        //         //     } catch(InterruptedException ex) {
        //         //         Thread.currentThread().interrupt();
        //         //     }
        //         // }
        //     }
        //     writer.flush();
        //     writer.close();
        // } catch (IOException e) {
        //     try {
        //         Thread.sleep(3000);
        //     } catch(InterruptedException ex) {
        //         Thread.currentThread().interrupt();
        //     }
        //     System.out.println("\nError caught while creating PrintWriter");
        // }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("lookup-node-distances.txt"));
            for (int source = 1; source < 200; ++source) {
                for (int destination = 1; destination < 200; ++destination) {
                    distances[source][destination] = readInt(bis);/* reader.nextInt(); */
                }
            }
        } catch (IOException e) {
            try {
                Thread.sleep(3000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Error:\nError creating Scanner.");
        }
        file = new File("generated-moves.txt");
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            this.graph = makeGraph(graphFilename);
            generateMoves();
        } catch (IOException e) {
            try {
                Thread.sleep(3000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            System.out.println("\nError caught while creating PrintWriter");
        }
        /* FIRST VERSION OF DISTANCES BY TICKETS */
        /*try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            distancesByTickets = new int[201][201][12][9][5];
            // from 3 to 10 with 6 taxi, 3 bus and 1 underground distancesByTickets[3][10][6][3][1]
            Dijkstra dijkstra = new Dijkstra(graphFilename);
            List<Integer> route = new ArrayList<Integer>();
            Map<Transport, Integer> tickets = new HashMap<Transport, Integer>();
            for (int source = 1; source < 200; ++source) {
                System.out.println("Computing Source: " + source);
                //writer.println("Source " + source);
                writer.print("\n\n\n\n\n\n");
                for (int destination = 1; destination < 200; ++destination) {
                    //writer.println("\n\n\nFrom " + source + " to " + destination);
                    writer.print("\n\n");
                    for (int taxiTickets = 0; taxiTickets < 12; ++taxiTickets) {
                        //writer.println("");
                        for (int busTickets = 0; busTickets < 9; ++busTickets) {
                            //writer.println("");
                            for (int ugTickets = 0; ugTickets < 5; ++ugTickets) {
                                tickets.clear();
                    			tickets.put(Transport.Taxi, taxiTickets);
                                tickets.put(Transport.Bus, busTickets);
                    			tickets.put(Transport.Underground, ugTickets);
                    			route = dijkstra.getRoute(source, destination, tickets, Colour.Blue);
                    			distancesByTickets[source][destination][taxiTickets][busTickets][ugTickets]
                                    = route.size();
                                //writer.println("Using " + taxiTickets + " taxis, " + busTickets + " buses, " + ugTickets + " undergrounds");
                                writer.print(distancesByTickets[source][destination][taxiTickets][busTickets][ugTickets]);
                                writer.print(" ");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("\nError caught while creating PrintWriter");
        }*/
        // try {
        //     //Scanner reader = new Scanner(file);
        //     BufferedInputStream bis = new BufferedInputStream(new FileInputStream("lookup-node-rank.txt"));
        //     for (int source = 1; source < 200; ++source) {
        //         System.out.println("Reading source: " + source);
        //         for (int destination = 1; destination < 200; ++destination) {
        //             for (int taxiTickets = 0; taxiTickets < 12; ++taxiTickets) {
        //                 for (int busTickets = 0; busTickets < 9; ++busTickets) {
        //                     for (int ugTickets = 0; ugTickets < 5; ++ugTickets) {
        //                         distancesByTickets[source][destination][taxiTickets][busTickets][ugTickets]
        //                             = readInt(bis);//reader.nextInt();
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // } catch (IOException e) {
        //     System.out.println("Error:\nError creating Scanner.");
        // }
    }

    /**
     * Adds an element to the end of an array. The zero'th element in the number
     * of components in the array.
     *
     * @param a an array of ints.
     * @param e the element to be added at the end of the array.
     */
    public static void addElementToArray(int[] a, int e) {
       a[ a[0] + 1 ] = e;
       ++a[0];
    }

    /**
     * Initialises the simulator.
     *
     * @param graphFilename the name of the file containing the graph components.
     * @return a ScotlandYardGraph generated from a local file.
     */
    private ScotlandYardGraph makeGraph(String graphFilename) {
        ScotlandYardGraphReader graphRead = new ScotlandYardGraphReader();
        ScotlandYardGraph graph = new ScotlandYardGraph();
        try {
            graph = graphRead.readGraph(graphFilename);
        } catch(IOException e) {
            System.out.println("Error creating ScotlandYardGraph from file.");
        }
        return graph;
    }

    /**
     * Generates all the moves for all positions on the map and stores them in
     * a two-dimensional array.
     *
     */
    private void generateMoves() {
        for (int location = 1; location < 200; ++location) {
            // System.out.println("Generating moves for location: " + location);
            generatedMoves[location][0] = 0;
            int player = encodeColour(Colour.Black);
            int normalTicket = 0;
            int secretTicket = encodeTicket(Ticket.Secret);
            int normalMove = 0;
            int secretMove = 0;
            Node<Integer> nodeLocation = graph.getNode(location);
            // System.out.println("\nGenerating moves.");
            for (Edge<Integer, Transport> e : graph.getEdgesFrom(nodeLocation)) {
               normalTicket = encodeTicket(Ticket.fromTransport(e.getData()));
               normalMove = player + normalTicket + e.getTarget().getIndex();
               //System.out.println("Adding normalMove: " + normalMove);
               addElementToArray(generatedMoves[location], normalMove);
        	   secretMove = player + secretTicket + e.getTarget().getIndex();
        	   addElementToArray(generatedMoves[location], secretMove);
        	   generateDoubleMoves(player, e, normalMove, generatedMoves[location]);
        	   generateDoubleMoves(player, e, secretMove, generatedMoves[location]);
            }
        }
    }

    /**
     * Updates the onlyTaxiLinks array with 1 if a certain node location contains
     * only taxi links, with 0 otherwise.
     */
    private void gettingTaxiLinks() {
        for (Node<Integer> n : graph.getNodes()) {
            onlyTaxiLinks[n.getIndex()] = 1;
            for (Edge<Integer,Transport> e : graph.getEdgesFrom(n)) {
                if (e.getData() != Transport.Taxi) {
                    onlyTaxiLinks[n.getIndex()] = 0;
                }
            }
        }
    }
    /**
     * Generates all double moves given a player, previous edge and previous move.
     * Double moves are two single moves concatenated.
     *
     * @param player encoded player int.
     * @param previousEdge the previous edge
     * @param movePrevious encoded previous move int
     * @param generatedMoves reference to a 1-dimensional array for a certain location
     */
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
    * Adds a spectator to the list of spectators.
    *
    * @param aiPlayer Spectator to be added.
    */
    private void addSpectator(Spectator aiPlayer) {
        spectators.add(aiPlayer);
    }

    @Override
    public List<Spectator> getSpectators(ScotlandYardView view) {
        return spectators;
    }

    @Override
    public void finish() {
    }

}
