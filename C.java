import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ArrayDeque;
import lejos.nxt.Button;

public class C {

  static boolean[][] passable;
  private static final byte ADD_POINT = 0; //adds waypoint to path
  private static final byte TRAVEL_PATH = 1; // enables slave to execute the path
  private static final byte STATUS = 2; // enquires about slave's position
  private static final byte SET_START = 3; // set initial waypoint
  private static final byte STOP = 4; // closes communication
  static int altura = 916, largura = 1182, cel_side = 50, x, y, dilatacao = 0;
  static int[][] map;

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

  private static boolean[][] criamapa () {
    int height = (int) Math.ceil((float)altura/(float)cel_side);
    int width = (int)Math.ceil((float)largura/(float)cel_side);
    boolean[][] map = new boolean[width][height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        Rectangle rect = new Rectangle (i*cel_side - dilatacao, j*cel_side - dilatacao, cel_side + 2*dilatacao, cel_side + 2*dilatacao);
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
    int width = passable.length;
    int height = passable[0].length;
    ArrayDeque<Pos> q = new ArrayDeque<Pos>();
    LinkedList<Pos> path = new LinkedList<Pos>();
    Pos pos = null;
    int x, y, dist;
    map = new int[width][height];
    if (!passable[start.x()][start.y()]) return null;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        map[i][j] = -1;
      }
    }
    /* Busca em largura */
    q.addFirst(start);
    map[start.x()][start.y()] = 0;
    while (!q.isEmpty()) {
      pos = q.removeLast();
      x = pos.x();
      y = pos.y();
      dist = map[x][y];
      if (x+1 < width)
       if ( map[x+1][y] == -1)
        if ( passable[x+1][y]) {
        map[x+1][y] = dist + 1;
        q.addFirst(new Pos(x+1, y));
      }
      if (x > 0 && map[x-1][y] == -1 && passable[x-1][y]) {
        map[x-1][y] = dist + 1;
        q.addFirst(new Pos(x-1, y));
      }
      if (y+1 < height && map[x][y+1] == -1 && passable[x][y+1]) {
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
    /* Acha o caminho */
    if (!pos.isEqual (goal)) {
      // Não tem caminho
      System.out.println("Não tem caminho");
      return null;
    }
    pos = goal;
    while (!pos.isEqual(start)) {
      path.addFirst(pos);
      pos = bestNeighbor(pos, map);
    }
    path.addFirst (start);
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
      int neix = neighbors[i].x();
      int neiy = neighbors[i].y();
      if (neix >= 0 && neiy >= 0 && neix < map.length && neiy < map[0].length) {
        int neighborvalue = map[neighbors[i].x()][neighbors[i].y()];
        if (neighborvalue > -1 && neighborvalue < bestValue) {
          best = neighbors[i];
          bestValue = neighborvalue;
        }
      }
    }
    return best;
  }

  private static LinkedList<Pos> lineariza (LinkedList<Pos> path) {
    Pos p, q, old_q = null;
    LinkedList<Pos> linearPath = new LinkedList<Pos>();
    boolean intercepta = false;
    p = path.removeFirst();
    linearPath.addLast(p);
    while (!path.isEmpty()) {
      q = p;
      intercepta = false;
      while (!intercepta) {
        old_q = q;
        if (path.isEmpty()) {
          linearPath.addLast(old_q);
          return linearPath;
        }
        q = path.removeFirst();
        Line pq = new Line (p.x(), p.y(), q.x(), q.y());
        for (int i = 0; i < passable.length; i++) {
          for (int j = 0; j < passable[0].length; j++) {
            Rectangle rect = new Rectangle (i + 0.00005f, j + 0.00005f, 0.9999f, 0.9999f);
            if (pq.intersects(rect) && !passable[i][j]) {
              intercepta = true;
            }
          }
        }
      }
      p = q;
      linearPath.addLast(old_q);
      old_q = q;
    }
    return linearPath;
  }

  public static void main (String[] args) {
    MasterNav master = new MasterNav();
    LinkedList<Pos> path;
    Pos start, goal;
    Scanner scan = new Scanner( System.in );
    float ret;
    char c;
    boolean linear = false;

    master.connect();

    System.out.println("Qual a largura do mapa (em mm)?");
    largura = scan.nextInt();
    System.out.println("Qual a altura (em mm)?");
    altura = scan.nextInt();
    System.out.println("Qual o lado de cada célula de ocupação (em mm)?");
    cel_side = scan.nextInt();
    System.out.println("Qual a dilatação (em mm)?");
    dilatacao = scan.nextInt();
    passable = criamapa ();

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

    System.out.println("Lineariza a trajetória? (S/N)");
    c = scan.next().charAt(0);
    if (Character.toUpperCase(c) == 'S') linear = true;

    path = findPath(start, goal);

    master.sendCommand (SET_START, (start.x()*cel_side)/10f, (start.y()*cel_side)/10f);

    if (path != null && !path.isEmpty()) {
      if (linear)
        path = lineariza(path);
      while (!path.isEmpty()) {
        Pos pos = path.removeFirst();
        ret = master.sendCommand(ADD_POINT, pos.x()*cel_side/10f, pos.y()*cel_side/10f);
      }
      ret = master.sendCommand(TRAVEL_PATH, -1, -1);
      // desenha((LinkedList<Pos>)path.clone());
      master.close();
    }
  }

  static void desenha (LinkedList<Pos> caminho) {
    int i, j, max = 0;
    for(i = 0; i < map.length; i++) {
      for(j = 0; j < map[0].length; j++) {
        if (map[i][j] > max) max = map[i][j];
      }
    }
    StdDraw.setCanvasSize( 512, (int) 512*map[0].length/map.length);
    StdDraw.setXscale((double) 0, (double) map.length);
    StdDraw.setYscale((double) 0, (double) map[0].length);

    for(i = 0; i < map.length; i++) {
      for(j = 0; j < map[0].length; j++) {
        if(map[i][j] > -1)
          StdDraw.setPenColor(255 - map[i][j]*255/max, 200 - map[i][j]*200/max, 100 - map[i][j]*100/max);
        else if (!passable[i][j])
          StdDraw.setPenColor(StdDraw.WHITE);
        else
          StdDraw.setPenColor(StdDraw.BLACK);

       StdDraw.filledSquare(i + 0.5, j + 0.5, 0.5);
      }
    }
    StdDraw.setPenColor(StdDraw.RED);
    Pos pos;
    Pos lastpos = caminho.getFirst();
    while (!caminho.isEmpty()) {
      pos = caminho.removeFirst();
     StdDraw.filledSquare(pos.x() + 0.5, pos.y() + 0.5, 0.5);
      StdDraw.line (pos.x() + 0.5, pos.y() + 0.5, lastpos.x() + 0.5, lastpos.y() + 0.5);
      lastpos = pos;
    }
  }
}
