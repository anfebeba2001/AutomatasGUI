public class DescripcionNatural {

    public static String generar(Automata a) {
        StringBuilder sb = new StringBuilder();
        Estado inicial = a.getEstadoInicial();

        sb.append("El autómata inicia en el estado ").append(inicial.getNombre()).append(".\n\n");
        sb.append("El conjunto de símbolos es: ").append(a.getAlfabeto()).append(".\n\n");
        sb.append("Transiciones:\n");

        for (Transicion t : a.getTransiciones()) {
            sb.append("  ")
              .append(t.getOrigen().getNombre())
              .append(" --")
              .append(t.getSimbolo())
              .append("--> ")
              .append(t.getDestino().getNombre())
              .append("\n");
        }

        sb.append("\nEstados finales: ");
        a.getEstados().stream()
                .filter(Estado::esFinal)
                .forEach(e -> sb.append(e.getNombre()).append(" "));

        sb.append("\n\nEste autómata acepta las cadenas que permiten llegar desde el estado inicial a un estado final siguiendo las transiciones anteriores.");

        return sb.toString();
    }
}
