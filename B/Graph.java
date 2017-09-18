import java.util.ArrayList;
import java.util.LinkedList;

public class Graph {
  private ArrayList<LinkedList<Edge>> adj;
  private int V;
  private int E;

  public Graph (int V) {
    this.V = V;
    this.E = 0;
    this.adj = new ArrayList<LinkedList<Edge>>();
    for (int i = 0; i < V; i++) {
      this.adj.add(new LinkedList<Edge>());
    }
  }

  public int V() {
    return V;
  }

  public int E() {
    return E;
  }

  public void addEdge (int v, int w, double weight) {
    Edge e = new Edge (v, w, weight);
    E++;
    adj.get(v).add(e);
    adj.get(w).add(e.reverse());
  }

  // Devolve a lista dos nós adjacentes a v
  public LinkedList<Edge> adj(int v) {
    return this.adj.get(v);
  }

  // Devolve a ShortestPathTree de um nó s representada em um vetor
  public int[] spt (int s) {
    int[] spt = new int[V];
    boolean explored[] = new boolean[V];
    double distTo[] = new double[V];
    IndexMinPQ<Double> q;
    q = new IndexMinPQ<Double>(V);

    for (int v = 0; v < V; v++) {
      explored[v] = false;
      distTo[v] = Double.POSITIVE_INFINITY;
    }
    explored[s] = true;
    distTo[s] = 0;
    spt[s] = s;

    q.insert (s, (double)0);

    while (q.size() != 0) {
      int v = q.delMin();
      explored[v] = true;
      for (Edge e : this.adj(v)) {
        int w = e.w();
        if (!explored[w]) {
          if (distTo[w] > distTo[v] + e.weight()) {
            distTo[w] = distTo[v] + e.weight();
            spt[w] = v;
            if (q.contains(w)) {
              q.decreaseKey (w, distTo[w]);
            }
            else {
              q.insert (w, distTo[w]);
            }
          }
        }
      }
    }
    return spt;
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(V + " vertices, " + E + " edges " + "\n");
    for (int v = 0; v < V; v++) {
        s.append(v + ": ");
        for (Edge e : adj.get(v)) {
            int w = e.w;
            s.append(w + " ");
        }
        s.append("\n");
    }
    return s.toString();
}

  public class Edge {
    int v;
    int w;
    double weight;

    public Edge (int v, int w, double weight) {
      this.v = v;
      this.w = w;
      this.weight = weight;
    }

    public int v () {
      return v;
    }

    public int w () {
      return w;
    }

    public double weight () {
      return weight;
    }

    public Edge reverse () {
      return new Edge (w, v, weight);
    }
  }
}
