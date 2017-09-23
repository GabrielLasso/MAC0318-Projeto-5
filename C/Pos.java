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
  public boolean isEqual(Pos p) {
    return p.x == x && p.y == y;
  }
}
