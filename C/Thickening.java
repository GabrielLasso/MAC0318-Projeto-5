import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ArrayDeque;
import lejos.nxt.Button;
// Igual ao FrenteDeOnda, só muda a criação do mapa

public class Thickening {
  static boolean[][] passable;
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

  private static boolean[][] criamapa (int altura, int largura, int cel_side, int dilatacao) {
    boolean[][] map = new boolean[altura/cel_side][largura/cel_side];
    for (int i = 0; i < altura/cel_side; i++) {
      for (int j = 0; j < largura/cel_side; j++) {
        Rectangle rect = new Rectangle (j*cel_side - dilatacao, i*cel_side - dilatacao, cel_side + 2*dilatacao, cel_side + 2*dilatacao);
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
    int[][] map = new int[passable.length][passable[0].length];
    ArrayDeque<Pos> q = new ArrayDeque<Pos>();
    LinkedList<Pos> path = new LinkedList<Pos>();
    Pos pos = null;
    int x, y, dist;
    if (!passable[start.x()][start.y()]) return null;
    for (int i = 0; i < passable.length; i++) {
      for (int j = 0; j < passable[0].length; j++) {
        map[i][j] = -1;
      }
    }
    /* Busca em largura */
    q.addFirst(start);
    map[start.x()][start.y()] = 0;
    System.out.println("" + start.x() + ' ' + start.y());
    while (!q.isEmpty()) {
      pos = q.removeLast();
      x = pos.x();
      y = pos.y();
      dist = map[x][y];
      if (x+1 < passable.length && map[x+1][y] == -1 && passable[x+1][y]) {
        map[x+1][y] = dist + 1;
        q.addFirst(new Pos(x+1, y));
      }
      if (x > 0 && map[x-1][y] == -1 && passable[x-1][y]) {
        map[x-1][y] = dist + 1;
        q.addFirst(new Pos(x-1, y));
      }
      if (y+1 < passable[0].length && map[x][y+1] == -1 && passable[x][y+1]) {
        map[x][y+1] = dist + 1;
        q.addFirst(new Pos(x, y+1));
      }
      if (y > 0 && map[x][y-1] == -1 && passable[x][y-1]) {
        map[x][y-1] = dist + 1;
        q.addFirst(new Pos(x, y-1));
      }
      if (pos.isEqual (goal)) {
        break;
      }
    }
    // for (int i = 0; i < passable.length; i++){
    //   for (int j = 0; j < passable[0].length; j++) {
    //     System.out.print ("\t"+map[i][j]);
    //   }
    //   System.out.println();
    // }
    /* Acha o caminho */
    if (!pos.isEqual (goal)) {
      // Não tem caminho
      return null;
    }
    pos = goal;
    while (!pos.isEqual(start)) {
      path.addFirst(pos);
      pos = bestNeighbor(pos, map);
    }
    return path;
  }

  private static Pos bestNeighbor (Pos current, int[][] map) {
    Pos[] neighbors = new Pos[8];
    Pos best;
    int bestValue;
    neighbors[0] = new Pos (current.x()-1, current.y());
    neighbors[1] = new Pos (current.x()-1, current.y()-1);
    neighbors[2] = new Pos (current.x(), current.y()-1);
    neighbors[3] = new Pos (current.x()+1, current.y()-1);
    neighbors[4] = new Pos (current.x()+1, current.y());
    neighbors[5] = new Pos (current.x()+1, current.y()+1);
    neighbors[6] = new Pos (current.x(), current.y()+1);
    neighbors[7] = new Pos (current.x()-1, current.y()+1);
    best = neighbors[0];
    bestValue = map[current.x()][current.y()];
    for (int i = 0; i < 8; i++) {
      if (neighbors[i].x() >= 0 && neighbors[i].y() >= 0) {
        int neighborvalue = map[neighbors[i].x()][neighbors[i].y()];
        if (neighborvalue > -1 && neighborvalue < bestValue) {
          best = neighbors[i];
          bestValue = neighborvalue;
        }
      }
    }
    return best;
  }

  public static void main (String[] args) {
    int altura = 916, largura = 1182, cel_side = 50, dilatacao, x, y;
    MasterNav master = new MasterNav();
    LinkedList<Pos> path;
    Pos start, goal;
    Scanner scan = new Scanner( System.in );
    float ret;
    master.connect();

    System.out.println("Qual a largura do mapa (em mm)?");
    largura = scan.nextInt();
    System.out.println("Qual a altura (em mm)?");
    altura = scan.nextInt();
    System.out.println("Qual o lado de cada célula de ocupação (em mm)?");
    cel_side = scan.nextInt();
    System.out.println("Qual a dilatação (em mm)?");
    dilatacao = scan.nextInt();
    passable = criamapa (altura, largura, cel_side, dilatacao);

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

    master.sendCommand (SET_START, start.x()/10, start.y()/10);

    path = findPath(start, goal);
    if (path != null) {
      while (!path.isEmpty()) {
        Pos pos = path.removeFirst();
        ret = master.sendCommand(ADD_POINT, pos.x()/10f, pos.y()/10f);
      }
      ret = master.sendCommand(TRAVEL_PATH, -1, -1);
      master.close();
    }
  }
}
