public class State {

    String name;
    private int x, y; 
    private final int radius;
    private boolean isInitial;
    private boolean isFinal;

    public State(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.radius = 25; 
        this.isInitial = false;
        this.isFinal = false;
    }

    public String getName() { return name; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getRadius() { return radius; }
    public boolean isInitial() { return isInitial; }
    public void setInitial(boolean isInitial) { this.isInitial = isInitial; }
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean isFinal) { this.isFinal = isFinal; }
}