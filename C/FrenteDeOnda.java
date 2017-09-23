import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ArrayDeque;
import lejos.nxt.Button;

public class FrenteDeOnda {
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

  private static LinkedList<Pos> findPath (Pos start, Pos goal) {
    int[][] map = new int[mapa.lenght][mapa[0].lenght];
    ArrayDeque<Pos> q = new ArrayDeque<Pos>();
    LinkedList<Pos> path;
    for (int i = 0; i < mapa.lenght; i++) {
      for (j = 0; j < mapa[0].lenght; j++) {
        map[i][j] = -1;
      }
    }
    /* Busca em largura */
    q.addFirst(start);
    while (!q.isEmpty()) {
      Pos pos = q.removeLast();
      int x = pos.x();
      int y = pos.y();
      int dist = map[x][y];
      map[x][y] = dist + 1;
      if (x+1 < mapa.lenght && map[x+1][y] == -1) {
        q.addFirst(new Pos(x+1, y));
      }
      if (x > 0 && map[x-1][y] == -1) {
        q.addFirst(new Pos(x-1, y));
      }
      if (y+1 < mapa[0].lenght && map[x][y+1] == -1) {
        q.addFirst(new Pos(x, y+1));
      }
      if (y > 0 && map[x][y-1] == -1) {
        q.addFirst(new Pos(x, y-1));
      }
      if (Pos.isEqual (goal)) {
        break;
      }
    }
    /* Acha o caminho */
    if (!Pos.isEqual (goal)) {
      // Não tem caminho
      return null;
    }
    pos = goal;
    while (!pos.isEqual(start)) {
      dist = pos.goal;
      path.addFirst(pos);
      pos = bestNeighbor(pos);
    }
    return path;
  }

  private static Pos bestNeighbor (Pos current) {
    Pos[] neighbors new Pos[8];
    Pos best;
    int bestValue;
    neighbors[0] = new Pos (current.x-1, current.y);
    neighbors[1] = new Pos (current.x-1, current.y-1);
    neighbors[2] = new Pos (current.x, current.y-1);
    neighbors[3] = new Pos (current.x+1, current.y-1);
    neighbors[4] = new Pos (current.x+1, current.y);
    neighbors[5] = new Pos (current.x+1, current.y+1);
    neighbors[6] = new Pos (current.x, current.y+1);
    neighbors[7] = new Pos (current.x-1, current.y+1);
    best = neighbors[0];
    bestValue = map[neighbors[0].x][neighbors[0].y];
    for (int i = 1; i < 8; i++) {
      if (map[neighbors[i].x][neighbors[i].y] < bestValue) {
        best = neighbors[i];
        bestValue = map[neighbors[i].x][neighbors[i].y];
      }
    }
    return best;
  }

  public static void main (String[] args) {
    int altura = 916, largura = 1182, cel_side = 50, x, y;
    MasterNav master = new MasterNav();
    master.connect();
    LinkedList<Pos> path;
    Pos start, goal;
    Scanner scan = new Scanner( System.in );
    master.connect();

    System.out.println("Qual a largura do mapa (em mm)?");
    largura = scan.nextInt();
    System.out.println("Qual a altura (em mm)?");
    altura = scan.nextInt();
    System.out.println("Qual o lado de cada célula de ocupação (em mm)?");
    cel_side = scan.nextInt();
    mapa = criaMapa (altura, largura, cel_side);

    System.out.println("Qual a posição X inicial (em mm)?");
    x = scan.nextInt() / cel_side;
    System.out.println("Qual a posição Y inicial (em mm)?");
    y = scan.nextInt() / cel_side;
    start = new Pos (x, y);
    System.out.println("Qual a posição X final (em mm)?");
    x = scan.nextInt() / cel_side;
    System.out.println("Qual a posição Y final (em mm)?");
    y = scan.nextInt() / cel_side;
    goal = new Pos (x, y);

    master.sendCommand (SET_START, start.x/10, start.y/10);

    path = findPath(start, goal);
    while (!path.isEmpty()) {
      Pos pos = path.removeFirst();
      ret = master.sendCommand(ADD_POINT, pos.x()/10f, pos.y()/10f);
    }
    ret = master.sendCommand(TRAVEL_PATH, -1, -1);
    master.close();
  }
}
