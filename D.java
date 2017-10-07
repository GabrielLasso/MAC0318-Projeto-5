import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lejos.nxt.Button;

public class D {
  static Graph mapa;
  private static final byte ADD_POINT = 0; //adds waypoint to path
  private static final byte TRAVEL_PATH = 1; // enables slave to execute the path
  private static final byte STATUS = 2; // enquires about slave's position
  private static final byte SET_START = 3; // set initial waypoint
  private static final byte STOP = 4; // closes communication

  static int[] path;
  static int dilatacao = 20;
  static double epsilon = 50.0;
  static ArrayList<Line> visibilityLines;
  static ArrayList<Line> lines;
  static final Line[] walls = {
    /* L-shape polygon */
    new Line(170,437,60,680),
    new Line(60,680,398,800),
    new Line(398,800,450,677),
    new Line(450,677,235,595),
    new Line(235,595,281,472),
    new Line(281,472,170,437),
    /* Triangle */
    new Line(1070,815,770,602),
    new Line(770,602,1060,516),
    new Line(1070,815,1060,516),
    /* Pentagon */
    new Line(335,345,502,155),
    new Line(502,155,700,225),
    new Line(700,225, 725,490),
    new Line(725,490,480,525),
    new Line(480,525,335,345)
  };

  static boolean mesmaLine (Line l1, Line l2) {
    Point p11, p12, p21, p22;
    p11 = l1.getP1();
    p12 = l1.getP2();
    p21 = l2.getP1();
    p22 = l2.getP2();
    if (p11.x == p21.x && p11.y == p21.y && p12.x == p22.x && p12.y == p22.y)
      return true;
    if (p12.x == p21.x && p12.y == p21.y && p11.x == p22.x && p11.y == p22.y)
      return true;
    return false;
  }

  private static void criaParedes () {
    ArrayList<Line> newLines;
    visibilityLines = new ArrayList<Line>();
    lines = new ArrayList<Line>();

    for (Line l : walls) {
      int x1 = (int) l.x1;
      int x2 = (int) l.x2;
      int y1 = (int) l.y1;
      int y2 = (int) l.y2;
      double[] normal = {y2-y1, x1-x2};
      double modulo = Math.sqrt (normal[0]*normal[0] + normal[1]*normal[1]);
      normal[0] = normal[0] * (dilatacao) / (1.1*modulo);
      normal[1] = normal[1] * (dilatacao) / (1.1*modulo);
      Line newL1 = new Line (x1 + (int)normal[0], y1 + (int)normal[1],
                             x2 + (int)normal[0], y2 + (int)normal[1]);
      Line newL2 = new Line (x1 - (int)normal[0], y1 - (int)normal[1],
                             x2 - (int)normal[0], y2 - (int)normal[1]);
      lines.add(newL1);
      lines.add(newL2);
    }

    for (Line l : walls) {
      int x1 = (int) l.x1;
      int x2 = (int) l.x2;
      int y1 = (int) l.y1;
      int y2 = (int) l.y2;
      double[] normal = {y2-y1, x1-x2};
      double modulo = Math.sqrt (normal[0]*normal[0] + normal[1]*normal[1]);
      normal[0] = normal[0] * dilatacao / modulo;
      normal[1] = normal[1] * dilatacao / modulo;
      Line newL1 = new Line (x1 + (int)normal[0], y1 + (int)normal[1],
                             x2 + (int)normal[0], y2 + (int)normal[1]);
      Line newL2 = new Line (x1 - (int)normal[0], y1 - (int)normal[1],
                             x2 - (int)normal[0], y2 - (int)normal[1]);
      visibilityLines.add(newL1);
      visibilityLines.add(newL2);
    }

    newLines = new ArrayList<Line> ();

    for (Line l1 : lines) {
      for (Line l2 : lines) {
        if (!mesmaLine(l1,l2)) {
          Point p11, p12, p21, p22;
          p11 = l1.getP1();
          p12 = l1.getP2();
          p21 = l2.getP1();
          p22 = l2.getP2();
          if (dist (p11, p21) < epsilon) {
            newLines.add (new Line (p11.x, p11.y, p21.x, p21.y));
          }
          if (dist (p11, p22) < epsilon) {
            newLines.add (new Line (p11.x, p11.y, p22.x, p22.y));
          }
          if (dist (p12, p21) < epsilon) {
            newLines.add (new Line (p12.x, p12.y, p21.x, p21.y));
          }
          if (dist (p12, p22) < epsilon) {
            newLines.add (new Line (p12.x, p12.y, p22.x, p22.y));
          }
        }
      }
    }

    for (Line l : newLines)
      lines.add (l);

    newLines = new ArrayList<Line> ();


    for (Line l1 : visibilityLines) {
      for (Line l2 : visibilityLines) {
        if (!mesmaLine(l1,l2)) {
          Point p11, p12, p21, p22;
          p11 = l1.getP1();
          p12 = l1.getP2();
          p21 = l2.getP1();
          p22 = l2.getP2();
          if (dist (p11, p21) < epsilon) {
            newLines.add (new Line (p11.x, p11.y, p21.x, p21.y));
          }
          if (dist (p11, p22) < epsilon) {
            newLines.add (new Line (p11.x, p11.y, p22.x, p22.y));
          }
          if (dist (p12, p21) < epsilon) {
            newLines.add (new Line (p12.x, p12.y, p21.x, p21.y));
          }
          if (dist (p12, p22) < epsilon) {
            newLines.add (new Line (p12.x, p12.y, p22.x, p22.y));
          }
        }
      }
    }

    for (Line l : newLines)
      visibilityLines.add (l);
  }

