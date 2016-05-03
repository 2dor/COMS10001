import graph.*;

import java.util.*;

public class Dijkstra {

    private final int INF = 0x3f3f3f3f;//roughly 1 billion
    private List<Integer> pathToSource;
    private int[] bestDist;
    private int[] previousNode;
    private List<Edge<Integer, Integer>> E;
    private List<Node<Integer>> V;
    private List<Node<Integer>> W;
    private Node<Integer> originNode;
    private Node<Integer> destinationNode;
    private Graph<Integer, Integer> graph;

    public Dijkstra(Graph<Integer, Integer> graph) {
        //TODO: Using the passed in graph, implement Dijkstras algorithm in this
        // class.
        this.graph = graph;
        pathToSource = new LinkedList<Integer>();
        this.previousNode = new int[1000];
        this.bestDist = new int[1000];
        for (int i = 0; i < 1000; ++i) {
            this.bestDist[i] = INF;
        }
        // All edges from the graph
        this.E = graph.getEdges();
        // All nodes from the graph
        // List<Node<Integer>> V = new ArrayList<Node<Integer>>();
        this.V = graph.getNodes();
        // Visited nodes
        //List<Node<Integer>> W = new ArrayList<Node<Integer>>();
        this.W = new ArrayList<Node<Integer>>();

        System.out.println("Tudor kurva0");
    }

    public List<Integer> shortestPath(Integer origin, Integer destination) {
        //TODO: You should return an ordered list of the node the indecies you
        // visit in your shortest path from origin to destination.
        Node<Integer> currentNode = new Node<Integer>(999);
        for (Node v : V) {
            if (v.getIndex() == origin) {
                currentNode = v;
                break;
            }
        }
        Node<Integer> nextNode = new Node<Integer> (999);
        W.add(currentNode);
        // Set the previous node of the origin to zero such that when we recurse
        // we know that it is the last
        bestDist[origin] = 0;
        int minimumDist = INF;
        while (W.size() != V.size()) {
            for (Edge e : graph.getEdgesFrom(currentNode)) {
                if (bestDist[(int) e.getTarget().getIndex()] > bestDist[(int) e.getSource().getIndex()] + (int) e.getData()) {
                    bestDist[(int) e.getTarget().getIndex()] = bestDist[(int) e.getSource().getIndex()] + (int) e.getData();
                    previousNode[(int) e.getTarget().getIndex() ] = (int) e.getSource().getIndex();
                }
            }
            minimumDist = INF;
            for (Node v : V) {
                for (Node w : W) {
                    if (v.getIndex() == w.getIndex()) {
                        continue;
                    }
                    if (minimumDist > bestDist[(int) v.getIndex()]) {
                        minimumDist = bestDist[(int) v.getIndex()];
                        nextNode = v;
                    }
                }

            }
            currentNode = nextNode;
        }
        //TODO: compute shortest path from origin to destination
        for (Node v : V) {
            if (v.getIndex() == destination) {
                currentNode = v;
                break;
            }
        }
        while (currentNode.getIndex() != origin) {
            pathToSource.add((int) currentNode.getIndex());
            currentNode = graph.getNode(previousNode[(int) currentNode.getIndex()]);
        }
        pathToSource.add((int) currentNode.getIndex());
        Collections.reverse(pathToSource);
        return pathToSource;
    }

}
