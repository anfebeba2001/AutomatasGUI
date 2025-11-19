import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AutomataDrawerGUI extends JFrame {

    private JSpinner spinNumStates, spinNumVariables;
    private JComboBox<String> cmbInitialState;
    private JList<String> listFinalStates;
    private JComboBox<String> cmbAutomataType;
    private DrawingPanel drawingPanel;

    private JComboBox<String> cmbTransSrc;
    private JComboBox<String> cmbTransSym;
    private JComboBox<String> cmbTransDest;

    private DefaultListModel<String> transitionListModel;
    private JList<String> listTransitions;

    private Map<String, State> stateMap;
    private final String[] DEFAULT_STATE_PREFIX = {"s"};
    private static final int MAX_LIMIT = 10;
    
    private JPanel controlPanel; 

    public AutomataDrawerGUI() {
        setTitle("Automata Designer");
        setSize(1150, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(420, 750));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.weightx = 1.0;

        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.5;
        controlPanel.add(new JLabel("Count States:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.5;
        spinNumStates = new JSpinner(new SpinnerNumberModel(3, 1, MAX_LIMIT, 1));
        spinNumStates.addChangeListener(e -> updateSelectionComponents());
        controlPanel.add(spinNumStates, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.5;
        controlPanel.add(new JLabel("Count Symbols:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.5;
        spinNumVariables = new JSpinner(new SpinnerNumberModel(2, 1, MAX_LIMIT, 1));
        spinNumVariables.addChangeListener(e -> updateSelectionComponents());
        controlPanel.add(spinNumVariables, gbc);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        controlPanel.add(new JSeparator(), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Initial State:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbInitialState = new JComboBox<>();
        controlPanel.add(cmbInitialState, gbc);

        gbc.gridx = 0; gbc.gridy = row++; 
        gbc.gridwidth = 2;
        controlPanel.add(new JLabel("Final States (Ctrl+Click):"), gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.weighty = 0.15; 
        gbc.fill = GridBagConstraints.BOTH;
        listFinalStates = new JList<>();
        listFinalStates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollFinal = new JScrollPane(listFinalStates);
        scrollFinal.setPreferredSize(new Dimension(300, 120));
        controlPanel.add(scrollFinal, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        cmbAutomataType = new JComboBox<>(new String[]{"DFA", "NFA", "NFA-LAMBDA"});
        cmbAutomataType.setSelectedItem("DFA");
        cmbAutomataType.addActionListener(e -> updateSelectionComponents());
        controlPanel.add(cmbAutomataType, gbc);

        gbc.gridy = row++;
        controlPanel.add(new JSeparator(), gbc);
        gbc.gridy = row++;
        controlPanel.add(new JLabel("Add Transitions:"), gbc);
        
        gbc.gridy = row++;
        controlPanel.add(createTransitionBuilderPanel(), gbc);
        
        gbc.gridy = row++; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(new JLabel("Transition List:"), gbc);
        gbc.gridy = row++;
        transitionListModel = new DefaultListModel<>();
        listTransitions = new JList<>(transitionListModel);
        listTransitions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTransitions = new JScrollPane(listTransitions);
        scrollTransitions.setPreferredSize(new Dimension(300, 150));
        controlPanel.add(scrollTransitions, gbc);

        gbc.gridy = row++; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btnDraw = new JButton("DRAW AUTOMATON");
        btnDraw.setFont(new Font("Arial", Font.BOLD, 14));
        btnDraw.addActionListener(e -> drawAutomaton());
        controlPanel.add(btnDraw, gbc);
        
        gbc.gridy = row++;
        JButton btnGenerateExp = new JButton("GENERATE EXPRESSION");
        btnGenerateExp.setFont(new Font("Arial", Font.BOLD, 14));
        btnGenerateExp.setForeground(new Color(0, 100, 0));
        btnGenerateExp.addActionListener(e -> generateExpression());
        controlPanel.add(btnGenerateExp, gbc);

        drawingPanel = new DrawingPanel();
        drawingPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        add(controlPanel, BorderLayout.WEST);
        add(drawingPanel, BorderLayout.CENTER);
        
        updateSelectionComponents();
    }
    
    private void updateSelectionComponents() {
        int numStates = (Integer) spinNumStates.getValue();
        int numSymbols = (Integer) spinNumVariables.getValue();

        List<String> stateNames = new ArrayList<>();
        for (int i = 1; i <= numStates; i++) {
            stateNames.add(DEFAULT_STATE_PREFIX[0] + i);
        }
        
        List<String> alphabetSymbols = new ArrayList<>();
        for (int i = 0; i < numSymbols; i++) {
            alphabetSymbols.add(String.valueOf(i)); 
        }
        
        String selectedType = (String) cmbAutomataType.getSelectedItem();
        if ("NFA-LAMBDA".equals(selectedType)) {
            alphabetSymbols.add("λ");
        }

        String[] stateArray = stateNames.toArray(String[]::new);
        String[] symbolArray = alphabetSymbols.toArray(String[]::new);

        cmbInitialState.setModel(new DefaultComboBoxModel<>(stateArray));
        if (stateNames.contains("s1")) cmbInitialState.setSelectedItem("s1"); 

        DefaultListModel<String> finalStateModel = new DefaultListModel<>();
        stateNames.forEach(finalStateModel::addElement);
        listFinalStates.setModel(finalStateModel);
        
        if (cmbTransSrc != null && cmbTransSym != null && cmbTransDest != null) {
            cmbTransSrc.setModel(new DefaultComboBoxModel<>(stateArray));
            cmbTransDest.setModel(new DefaultComboBoxModel<>(stateArray));
            cmbTransSym.setModel(new DefaultComboBoxModel<>(symbolArray));
        }
        
        if (transitionListModel != null) {
            transitionListModel.clear();
        }
    }

    private JPanel createTransitionBuilderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 5, 5)); 
        
        cmbTransSrc = new JComboBox<>();
        cmbTransSym = new JComboBox<>();
        cmbTransDest = new JComboBox<>();

        JButton btnAdd = new JButton("Add [+]");
        btnAdd.addActionListener(e -> addTransition((String)cmbTransSrc.getSelectedItem(), (String)cmbTransSym.getSelectedItem(), (String)cmbTransDest.getSelectedItem()));
        
        JButton btnRemove = new JButton("Del [-]");
        btnRemove.addActionListener(e -> removeSelectedTransition());

        panel.add(cmbTransSrc);
        panel.add(cmbTransSym);
        panel.add(cmbTransDest);
        
        panel.add(btnAdd);
        panel.add(new JLabel("")); 
        panel.add(btnRemove);
        
        return panel;
    }
    
    private void addTransition(String src, String symbol, String dest) {
        if (src == null || symbol == null || dest == null) return;
        
        String type = (String) cmbAutomataType.getSelectedItem();
        
        if (!"NFA-LAMBDA".equals(type) && "λ".equals(symbol)) {
            JOptionPane.showMessageDialog(this, "Error: 'λ' transitions are only allowed in NFA-LAMBDA.", "Invalid Transition", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String transition = src + "," + symbol + "," + dest;
        
        if (transitionListModel.contains(transition)) {
             JOptionPane.showMessageDialog(this, "Error: Duplicate transition (" + transition + ").", "Duplicate", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        if ("DFA".equals(type)) {
            for (int i = 0; i < transitionListModel.getSize(); i++) {
                String existing = transitionListModel.getElementAt(i);
                String[] parts = existing.split(",");
                if (parts[0].equals(src) && parts[1].equals(symbol)) {
                    JOptionPane.showMessageDialog(this, "DFA Violation: State '" + src + "' already has a transition for '" + symbol + "'.", "DFA Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        transitionListModel.addElement(transition);
    }
    
    private void removeSelectedTransition() {
        int selected = listTransitions.getSelectedIndex();
        if (selected != -1) {
            transitionListModel.remove(selected);
        }
    }

    private Set<String> getSelectedFinalStates() {
        return new HashSet<>(listFinalStates.getSelectedValuesList());
    }

    private List<AutomataDefinition.TransitionData> getTransitionData() {
        List<AutomataDefinition.TransitionData> list = new ArrayList<>();
        for (int i = 0; i < transitionListModel.getSize(); i++) {
            String transitionStr = transitionListModel.getElementAt(i);
            String[] parts = transitionStr.split(",");
            if (parts.length == 3) {
                list.add(new AutomataDefinition.TransitionData(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            }
        }
        return list;
    }

    private void drawAutomaton() {
        try {
            AutomataDefinition def = buildDefinition();
            
            Set<String> reachableStates = findReachableStates(def);
            Set<String> drawingStatesQ = reachableStates;
            Set<String> drawingFinalStates = def.getFinalStates().stream()
                                                      .filter(drawingStatesQ::contains)
                                                      .collect(Collectors.toSet());

            List<AutomataDefinition.TransitionData> drawingTransitionsDelta = def.getTransitionsDelta().stream()
                .filter(t -> drawingStatesQ.contains(t.getSource()) && drawingStatesQ.contains(t.getDestination()))
                .collect(Collectors.toList());
                
            AutomataDefinition drawingDef = new AutomataDefinition(
                drawingStatesQ, 
                def.getAlphabetSigma(), 
                def.getInitialState(), 
                drawingFinalStates, 
                drawingTransitionsDelta, 
                def.getType()
            );

            drawingPanel.clearAll();
            stateMap = new HashMap<>(); 
            
            List<State> newStates = createPositionalStates(drawingDef);
            newStates.forEach(e -> stateMap.put(e.getName(), e));
            drawingPanel.setStates(newStates);
            
            List<Transition> newTransitions = createTransitions(drawingDef);
            drawingPanel.setTransitions(newTransitions);
            
            String message = "Automaton loaded successfully.";
            if (def.getStatesQ().size() != reachableStates.size()) {
                message += "\nHidden unreachable states: " + (def.getStatesQ().size() - reachableStates.size());
            }
            
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            drawingPanel.repaint();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateExpression() {
        try {
            AutomataDefinition def = buildDefinition();
            
            AutomataDefinition dfaForConversion;
            String conversionMessage = "";

            if (!def.getType().equals("DFA")) {
                dfaForConversion = AutomataConverter.convertToDFA(def);
                conversionMessage = "(Converted from " + def.getType() + " to DFA)\n\n";
            } else {
                dfaForConversion = def;
            }

            String regex = KleeneConverter.convert(dfaForConversion);
            
            JTextArea textArea = new JTextArea(conversionMessage + regex);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 250));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Generated Regular Expression", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating expression: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private AutomataDefinition buildDefinition() {
        Set<String> allStatesQ = generateStateSet();
        Set<String> alphabetSigma = generateSymbolSet();
        String initialStr = (String) cmbInitialState.getSelectedItem();
        Set<String> finalStates = getSelectedFinalStates();
        List<AutomataDefinition.TransitionData> transitionsDelta = getTransitionData();
        String typeStr = (String) cmbAutomataType.getSelectedItem();
        
        if (allStatesQ.size() > (Integer) spinNumStates.getValue()) {
            throw new IllegalArgumentException("Internal Error: State count mismatch.");
        }
        
        return AutomataParser.parse(allStatesQ, alphabetSigma, initialStr, finalStates, transitionsDelta, typeStr);
    }
    
    private Set<String> findReachableStates(AutomataDefinition def) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        String initialState = def.getInitialState();
        if (initialState != null && def.getStatesQ().contains(initialState)) {
            reachable.add(initialState);
            queue.add(initialState);
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            for (AutomataDefinition.TransitionData t : def.getTransitionsDelta()) {
                if (t.getSource().equals(current) && def.getStatesQ().contains(t.getDestination())) {
                    if (reachable.add(t.getDestination())) {
                        queue.add(t.getDestination());
                    }
                }
            }
        }
        return reachable;
    }
    
    private Set<String> generateStateSet() {
        int num = (Integer) spinNumStates.getValue();
        Set<String> states = new HashSet<>();
        for (int i = 1; i <= num; i++) states.add(DEFAULT_STATE_PREFIX[0] + i);
        return states;
    }

    private Set<String> generateSymbolSet() {
        int num = (Integer) spinNumVariables.getValue();
        Set<String> symbols = new HashSet<>();
        for (int i = 0; i < num; i++) {
             symbols.add(String.valueOf(i));
        }
        return symbols;
    }
    
    private List<State> createPositionalStates(AutomataDefinition def) {
        List<State> list = new ArrayList<>();
        String[] qNames = def.getStatesQ().toArray(new String[0]);
        Arrays.sort(qNames); 
        int n = qNames.length;
        
        int radiusPanel = Math.min(drawingPanel.getWidth(), drawingPanel.getHeight()) / 3;
        int centerX = drawingPanel.getWidth() / 2;
        int centerY = drawingPanel.getHeight() / 2;
        
        for (int i = 0; i < n; i++) {
            double angle = (2 * Math.PI / n) * i - (Math.PI / 2);
            int x = centerX + (int) (radiusPanel * Math.cos(angle));
            int y = centerY + (int) (radiusPanel * Math.sin(angle));
            
            State e = new State(qNames[i], x, y);
            
            if (qNames[i].equals(def.getInitialState())) {
                e.setInitial(true);
            }
            if (def.getFinalStates().contains(qNames[i])) {
                e.setFinal(true);
            }
            list.add(e);
        }
        return list;
    }
    
    private List<Transition> createTransitions(AutomataDefinition def) {
        List<Transition> list = new ArrayList<>();
        
        for (AutomataDefinition.TransitionData td : def.getTransitionsDelta()) {
            State source = stateMap.get(td.getSource());
            State destination = stateMap.get(td.getDestination());
            String symbol = td.getSymbol();
            
            if (source != null && destination != null) {
                list.add(new Transition(source, destination, symbol));
            }
        }
        return list;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AutomataDrawerGUI().setVisible(true);
        });
    }
}