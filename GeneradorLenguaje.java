import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class GeneradorLenguaje {

    public static List<String> generarHastaN(
            List<Estado> estados,
            List<Transicion> transiciones,
            int n) {

        List<String> aceptadas = new ArrayList<>();

        // 1. Obtener estado inicial
        Estado inicial = null;
        for (Estado e : estados) {
            if (e.esInicial()) {
                inicial = e;
                break;
            }
        }

        if (inicial == null) return aceptadas;

        // 2. Construir alfabeto real desde transiciones (excluir lambda si existe)
        Set<String> sigma = new HashSet<>();
        for (Transicion t : transiciones) {
            String s = t.getSimbolo();
            if (s == null) continue;
            // si quieres incluir 'λ' como símbolo visible, quítale este filtro
            if (!s.equals("λ")) sigma.add(s);
        }

        // 3. BFS: (estado actual, cadena construida)
        Queue<NodoCadena> cola = new LinkedList<>();
        cola.add(new NodoCadena(inicial, "")); // cadena vacía posible si inicial es final

        while (!cola.isEmpty()) {
            NodoCadena p = cola.poll();

            // Si es aceptado y la cadena no supera longitud n
            if (p.estado.esFinal() && p.cadena.length() <= n) {
                // Evitar duplicados: podrías querer un Set en lugar de List
                aceptadas.add(p.cadena);
            }

            // No seguir creciendo esta cadena si ya llegó a n
            if (p.cadena.length() == n) continue;

            // Extender con cada símbolo del alfabeto
            for (String s : sigma) {
                // Buscar destinos manualmente
                for (Transicion t : transiciones) {
                    if (t.getOrigen() == p.estado && t.getSimbolo().equals(s)) {
                        Estado nuevo = t.getDestino();
                        cola.add(new NodoCadena(nuevo, p.cadena + s));
                    }
                }
            }
        }

        return aceptadas;
    }

    // clase auxiliar renombrada y con miembros privados (encapsulada)
    private static class NodoCadena {
        Estado estado;
        String cadena;

        NodoCadena(Estado e, String c) {
            this.estado = e;
            this.cadena = c;
        }
    }
}
