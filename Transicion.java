public class Transicion {

    private final Estado origen;
    private final Estado destino;
    private final String simbolo;


    public Transicion(Estado origen, Estado destino, String simbolo) {
        this.origen = origen;
        this.destino = destino;
        this.simbolo = simbolo;
    }

    public Estado getOrigen() {
        return origen;
    }

    public Estado getDestino() {
        return destino;
    }

    public String getSimbolo() {
        return simbolo;
    }
}