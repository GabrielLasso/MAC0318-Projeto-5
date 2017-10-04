import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lejos.nxt.Button;

public class CustoUniforme {
  static Graph mapa;
  private static final byte ADD_POINT = 0; //adds waypoint to path
	private static final byte TRAVEL_PATH = 1; // enables slave to execute the path
	private static final byte STATUS = 2; // enquires about slave's position
	private static final byte SET_START = 3; // set initial waypoint
	private static final byte STOP = 4; // closes communication

  static final Line[] lines = {
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
  static final Point[] points = {
    new Point(100,813),    /* P1 */
    new Point(426,873),   /* P2 */
    new Point(1140,885),  /* P3 */
    new Point(1117,432),  /* P4 */
    new Point(830,507),   /* P5 */
    new Point(690,571),   /* P6 */
    new Point(450,593),   /* P7 */
    new Point(263,350),   /* P8 */
    new Point(531,382),   /* P9 */
    new Point(986,166),    /* P10 */
    new Point(490,100)     /* P11 */
  };

  private static Graph criaGrafo () {
    int V = points.length;
    Graph G = new Graph(V);
    for (int i = 0; i < V; i++) {
      Point p1 = points[i];
      for (int j = i+1; j < V; j++) {
        Point p2 = points[j];
        boolean passable = true;
        for (int k = 0; k < lines.length; k++) {
          Line p1p2 = new Line (p1.x, p1.y, p2.x, p2.y);
          if (lines[k].intersectsAt(p1p2) != null) {
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
    return Math.sqrt()(p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
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
    int r, s, i, v;
    float ret;
    master.connect();
    mapa = criaGrafo();
    System.out.println("Qual o ponto inicial?");
    r = scan.nextInt() - 1;
    System.out.println("Qual a meta?");
    s = scan.nextInt() - 1;
    ret = master.sendCommand (SET_START, points[r].x/10, points[r].y/10);
    spt = mapa.spt(r);
    path = findPath(spt, s);
    for (i = 1; i < path.length; i++) {
      ret = master.sendCommand(ADD_POINT, points[path[i]].x/10, points[path[i]].y/10);
      System.out.println("ponto: " + (path[i] + 1) + " X: " + points[path[i]].x/10 + " Y: " + points[path[i]].y/10 +" return: " + ret);
    }
    System.out.print("3... 2... 1... ");
    ret = master.sendCommand(TRAVEL_PATH, -1, -1);
    System.out.println("GO!");
    Button.waitForAnyPress();
    master.close();

  }
}
