package player;

import net.*;
import scotlandyard.*;

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

    private int distancesByTickets[][][][][];
    private String graphFilename;
    private List<Spectator> spectators = new ArrayList<Spectator>();

    @Override
    public Player getPlayer(Colour colour, ScotlandYardView view, String mapFilename) {
        //TODO: Update this with your AI implementation.
        System.out.println(mapFilename);
        distancesByTickets = new int[201][201][12][9][5];
        testEfficiency.test2();
        ready();
        //testEfficiency.test1();
        System.out.println("Creating " + colour + " random player.\n");
        AIPlayer aiPlayer = new AIPlayer(view, mapFilename, colour, distancesByTickets);
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
        File file = new File("lookup-node-rank.txt");

        try {
            //Scanner reader = new Scanner(file);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("lookup-node-rank.txt"));
            for (int source = 1; source < 200; ++source) {
                System.out.println("Reading source: " + source);
                for (int destination = 1; destination < 200; ++destination) {
                    for (int taxiTickets = 0; taxiTickets < 12; ++taxiTickets) {
                        for (int busTickets = 0; busTickets < 9; ++busTickets) {
                            for (int ugTickets = 0; ugTickets < 5; ++ugTickets) {
                                distancesByTickets[source][destination][taxiTickets][busTickets][ugTickets]
                                    = readInt(bis);//reader.nextInt();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error:\nError creating Scanner.");
        }
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
