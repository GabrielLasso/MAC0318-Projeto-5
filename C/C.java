import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ArrayDeque;
// import lejos.nxt.Button;

public class C {
  static boolean[][] mapa;
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

  private static boolean[][] criaMapa (int altura, int largura, int cel_side) {
    boolean[][] map = new boolean[altura/cel_side][largura/cel_side];
    for (int i = 0; i < altura/cel_side; i++) {
      for (int j = 0; j < largura/cel_side; j++) {
        Rectangle rect = new Rectangle (j*cel_side, i*cel_side, cel_side, cel_side);
        map[i][j] = true;
        for (Line l : lines) {
          if (l.intersects(rect)) {
            map[i][j] = false;
          }
        }
      }
    }
    return map;
  }

  private static Pos[] findPath (Pos start, Pos goal) {
    int[][] map = new int[mapa.lenght][mapa[0].lenght];
    ArrayDeque<Pos> q = new ArrayDeque<Pos>();
    for (int i = 0; i < mapa.lenght; i++) {
      for (j = 0; j < mapa[0].lenght; j++) {
        map[i][j] = -1;
      }
    }
    q.addFirst(s);
    while (!q.isEmpty()) {
      Pos pos = q.removeLast();
      int x = pos.x();
      int y = pos.y();
      int dist = map[x][y];
      map[x][y] = dist + 1;
      if (map[x+1][y] == -1) {
        q.addFirst(new Pos(x+1, y));
      }
      if (map[x-1][y] == -1) {
        q.addFirst(new Pos(x-1, y));
      }
      if (map[x][y+1] == -1) {
        q.addFirst(new Pos(x, y+1));
      }
      if (map[x][y-1] == -1) {
        q.addFirst(new Pos(x, y-1));
      }
    }
  }


  public static void main (String[] args) {
    int altura = 916, largura = 1182;
    Pos[] path;
    mapa = criaMapa (altura, largura, 50);
    System.out.println("" + (altura/cel_side) + largura/cel_side + mapa.lenght + mapa[0].lenght);
  }

  public class Pos {
    private int x, y;
    public Pos (int x, int y) {
      this.x = x;
      this.y = y;
    }
    public int x() {
      return x;
    }
    public int y() {
      return y;
    }
  }
}
