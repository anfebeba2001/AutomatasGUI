import java.util.*;

public class KleeneConverter {

    public static String convert(AutomataDefinition def) {
        Map<String, Map<String, RegularExpression>> graph = new TreeMap<>();
        
        Set<String> allStates = new HashSet<>(def.getStatesQ());
        for (String s : allStates) graph.put(s, new TreeMap<>());

        for (AutomataDefinition.TransitionData t : def.getTransitionsDelta()) {
            Map<String, RegularExpression> outgoing = graph.get(t.getSource());
            RegularExpression current = outgoing.getOrDefault(t.getDestination(), RegularExpression.EMPTY);
            outgoing.put(t.getDestination(), RegularExpression.union(current, RegularExpression.symbol(t.getSymbol())));
        }

        String startNode = "QS";
        String finishNode = "QF";
        
        graph.put(startNode, new TreeMap<>());
        graph.put(finishNode, new TreeMap<>());

        graph.get(startNode).put(def.getInitialState(), RegularExpression.LAMBDA);

        for (String finalState : def.getFinalStates()) {
            if (graph.containsKey(finalState)) {
                Map<String, RegularExpression> out = graph.get(finalState);
                RegularExpression current = out.getOrDefault(finishNode, RegularExpression.EMPTY);
                out.put(finishNode, RegularExpression.union(current, RegularExpression.LAMBDA));
            }
        }

        List<String> toEliminate = new ArrayList<>(allStates);
       
        for (String k : toEliminate) {
            RegularExpression loop = graph.get(k).remove(k); 
            RegularExpression loopStar = (loop == null) ? RegularExpression.LAMBDA : RegularExpression.star(loop);

            List<String> incoming = new ArrayList<>();
            for (String s : graph.keySet()) {
                if (graph.get(s).containsKey(k)) incoming.add(s);
            }
            
            Map<String, RegularExpression> outgoing = graph.get(k); 

            for (String inState : incoming) {
                RegularExpression r_in = graph.get(inState).remove(k); 
                
                for (Map.Entry<String, RegularExpression> entry : outgoing.entrySet()) {
                    String outState = entry.getKey();
                    RegularExpression r_out = entry.getValue();

                    RegularExpression pathViaK = RegularExpression.concat(r_in, RegularExpression.concat(loopStar, r_out));
                    
                    RegularExpression existing = graph.get(inState).getOrDefault(outState, RegularExpression.EMPTY);
                    graph.get(inState).put(outState, RegularExpression.union(existing, pathViaK));
                }
            }
            graph.remove(k);
        }

        RegularExpression result = graph.get(startNode).get(finishNode);
        return result == null ? "Ø" : result.toString();
    }
}