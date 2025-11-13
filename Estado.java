public class Estado {

    String nombre;
    private int x, y; 
    private final int radio;
    private boolean esInicial;
    private boolean esFinal;
    private boolean seleccionado;

    public Estado(String nombre, int x, int y) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.radio = 25; 
        this.esInicial = false;
        this.esFinal = false;
        this.seleccionado = false;
    }

    public boolean contienePunto(int px, int py) {
        return (px - x) * (px - x) + (py - y) * (py - y) <= radio * radio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRadio() {
        return radio;
    }

    public boolean esInicial() {
        return esInicial;
    }

    public void setInicial(boolean esInicial) {
        this.esInicial = esInicial;
    }

    public boolean esFinal() {
        return esFinal;
    }

    public void setFinal(boolean esFinal) {
        this.esFinal = esFinal;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }
}