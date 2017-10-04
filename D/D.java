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

  static int dilatacao = 50;
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

  private static void criaParedes () {
    double epsilon = 50.0;
    ArrayList<Line> newLines = new ArrayList<Line> ();
    ArrayList<Line> fecha = new ArrayList<Line> ();
    lines = new ArrayList<Line>();
    for (int i = 0; i < walls.length; i++)
      lines.add (walls[i]);

    for (Line l : lines) {
      int x1 = (int) l.x1;
      int x2 = (int) l.x2;
      int y1 = (int) l.y1;
      int y2 = (int) l.y2;
      double[] normal = {x1-x2, y2-y1};
      double modulo = Math.sqrt ((x1-x2) * (x1-x2) + (y2-y1) * (y2-y1));
      Line newL1 = new Line (x1 + (int)(dilatacao * normal[0]/modulo), y1 + (int)(dilatacao * normal[0]/modulo),
                            x2 + (int)(dilatacao * normal[0]/modulo), y2 + (int)(dilatacao * normal[0]/modulo));
      Line newL2 = new Line (x1 - (int)(dilatacao * normal[0]/modulo), y1 - (int)(dilatacao * normal[0]/modulo),
                            x2 - (int)(dilatacao * normal[0]/modulo), y2 - (int)(dilatacao * normal[0]/modulo));
      newLines.add(newL1);
      newLines.add(newL2);
    }

    for (Line l1 : newLines) {
      for (Line l2 : newLines) {
        Point p11, p12, p21, p22;
        p11 = l1.getP1();
        p12 = l1.getP2();
        p21 = l2.getP1();
        p22 = l2.getP2();
        if (dist (p11, p21) < epsilon) {
          fecha.add (new Line (p11.x, p11.y, p21.x, p21.y));
        }
        if (dist (p11, p22) < epsilon) {
          fecha.add (new Line (p11.x, p11.y, p22.x, p22.y));
        }
        if (dist (p12, p21) < epsilon) {
          fecha.add (new Line (p12.x, p12.y, p21.x, p21.y));
        }

        if (dist (p12, p22) < epsilon) {
          fecha.add (new Line (p12.x, p12.y, p22.x, p22.y));
        }
      }
    }

    for (Line l : newLines)
      lines.add (l);
    for (Line l : fecha)
      lines.add (l);
  }

  private static Graph criaGrafo (Point start, Point goal) {
    Graph G = new Graph(0);
    G.addNode (start);
    G.addNode (goal);

    for (int i = 0; i < lines.size(); i++) {
      Line l1 = lines.get(i);
      for (int j = i+1; j < lines.size(); j++) {
        Line l2 = lines.get(j);
        Point p = l1.intersectsAt (l2);
        if (p != null) {
          G.addNode(p.x, p.y);
          System.out.println (G.V() + " " + p.x + " " + p.y);
        }
      }
    }

    for (int i = 0; i < G.V(); i++) {
      Point p1 = G.getPoint (i);
      for (int j = i+1; j < G.V(); j++) {
        Point p2 = G.getPoint (j);
        boolean passable = true;
        for (int k = 0; k < lines.size(); k++) {
          Line p1p2 = new Line (p1.x, p1.y, p2.x, p2.y);
          if (lines.get(k).intersectsAt(p1p2) != null) {
            passable = false;
          }
        }
        if (passable) {
          System.out.println (p1.x + " " + p1.y + " " + p2.x + " " + p2.y);
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
    int[] path, spt;
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
    ret = master.sendCommand (SET_START, start.x/10, start.y/10);
    spt = mapa.spt(0);
    path = findPath(spt, 1);
    for (i = 1; i < path.length; i++) {
      ret = master.sendCommand(ADD_POINT, mapa.getPoint(path[i]).x/10, mapa.getPoint(path[i]).y/10);
      // System.out.println("ponto: " + (path[i] + 1) + " X: " + points[path[i]].x/10 + " Y: " + points[path[i]].y/10 +" return: " + ret);
    }
    System.out.print("3... 2... 1... ");
    ret = master.sendCommand(TRAVEL_PATH, -1, -1);
    System.out.println("GO!");
    Button.waitForAnyPress();
    master.close();

  }
}
