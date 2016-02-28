import graph.*;

import java.util.*;

public class Dijkstra {

    private final int INF = 2 * 0x3f3f3f3f;
    private List<Integer> pathToSource;
    private int[] bestDist;
    private int[] previousNode;
    private List<Edge<Integer, Integer>> E;
    private List<Node<Integer>> V;
    private List<Node<Integer>> W;

    public Dijkstra(Graph<Integer, Integer> graph) {
        //TODO: Using the passed in graph, implement Dijkstras algorithm in this
        // class.
        pathToSource = new LinkedList<Integer>();
        int[] previousNode = new int[1000];
        int[] bestDist = new int[1000];
        for (int i = 0; i < 1000; ++i) {
            bestDist[i] = INF;
        }
        // All edges from the graph
        List<Edge<Integer, Integer>> E = graph.getEdges();
        // All nodes from the graph
        List<Node<Integer>> V = graph.getNodes();
        // Visited nodes
        List<Node<Integer>> W = new ArrayList<Node<Integer>>();
    }

    public List<Integer> shortestPath(Integer origin, Integer destination) {
        //TODO: You should return an ordered list of the node the indecies you
        // visit in your shortest path from origin to destination.

        Node<Integer> currentNode = new Node<Integer>();
        Node<Integer> nextNode = new Node<Integer>();
        currentNode = V.getNode(origin);
        W.add(currentNode);
        bestDist[origin] = 0;
        int minimumDist = INF;
        while (W.size() != V.size()) {
            for (Edge e : currentNode.getEdgesFrom()) {
                if (bestDist[e.getTarget().getIndex()] > bestDist[e.getSource().getIndex()] + e.getData()) {
                    bestDist[e.getTarget().getIndex()] = bestDist[e.getSource().getIndex()] + e.getData();
                    previousNode[ e.getTarget().getIndex() ] = e.getSource().getIndex();
                }
            }
            minimumDist = INF;
            for (Node v : V) {
                for (Node w : W) {
                    if (v.getIndex() == w.getIndex()) {
                        continue;
                    }
                    if (minimumDist > bestDist[v.getIndex()]) {
                        minimumDist = bestDist[v.getIndex()];
                        nextNode = v;
                    }
                }

            }
            currentNode = nextNode;
        }
        //TODO: compute shortest path from origin to destination
        currentNode = V.getNode(origin);
        //Node<Integer> destinationNode = ;
        while (currentNode.getIndex() != destination) {
            pathToSource
        }
        return pathToSource;
    }

}
