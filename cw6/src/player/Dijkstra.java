package player;

import scotlandyard.*;
import graph.*;

import java.util.*;
import java.io.IOException;

/**
 * A class to find the best route to take giving the best possible options
 * to change location as quickly as possible.
 */

public class Dijkstra {

    private PageRank pageRank;
    private Graph<Integer, Transport> graph;
    private List<Node<Integer>> nodes;
    private static int INFINITY = 0x3f3f3f3f;//roughly 1 billion
    /**
     * Constructs a new Dijkstra object.
     *
     * @param graphFilename the path to the file containing the graph data.
     */
    public Dijkstra(String graphFilename) {
        try {

            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            this.graph = graphReader.readGraph(graphFilename);
            nodes = graph.getNodes();
            pageRank = new PageRank(graph);
            pageRank.iterate(100);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Returns the route between two nodes, taking into
     * account player tickets and the rank of nodes.
     *
     * @param start the start location.
     * @param destination the destination location.
     * @param tickets the tickets for each route
     * the player holds.
     * @return the optimal route from start to destination.
     */
    public List<Integer> getRoute(int start, int destination, Map<Transport, Integer> tickets, Colour player) {

        List<Node<Integer>> nodes = graph.getNodes();
        //Initialisation
        Map<Node<Integer>, Integer> unvisitedNodes = new HashMap<Node<Integer>, Integer>();
        Map<Node<Integer>, Integer> distances = new HashMap<Node<Integer>, Integer>();
        Map<Node<Integer>, Node<Integer>> previousNodes = new HashMap<Node<Integer>, Node<Integer>>();
        Node<Integer> currentNode = graph.getNode(start);
        //Map<Node<Integer>, Map<Transport, Integer>> ticketsAtNode = new HashMap<Node<Integer>, HashMap<Transport, Integer>>();
        /* Initialise source with distance 0.0 and the rest of the nodes
         * with distance POSITIVE_INFINITY
         * Initialise unvisitedNodes with their respective PageRank
         */
        for (Node<Integer> node : nodes) {
            if (!currentNode.getIndex().equals(node.getIndex())) {
                distances.put(node, INFINITY);
                //ticketsAtNode.put(node, new HashMap<Transport, Integer>());
            } else {
                distances.put(node, 0);
                //ticketsAtNode.put(node, new HashMap<Transport, Integer>(tickets));
            }
            Integer location = node.getIndex();
            try {
                //unvisitedNodes.put(node, (1/pageRank.getPageRank(location)));
                unvisitedNodes.put(node, distances.get(node));
            } catch (Exception e) {
                System.err.println(e);
            }
            previousNodes.put(node, null);
        }
        //Search through the graph
        while (unvisitedNodes.size() > 0) {
            //TODO: modify minDistance() so it does not take the PageRank into account
            Node<Integer> m = minDistance(distances, unvisitedNodes);
            if (m == null) break;
            currentNode = m;
            /* Stop when we reach the destination */
            if (currentNode.getIndex().equals(destination)) {
                // System.out.println("Coming to destination " + destination + " from " + previousNodes.get(currentNode));
                break;
            }
            unvisitedNodes.remove(currentNode);

            step(graph, distances, unvisitedNodes, currentNode, previousNodes, tickets, player);
        }

        //Move backwards finding the shortest route
        List<Integer> route = new ArrayList<Integer>();
        while (previousNodes.get(currentNode) != null) {
            route.add(0, currentNode.getIndex());
            currentNode = previousNodes.get(currentNode);
        }
        route.add(0, currentNode.getIndex());
        return route;
    }

    // Perform a step in Dijkstra's algorithm.
    // @param graph the Graph containing all nodes.
    // @param distances all calculated distances.
    // @param unvisitedNodes set of unvisited nodes.
    // @param currentNode the node we are currently
    // looking at.
    // @param previousNodes map of nodes to the node
    // that they moved from.
    // @param tickets the player tickets for different
    // routes.
    private void step(Graph<Integer, Transport> graph, Map<Node<Integer>, Integer> distances,
                      Map<Node<Integer>, Integer> unvisitedNodes,
                      Node<Integer> currentNode,
                      Map<Node<Integer>, Node<Integer>> previousNodes,
                      Map<Transport, Integer> tickets,
					  Colour player) {
        List<Edge<Integer, Transport>> edges = graph.getEdgesFrom(currentNode);
        Integer currentDistance = distances.get(currentNode);
		// System.out.println("Current node " + currentNode.getIndex() + " with distance " + currentDistance);
        for (Edge<Integer, Transport> e : edges) {
            //For all neighbours
            Node<Integer> neighbour = e.getTarget();
            /* if the neighbour has NOT been visited yet*/
            if (unvisitedNodes.get(neighbour) != null) {
                Transport route = e.getData();
				if (player != Colour.Black && route == Transport.Boat) continue; // Detectives cannot use boats
				//System.out.println(e.getData());
                //Integer numTickets = ticketsAtNode.get(neighbour).get(route);
                //Update distances
				//System.out.println(pageRank.getPageRank(neighbour.getIndex()));
				//System.out.println(numTickets);
				//System.out.println(currentDistance);
                Integer tentativeDistance = currentDistance + 1;
                if (tentativeDistance < distances.get(neighbour)) {
                    // System.out.println("updated distance for neighbour " + neighbour.getIndex() + " to " + tentativeDistance);
                    distances.put(neighbour, tentativeDistance);
                    previousNodes.put(neighbour, currentNode);
                }
            }
        }
    }

    // Returns the node with the minimum distance from
    // the set of unvisited nodes.
    // @param distances the current distances.
    // @param unvisitedNodes the nodes that have yet to be visited.
    // @return the minimum distance for all unvisited nodes.
    private Node<Integer> minDistance(Map<Node<Integer>, Integer> distances, Map<Node<Integer>, Integer> unvisitedNodes) {
        Integer min = INFINITY;

        Node<Integer> minNode = null;
        for (Map.Entry<Node<Integer>, Integer> entry : distances.entrySet()) {
            Integer d = entry.getValue();
            /* if this node's distance is smaller than the minimum distance
             * so far AND it is an unvisited node, save it
             */
            if (min > d && unvisitedNodes.containsKey(entry.getKey())) {
                min = d;
                minNode = entry.getKey();
            }
        }
        return minNode;
    }

}
