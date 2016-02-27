import graph.*;

import java.util.*;
import java.lang.*;

public class Prim {

    private UndirectedGraph<Integer, Integer> minimumSpanningTree;

    public Prim(Graph<Integer, Integer> graph) {
        //TODO: Using the passed in graph, implement Prims algorithm in this
        // class.
        minimumSpanningTree = new UndirectedGraph<Integer, Integer>();
        // All edges from the graph
        List<Edge<Integer, Integer>> E = graph.getEdges();
        // All nodes from the graph
        List<Node<Integer>> V = graph.getNodes();
        // Visited nodes
        List<Node<Integer>> W = new ArrayList<Node<Integer>>();
        // Utilised Edges
        List<Edge<Integer, Integer>> F = new ArrayList<Edge<Integer, Integer>>();
        // Reachable Edges
        List<Edge<Integer, Integer>> R = new ArrayList<Edge<Integer, Integer>>();
        // System.out.println(graph.toString());
        Node<Integer> newNode = V.get(0);
        W.add(newNode);
        minimumSpanningTree.add(newNode);
        // bullshit values
        Edge<Integer, Integer> minimum = new Edge<Integer,Integer>(new Node<Integer>(1), new Node<Integer>(2), 0);
        boolean flag = false;
        do {

            for (Edge e : graph.getEdgesFrom(newNode)) {
                flag = false;
                for (Node n : W) {
                    if(e.getTarget() == n){
                        flag = true;
                    }
                }
                if (!flag) {
                    R.add(e);
                }
            }
            minimum = R.get(0);

            for (Edge r : R) {
                System.out.println(r);
                if ((Integer) r.getData() < (Integer) minimum.getData())
                    minimum = r;
            }
            System.out.print("\n");
            System.out.println(minimum.toString());
            System.out.print("\n");
            //System.out.println(minimum.toString());
            W.add(minimum.getTarget());
            // Add node
            minimumSpanningTree.add(minimum.getTarget());
            //System.out.println(minimumSpanningTree.toString());
            // Add edge
            minimumSpanningTree.add(minimum);
            R.remove(minimum);
            newNode = minimum.getTarget();
        } while(W.size() < V.size());
        System.out.println(minimumSpanningTree.toString());
        //System.out.println("\n");
        // System.out.println(R.get(0));
        // System.out.println(R.get(1));
    }

    public Graph<Integer, Integer> getMinimumSpanningTree() {
        //TODO: You should return a new graph that represents the minimum
        // spanning tree of the graph.
        //Graph<Integer, Integer> myGraph = new UndirectedGraph<Integer, Integer>();
        return minimumSpanningTree;
    }

}
