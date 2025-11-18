import java.util.*;

public class Automata {

    private List<Estado> estados = new ArrayList<>();
    private List<Transicion> transiciones = new ArrayList<>();

    public void agregarEstado(Estado e) {
        estados.add(e);
    }

    public void agregarTransicion(Transicion t) {
        transiciones.add(t);
    }

    public List<Estado> getEstados() {
        return estados;
    }

    public List<Transicion> getTransiciones() {
        return transiciones;
    }

    public Estado getEstadoInicial() {
        return estados.stream()
                .filter(Estado::esInicial)
                .findFirst()
                .orElse(null);
    }

    public boolean esFinal(Estado e) {
        return e.esFinal();
    }

    public List<Estado> mover(Estado estado, String simbolo) {
        List<Estado> posibles = new ArrayList<>();

        for (Transicion t : transiciones) {
            if (t.getOrigen().equals(estado) && t.getSimbolo().equals(simbolo)) {
                posibles.add(t.getDestino());
            }
        }
        return posibles;
    }

    public Set<String> getAlfabeto() {
        Set<String> sigma = new HashSet<>();
        for (Transicion t : transiciones) {
            sigma.add(t.getSimbolo());
        }
        return sigma;
    }
}
