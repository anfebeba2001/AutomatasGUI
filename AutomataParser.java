import java.util.*;

public class AutomataParser {

    public static AutomataDefinition parse(Set<String> statesQ, Set<String> alphabetSigma, 
                                           String initialStr, Set<String> finalStates, 
                                           List<AutomataDefinition.TransitionData> transitionsDelta, String typeStr) throws IllegalArgumentException {

        String initialState = initialStr.trim();
        String type = typeStr.toUpperCase();
        
        if (statesQ.isEmpty()) throw new IllegalArgumentException("Q (States) cannot be empty.");
        if (alphabetSigma.isEmpty()) throw new IllegalArgumentException("Sigma (Alphabet) cannot be empty.");
        if (initialState.isEmpty()) throw new IllegalArgumentException("Initial state cannot be empty.");
        if (type.isEmpty()) throw new IllegalArgumentException("Automata Type cannot be empty.");
        if (initialState.equals("q0")) throw new IllegalArgumentException("Initial state name 'q0' is restricted. Please use an alternative name.");

        if (!statesQ.contains(initialState)) {
            throw new IllegalArgumentException("Initial state '" + initialState + "' is not defined in Q.");
        }
        for (String f : finalStates) {
            if (!statesQ.contains(f)) {
                throw new IllegalArgumentException("Final state '" + f + "' is not defined in Q.");
            }
        }

        if (type.equals("DFA")) {
            validateDFATransitions(statesQ, alphabetSigma, transitionsDelta);
        } else if (type.equals("NFA") || type.equals("NFA-LAMBDA")) {
            Set<String> validSymbols = new HashSet<>(alphabetSigma);
            if (type.equals("NFA-LAMBDA")) {
                 validSymbols.add("λ");
            }
            validateBaseTransitions(statesQ, validSymbols, transitionsDelta);
        } else {
            throw new IllegalArgumentException("Unsupported Automata Type: " + type);
        }
        
        return new AutomataDefinition(statesQ, alphabetSigma, initialState, finalStates, transitionsDelta, type);
    }
    
    private static void validateBaseTransitions(Set<String> statesQ, Set<String> validSymbols, 
                                            List<AutomataDefinition.TransitionData> transitions) {
        for (AutomataDefinition.TransitionData t : transitions) {
            if (!statesQ.contains(t.getSource())) {
                throw new IllegalArgumentException("Invalid transition: Source state '" + t.getSource() + "' is not in Q.");
            }
            if (!statesQ.contains(t.getDestination())) {
                throw new IllegalArgumentException("Invalid transition: Destination state '" + t.getDestination() + "' is not in Q.");
            }
            if (!validSymbols.contains(t.getSymbol())) {
                throw new IllegalArgumentException("Invalid transition: Symbol '" + t.getSymbol() + "' is not in the alphabet Σ (or 'λ').");
            }
        }
    }
    
    private static void validateDFATransitions(Set<String> statesQ, Set<String> alphabetSigma, 
                                              List<AutomataDefinition.TransitionData> transitions) {
        
        for (AutomataDefinition.TransitionData t : transitions) {
            if (t.getSymbol().equals("λ")) {
                throw new IllegalArgumentException("DFA Error: Lambda (λ) transitions are not allowed.");
            }
        }
        
        validateBaseTransitions(statesQ, alphabetSigma, transitions);

        for (String q : statesQ) {
            for (String symbol : alphabetSigma) {
                
                long numTransitions = transitions.stream()
                    .filter(t -> t.getSource().equals(q) && t.getSymbol().equals(symbol))
                    .count();

                if (numTransitions > 1) {
                    throw new IllegalArgumentException("DFA Error (Non-Deterministic): State '" + q + "' has multiple transitions for symbol '" + symbol + "'.");
                }
            }
        }
    }
}