import java.util.*;
import java.util.stream.Collectors;

public class KleeneConverter {

    public static String convert(AutomataDefinition def) {
        List<String> states = new ArrayList<>(def.getStatesQ());
        Collections.sort(states);
        
        int n = states.size();
        Map<String, Integer> stateIndex = new HashMap<>();
        for (int i = 0; i < n; i++) stateIndex.put(states.get(i), i);

        RegularExpression[][] table = new RegularExpression[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                table[i][j] = RegularExpression.EMPTY;
            }
        }

        for (AutomataDefinition.TransitionData t : def.getTransitionsDelta()) {
            int u = stateIndex.get(t.getSource());
            int v = stateIndex.get(t.getDestination());
            RegularExpression symbol = new RegularExpression(t.getSymbol());
            table[u][v] = table[u][v].union(symbol);
        }

        for (int i = 0; i < n; i++) {
            table[i][i] = table[i][i].union(RegularExpression.LAMBDA);
        }

        for (int k = 0; k < n; k++) {
            RegularExpression kkStar = table[k][k].star();
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    RegularExpression path = table[i][k].concatenate(kkStar).concatenate(table[k][j]);
                    table[i][j] = table[i][j].union(path);
                }
            }
        }

        String initial = def.getInitialState();
        Set<String> finals = def.getFinalStates();
        
        if (finals.isEmpty()) return "Ø";

        int startIndex = stateIndex.get(initial);
        RegularExpression result = RegularExpression.EMPTY;

        for (String finalState : finals) {
            int finalIndex = stateIndex.get(finalState);
            result = result.union(table[startIndex][finalIndex]);
        }

        return result.toString();
    }
}