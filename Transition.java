public class Transition {

    private final State source;
    private final State destination;
    private final String symbol;

    public Transition(State source, State destination, String symbol) {
        this.source = source;
        this.destination = destination;
        this.symbol = symbol;
    }

    public State getSource() { return source; }
    public State getDestination() { return destination; }
    public String getSymbol() { return symbol; }
}