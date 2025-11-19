import java.util.Set;
import java.util.List;

public class AutomataDefinition {
    
    private final Set<String> statesQ;
    private final Set<String> alphabetSigma;
    private final String initialState;
    private final Set<String> finalStates;
    private final List<TransitionData> transitionsDelta;
    private final String type;

    public AutomataDefinition(Set<String> statesQ, Set<String> alphabetSigma, 
                              String initialState, Set<String> finalStates, 
                              List<TransitionData> transitionsDelta, String type) {
        this.statesQ = statesQ;
        this.alphabetSigma = alphabetSigma;
        this.initialState = initialState;
        this.finalStates = finalStates;
        this.transitionsDelta = transitionsDelta;
        this.type = type;
    }

    public Set<String> getStatesQ() { return statesQ; }
    public Set<String> getAlphabetSigma() { return alphabetSigma; }
    public String getInitialState() { return initialState; }
    public Set<String> getFinalStates() { return finalStates; }
    public List<TransitionData> getTransitionsDelta() { return transitionsDelta; }
    public String getType() { return type; }
    
    public static class TransitionData {
        private final String source;
        private final String symbol;
        private final String destination;

        public TransitionData(String source, String symbol, String destination) {
            this.source = source;
            this.symbol = symbol;
            this.destination = destination;
        }

        public String getSource() { return source; }
        public String getSymbol() { return symbol; }
        public String getDestination() { return destination; }
    }
}