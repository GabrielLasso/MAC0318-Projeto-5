import lejos.geom.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class B {
  static Graph mapa;

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
          G.addEdge(i,j, distSquare(p1, p2));
        }
      }
    }
    return G;
  }

  private static double distSquare (Point p1, Point p2) {
    return (p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y);
  }

  public static void main (String[] args) {
    Graph mapa = criaGrafo();
  }

}
