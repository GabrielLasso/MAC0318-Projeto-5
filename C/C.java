import lejos.geom.*;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lejos.nxt.Button;

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

  public static void main (String[] args) {
    int altura = 916, largura = 1182;
    mapa = criaMapa (altura, largura, 50);
  }
}