  private static Graph criaGrafo (Point start, Point goal) {
    Graph G = new Graph(0);
    G.addNode (start);
    G.addNode (goal);

    for (Line l : visibilityLines) {
      Point p1 = l.getP1();
      Point p2 = l.getP2();
      G.addNode(p1.x, p1.y);
      G.addNode(p2.x, p2.y);
    }

    for (int i = 0; i < visibilityLines.size(); i++) {
      Line l1 = visibilityLines.get(i);
      for (int j = i+1; j < visibilityLines.size(); j++) {
        Line l2 = visibilityLines.get(j);
        Point p = l1.intersectsAt (l2);
        if (p != null) {
          boolean add = true;
          for (int k = 0; k < G.V(); k++) {
            if (dist (G.getPoint(k), p) < 10)
              add = false;
          }
          if (add)
            G.addNode(p.x, p.y);
        }
      }
    }

    for (int i = 0; i < G.V(); i++) {
      Point p1 = G.getPoint (i);
      for (int j = i+1; j < G.V(); j++) {
        Point p2 = G.getPoint (j);
        boolean passable = true;
        for (Line l : lines) {
          Line p1p2 = new Line (p1.x, p1.y, p2.x, p2.y);
          Point inter = l.intersectsAt(p1p2);
          if ((inter != null)){
            passable = false;
          }
        }
        if (passable) {
          G.addEdge(i,j, dist(p1, p2));
        }
      }
    }
    return G;
  }

  private static double dist (Point p1, Point p2) {
    return Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
  }

  public static int[] findPath (int[] spt, int w) {
    int[] path_rev = new int[mapa.V()], path;
    int j, i = 0, v;
    for (v = w; v != spt[v]; v = spt[v]) {
      path_rev[i] = v;
      i++;
    }
    path = new int[i+1];
    for (j = i; i >= 0; i--) {
      path[i] = path_rev[j-i];
    }
    return path;
  }

  public static void main (String[] args) {
    MasterNav master = new MasterNav();
    Scanner scan = new Scanner( System.in );
    int[] spt;
    int i, v, x, y;
    Point start, goal;
    float ret;
    master.connect();
    System.out.println("Qual a posição X inicial (em mm)?");
    x = scan.nextInt();
    System.out.println("Qual a posição Y inicial (em mm)?");
    y = scan.nextInt();
    start = new Point (x, y);
    System.out.println("Qual a posição X final (em mm)?");
    x = scan.nextInt();
    System.out.println("Qual a posição Y final (em mm)?");
    y = scan.nextInt();
    goal = new Point (x, y);
    criaParedes();
    mapa = criaGrafo(start, goal);
    spt = mapa.spt(0);
    path = findPath(spt, 1);
    master.sendCommand (SET_START, start.x/10f, start.y/10f);
    for (i = 0; i < path.length; i++) {
      master.sendCommand(ADD_POINT, mapa.getPoint(path[i]).x/10f, mapa.getPoint(path[i]).y/10f);
    }
    master.sendCommand(TRAVEL_PATH, -1, -1);
    desenha();
    master.close();
  }
  static void desenha () {
    int i;
    StdDraw.setCanvasSize(512, 500);
    StdDraw.setXscale(0.0, 1182.0);
    StdDraw.setYscale(0.0, 916.0);

    for (Line l : visibilityLines) {
      StdDraw.line (l.x1, l.y1, l.x2, l.y2);
    }

    StdDraw.setPenColor (StdDraw.RED);

    for (Line l : lines) {
      StdDraw.line (l.x1, l.y1, l.x2, l.y2);
    }

    StdDraw.setPenColor (StdDraw.GRAY);

    for (i = 0; i < mapa.V(); i++) {
      for (Graph.Edge e : mapa.adj(i)) {
        Point p1, p2;
        p1 = mapa.getPoint (e.v());
        p2 = mapa.getPoint (e.w());
        StdDraw.line (p1.x, p1.y, p2.x, p2.y);
      }
    }

    StdDraw.setPenColor (StdDraw.BLUE);

    for (i = 1; i < path.length; i++) {
      Point p1, p2;
      p1 = mapa.getPoint(path[i]);
      p2 = mapa.getPoint(path[i-1]);
      StdDraw.line (p1.x, p1.y, p2.x, p2.y);
    }
  }
}
