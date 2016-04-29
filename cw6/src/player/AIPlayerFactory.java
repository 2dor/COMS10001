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

    @Override
    public Player getPlayer(Colour colour, ScotlandYardView view, String mapFilename) {
        //TODO: Update this with your AI implementation.
        System.out.println(mapFilename);
        distances = new int[201][201];
        generatedMoves = new int[201][501];
        //testEfficiency.test2();
        this.view = view;
        ready();
        //testEfficiency.test1();
        System.out.println("Creating " + colour + " random player.\n");
        AIPlayer aiPlayer = new AIPlayer(view, mapFilename, colour, distances, generatedMoves);
        addSpectator(aiPlayer);
        return aiPlayer;
    }
    /*
     * parses the InputStream and returns an int
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

    @Override
    public void ready() {
        //TODO: Any code you need to execute when the game starts, put here.
        this.graphFilename = "graph.txt";
        //File file = new File("lookup-node-rank.txt");
        File file = new File("lookup-node-distances.txt");
        try {
            //Scanner reader = new Scanner(file);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("lookup-node-distances.txt"));
            for (int source = 1; source < 200; ++source) {
                System.out.println("Reading source: " + source);
                for (int destination = 1; destination < 200; ++destination) {
                    distances[source][destination] = readInt(bis);//reader.nextInt();
                }
            }
        } catch (IOException e) {
            System.out.println("Error:\nError creating Scanner.");
        }
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
        //     }
        // } catch (IOException e) {
        //     System.out.println("\nError caught while creating PrintWriter");
        // }
        file = new File("generated-moves.txt");
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            //distances = new int[201][201];
            this.graph = makeGraph(graphFilename);
            generateMoves();
        } catch (IOException e) {
            System.out.println("\nError caught while creating PrintWriter");
        }
        /* FIRST  */
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

    // Takes an array and an element as parameter and adds the element
    // to the array.
    private void addElementToArray(int[] a, int e) {
       a[ a[0] + 1 ] = e;
       ++a[0];
    }

    private ScotlandYardGraph makeGraph(String graphFilename) {
        ScotlandYardGraphReader graphRead = new ScotlandYardGraphReader();
        ScotlandYardGraph graph = new ScotlandYardGraph();
        try {
            graph = graphRead.readGraph(graphFilename);
        } catch(IOException e) {
            //TODO maybe: handle exception
        }
        return graph;
    }

    // Generates all possible moves(encoded form) for a player given a location.
    private void generateMoves() {
        for (int location = 1; location < 200; ++location) {
            System.out.println("Generating moves for location: " + location);
            generatedMoves[location][0] = 0;
            int player = encodeColour(Colour.Black);
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
               addElementToArray(generatedMoves[location], normalMove);
        	   secretMove = player + secretTicket + e.getTarget().getIndex();
        	   addElementToArray(generatedMoves[location], secretMove);
        	   generateDoubleMoves(player, e, normalMove, generatedMoves[location]);
        	   generateDoubleMoves(player, e, secretMove, generatedMoves[location]);
            }
        }
        // System.out.println("All generated moves: ");
        // for (int i = 0; i < generatedMoves[0]; ++i) {
        //     System.out.print(generatedMoves[i] + " ");
        // }
        // System.out.println("");
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

    private void addSpectator(Spectator aiPlayer) {
        spectators.add(aiPlayer);
    }

    @Override
    public List<Spectator> getSpectators(ScotlandYardView view) {
        //TODO: Add your AI here if you want it to be a spectator.
        return spectators;
    }

    @Override
    public void finish() {
        //TODO: Any code you need to execute when the game ends, put here.
    }

}
