import java.util.*;
import java.util.stream.Collectors;

public class AutomataConverter {

    public static AutomataDefinition convertToDFA(AutomataDefinition nfa) {
        Set<String> alphabet = nfa.getAlphabetSigma();
        if (nfa.getType().equals("NFA-LAMBDA")) {
            alphabet.remove("λ");
        }

        Map<Set<String>, String> subsetToName = new HashMap<>();
        Map<String, Set<String>> nameToSubset = new HashMap<>();
        Queue<Set<String>> queue = new LinkedList<>();
        
        int stateCounter = 0;
        
        Set<String> initialSubset = getEpsilonClosure(Set.of(nfa.getInitialState()), nfa.getTransitionsDelta());
        String initialName = "d" + stateCounter++;
        
        subsetToName.put(initialSubset, initialName);
        nameToSubset.put(initialName, initialSubset);
        queue.add(initialSubset);
        
        Set<String> dfaStates = new HashSet<>();
        dfaStates.add(initialName);
        
        List<AutomataDefinition.TransitionData> dfaTransitions = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            Set<String> currentSubset = queue.poll();
            String currentName = subsetToName.get(currentSubset);
            
            for (String symbol : alphabet) {
                Set<String> moveResult = getMove(currentSubset, symbol, nfa.getTransitionsDelta());
                Set<String> targetSubset = getEpsilonClosure(moveResult, nfa.getTransitionsDelta());
                
                if (targetSubset.isEmpty()) continue; 
                
                if (!subsetToName.containsKey(targetSubset)) {
                    String newName = "d" + stateCounter++;
                    subsetToName.put(targetSubset, newName);
                    nameToSubset.put(newName, targetSubset);
                    dfaStates.add(newName);
                    queue.add(targetSubset);
                }
                
                String targetName = subsetToName.get(targetSubset);
                dfaTransitions.add(new AutomataDefinition.TransitionData(currentName, symbol, targetName));
            }
        }
        
        Set<String> dfaFinalStates = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : nameToSubset.entrySet()) {
            String dfaState = entry.getKey();
            Set<String> nfaSubset = entry.getValue();
            
            boolean isFinal = false;
            for (String subState : nfaSubset) {
                if (nfa.getFinalStates().contains(subState)) {
                    isFinal = true;
                    break;
                }
            }
            if (isFinal) {
                dfaFinalStates.add(dfaState);
            }
        }
        
        return new AutomataDefinition(
            dfaStates,
            alphabet,
            initialName,
            dfaFinalStates,
            dfaTransitions,
            "DFA"
        );
    }

    private static Set<String> getEpsilonClosure(Set<String> states, List<AutomataDefinition.TransitionData> transitions) {
        Set<String> closure = new HashSet<>(states);
        Stack<String> stack = new Stack<>();
        stack.addAll(states);
        
        while (!stack.isEmpty()) {
            String current = stack.pop();
            
            for (AutomataDefinition.TransitionData t : transitions) {
                if (t.getSource().equals(current) && t.getSymbol().equals("λ")) {
                    if (!closure.contains(t.getDestination())) {
                        closure.add(t.getDestination());
                        stack.push(t.getDestination());
                    }
                }
            }
        }
        return closure;
    }

    private static Set<String> getMove(Set<String> states, String symbol, List<AutomataDefinition.TransitionData> transitions) {
        Set<String> result = new HashSet<>();
        for (String state : states) {
            for (AutomataDefinition.TransitionData t : transitions) {
                if (t.getSource().equals(state) && t.getSymbol().equals(symbol)) {
                    result.add(t.getDestination());
                }
            }
        }
        return result;
    }
}