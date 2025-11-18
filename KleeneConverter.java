import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KleeneConverter {

    private final List<Estado> estados;
    private final List<Transicion> transiciones;
    private final Map<String, Integer> estadoToIndex;
    private ExpresionRegular[][] R; // Matriz de Expresiones Regulares

    public KleeneConverter(List<Estado> estados, List<Transicion> transiciones) {
        this.estados = estados;
        this.transiciones = transiciones;
        this.estadoToIndex = new HashMap<>();
        
        // Asignar un índice a cada estado (q0 -> 0, q1 -> 1, ...)
        for (int i = 0; i < estados.size(); i++) {
            estadoToIndex.put(estados.get(i).getNombre(), i);
        }

        initializeMatrix();
    }

    private void initializeMatrix() {
        int n = estados.size();
        R = new ExpresionRegular[n][n];

        // Inicializar toda la matriz con VACIO (Ø)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                R[i][j] = ExpresionRegular.VACIO;
            }
        }

        // Rellenar la matriz inicial con las transiciones
        for (Transicion t : transiciones) {
            int i = estadoToIndex.get(t.getOrigen().getNombre());
            int j = estadoToIndex.get(t.getDestino().getNombre());
            
            ExpresionRegular simboloER = new ExpresionRegular(t.getSimbolo());
            
            // Si ya existe una transición, es una UNIÓN
            R[i][j] = R[i][j].union(simboloER);
        }
        
        // Añadir lambda (λ) a la diagonal (auto-transiciones)
        for (int i = 0; i < n; i++) {
            R[i][i] = R[i][i].union(ExpresionRegular.LAMBDA);
        }
    }

    /**
     * Aplica el Algoritmo de Eliminación de Estados para obtener la ER.
     */
    public ExpresionRegular getExpresionRegular() {
        int n = estados.size();
        int numEstados = n;

        // Implementación del Algoritmo de Eliminación de Estados (Rij(k) = Rij(k-1) + Rik(k-1) * (Rkk(k-1))* * Rkj(k-1))
        for (int k = 0; k < numEstados; k++) { // Estado a eliminar (pivote)
            
            // Calculamos Rkk_star = (Rkk)* una sola vez por pivote
            ExpresionRegular Rkk_star = R[k][k].cerraduraKleene();
            
            for (int i = 0; i < numEstados; i++) { // Origen
                for (int j = 0; j < numEstados; j++) { // Destino
                    
                    // Solo actualizamos si i y j son distintos de k
                    if (i != k || j != k) {
                        
                        // Parte R_ik * (R_kk)* * R_kj
                        ExpresionRegular caminoViaK = R[i][k]
                            .concatenacion(Rkk_star)
                            .concatenacion(R[k][j]);
                        
                        // Nueva R_ij = R_ij + (Camino via k)
                        R[i][j] = R[i][j].union(caminoViaK);
                    }
                }
            }
        }
        
        // El resultado final es la unión de todas las ERs de las transiciones
        // desde el estado inicial a todos los estados finales.
        
        Estado estadoInicial = estados.stream().filter(Estado::esInicial).findFirst()
                                .orElseThrow(() -> new IllegalStateException("No se encontró estado inicial."));
                                
        List<Estado> estadosFinales = estados.stream().filter(Estado::esFinal).collect(Collectors.toList());

        if (estadosFinales.isEmpty()) {
            return ExpresionRegular.VACIO; // El lenguaje es vacío si no hay estados finales
        }

        int indexInicial = estadoToIndex.get(estadoInicial.getNombre());
        ExpresionRegular resultadoFinal = ExpresionRegular.VACIO;

        for (Estado eFinal : estadosFinales) {
            int indexFinal = estadoToIndex.get(eFinal.getNombre());
            // El resultado es la unión de R[indexInicial][indexFinal] para todos los estados finales
            resultadoFinal = resultadoFinal.union(R[indexInicial][indexFinal]);
        }

        // El resultado final R[inicial][final] tiene autoenlaces incluidos.
        return resultadoFinal;
    }
}