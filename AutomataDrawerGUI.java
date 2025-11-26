import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class AutomataDrawerGUI extends JFrame {

    private final JSpinner spinNumStates;
    private final JSpinner spinNumVariables;
    private final JComboBox<String> cmbInitialState;
    private final JList<String> listFinalStates;
    private final DrawingPanel drawingPanel;
    private final JTextArea txtRegexOutput;

    private JComboBox<String> cmbTransSrc;
    private JComboBox<String> cmbTransSym;
    private JComboBox<String> cmbTransDest;

    private final DefaultListModel<String> transitionListModel;
    private final JList<String> listTransitions;

    private Map<String, State> stateMap;
    private final String[] DEFAULT_STATE_PREFIX = {"s"};
    private static final int MAX_LIMIT = 10;
    
    private final JPanel controlPanel; 

    public AutomataDrawerGUI() {
        setTitle("Real-time DFA Designer");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setPreferredSize(new Dimension(420, 800));
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
        spinNumStates.addChangeListener(e -> {
            updateSelectionComponents();
            refreshAll();
        });
        controlPanel.add(spinNumStates, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.5;
        controlPanel.add(new JLabel("Count Symbols:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.5;
        spinNumVariables = new JSpinner(new SpinnerNumberModel(2, 1, MAX_LIMIT, 1));
        spinNumVariables.addChangeListener(e -> {
            updateSelectionComponents();
            refreshAll();
        });
        controlPanel.add(spinNumVariables, gbc);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        controlPanel.add(new JSeparator(), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Initial State:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbInitialState = new JComboBox<>();
        cmbInitialState.addActionListener(e -> refreshAll());
        controlPanel.add(cmbInitialState, gbc);

        gbc.gridx = 0; gbc.gridy = row++; 
        gbc.gridwidth = 2;
        controlPanel.add(new JLabel("Final States (Ctrl+Click):"), gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.weighty = 0.15; 
        gbc.fill = GridBagConstraints.BOTH;
        listFinalStates = new JList<>();
        listFinalStates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listFinalStates.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshAll();
        });
        JScrollPane scrollFinal = new JScrollPane(listFinalStates);
        scrollFinal.setPreferredSize(new Dimension(300, 100));
        controlPanel.add(scrollFinal, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(new JSeparator(), gbc);

        gbc.gridy = row++;
        controlPanel.add(new JLabel("Add Transitions:"), gbc);
        
        gbc.gridy = row++;
        controlPanel.add(createTransitionBuilderPanel(), gbc);
        
        gbc.gridy = row++; gbc.weighty = 0.4; gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(new JLabel("Transition List:"), gbc);
        gbc.gridy = row++;
        transitionListModel = new DefaultListModel<>();
        listTransitions = new JList<>(transitionListModel);
        listTransitions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTransitions = new JScrollPane(listTransitions);
        scrollTransitions.setPreferredSize(new Dimension(300, 150));
        controlPanel.add(scrollTransitions, gbc);

        gbc.gridy = row++; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(new JLabel("Calculated Regular Expression:"), gbc);
        
        gbc.gridy = row++; gbc.weighty = 0.3; gbc.fill = GridBagConstraints.BOTH;
        txtRegexOutput = new JTextArea();
        txtRegexOutput.setEditable(false);
        txtRegexOutput.setLineWrap(true);
        txtRegexOutput.setWrapStyleWord(true);
        txtRegexOutput.setFont(new Font("Monospaced", Font.BOLD, 16));
        txtRegexOutput.setBackground(new Color(240, 240, 240));
        txtRegexOutput.setBorder(BorderFactory.createEtchedBorder());
        controlPanel.add(new JScrollPane(txtRegexOutput), gbc);

        drawingPanel = new DrawingPanel();
        drawingPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        add(controlPanel, BorderLayout.WEST);
        add(drawingPanel, BorderLayout.CENTER);
        
        updateSelectionComponents();
        refreshAll();
    }
    
    private void refreshAll() {
        try {
            AutomataDefinition def = buildDefinition();
            
            drawAutomaton(def);
         
            String regex = KleeneConverter.convert(def);
            txtRegexOutput.setText(regex);
            txtRegexOutput.setForeground(new Color(0, 100, 0));

        } catch (Exception ex) {
            if (!ex.getMessage().contains("Internal Error")) {
                txtRegexOutput.setText("..."); 
                txtRegexOutput.setForeground(Color.GRAY);
            }
        }
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
        
        String[] stateArray = stateNames.toArray(String[]::new);
        String[] symbolArray = alphabetSymbols.toArray(String[]::new);

        Object currentInit = cmbInitialState.getSelectedItem();
        cmbInitialState.setModel(new DefaultComboBoxModel<>(stateArray));
        if (stateNames.contains(currentInit)) cmbInitialState.setSelectedItem(currentInit);
        else if (stateNames.contains("s1")) cmbInitialState.setSelectedItem("s1"); 

        List<String> currentFinals = listFinalStates.getSelectedValuesList();
        DefaultListModel<String> finalStateModel = new DefaultListModel<>();
        stateNames.forEach(finalStateModel::addElement);
        listFinalStates.setModel(finalStateModel);
        
        int[] indices = currentFinals.stream()
                .mapToInt(stateNames::indexOf)
                .filter(i -> i >= 0)
                .toArray();
        listFinalStates.setSelectedIndices(indices);
        
        if (cmbTransSrc != null && cmbTransSym != null && cmbTransDest != null) {
            Object src = cmbTransSrc.getSelectedItem();
            Object sym = cmbTransSym.getSelectedItem();
            Object dest = cmbTransDest.getSelectedItem();

            cmbTransSrc.setModel(new DefaultComboBoxModel<>(stateArray));
            cmbTransDest.setModel(new DefaultComboBoxModel<>(stateArray));
            cmbTransSym.setModel(new DefaultComboBoxModel<>(symbolArray));
            
            if (src != null) cmbTransSrc.setSelectedItem(src);
            if (sym != null) cmbTransSym.setSelectedItem(sym);
            if (dest != null) cmbTransDest.setSelectedItem(dest);
        }
        
     
        boolean removed = false;
        for (int i = transitionListModel.getSize() - 1; i >= 0; i--) {
            String t = transitionListModel.get(i);
            String[] parts = t.split(",");
            if (!stateNames.contains(parts[0]) || !stateNames.contains(parts[2]) || !alphabetSymbols.contains(parts[1])) {
                transitionListModel.remove(i);
                removed = true;
            }
        }
        if (removed) refreshAll();
    }

    private JPanel createTransitionBuilderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 5, 5)); 
        
        cmbTransSrc = new JComboBox<>();
        cmbTransSym = new JComboBox<>();
        cmbTransDest = new JComboBox<>();

        JButton btnAdd = new JButton("Add [+]");
        btnAdd.addActionListener(e -> {
            addTransition((String)cmbTransSrc.getSelectedItem(), (String)cmbTransSym.getSelectedItem(), (String)cmbTransDest.getSelectedItem());
            refreshAll();
        });
        
        JButton btnRemove = new JButton("Del [-]");
        btnRemove.addActionListener(e -> {
            removeSelectedTransition();
            refreshAll();
        });

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
        
        String transition = src + "," + symbol + "," + dest;
        
        if (transitionListModel.contains(transition)) return;
        
        for (int i = 0; i < transitionListModel.getSize(); i++) {
            String existing = transitionListModel.getElementAt(i);
            String[] parts = existing.split(",");
            if (parts[0].equals(src) && parts[1].equals(symbol)) {
                transitionListModel.remove(i); 
                break;
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

    private void drawAutomaton(AutomataDefinition def) {
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
        drawingPanel.repaint();
    }

    private AutomataDefinition buildDefinition() {
        Set<String> allStatesQ = generateStateSet();
        Set<String> alphabetSigma = generateSymbolSet();
        String initialStr = (String) cmbInitialState.getSelectedItem();
        Set<String> finalStates = getSelectedFinalStates();
        List<AutomataDefinition.TransitionData> transitionsDelta = getTransitionData();
        
        if (allStatesQ.size() > (Integer) spinNumStates.getValue()) {
            throw new IllegalArgumentException("Internal Error");
        }
        if (initialStr == null) throw new IllegalArgumentException("No Initial");
        
        return AutomataParser.parse(allStatesQ, alphabetSigma, initialStr, finalStates, transitionsDelta, "DFA");
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