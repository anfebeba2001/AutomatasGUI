import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AutomataGUI extends JFrame {

    private DrawingPanel drawingPanel;
    private JComboBox<String> cmbTipoAutomata;
    private JTextField txtAlfabeto;

    public AutomataGUI() {
        setTitle("Diseñador de Autómatas (AFD, AFN, AFN-λ)");
        setSize(1000, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        cmbTipoAutomata = new JComboBox<>(new String[]{"AFD", "AFN", "AFN-Lambda"});
        controlPanel.add(new JLabel("Tipo:"));
        controlPanel.add(cmbTipoAutomata);

        controlPanel.add(new JLabel("     Alfabeto (Σ) [ej: a,b]:"));
        txtAlfabeto = new JTextField(15);
        controlPanel.add(txtAlfabeto);
        
        drawingPanel = new DrawingPanel(cmbTipoAutomata, txtAlfabeto);
        add(drawingPanel, BorderLayout.CENTER);

        JButton btnClear = new JButton("Limpiar Todo");
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres limpiar todo?", "Confirmar Limpieza", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                drawingPanel.limpiarTodo();
            }
        });
        controlPanel.add(btnClear);

        JButton btnProcess = new JButton("Validar Autómata");
        btnProcess.addActionListener(e -> {
            validarAutomata();
        });
        controlPanel.add(btnProcess);
        
        // --- NUEVA FUNCIONALIDAD: Generar Expresión Regular ---
        JButton btnER = new JButton("Generar Exp. Regular (AFD)");
        btnER.addActionListener(e -> {
            generarExpresionRegular();
        });
        controlPanel.add(btnER);
        // ----------------------------------------------------

        add(controlPanel, BorderLayout.NORTH);

        add(crearPanelInstrucciones(), BorderLayout.EAST);
    }

    private void validarAutomata() {
        String tipo = (String) cmbTipoAutomata.getSelectedItem();
        
        if (tipo.equals("AFD")) {
            if (validarAFD_Logico()) {
                 JOptionPane.showMessageDialog(this, "¡Autómata Válido!", "Validación Exitosa (AFD)", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (tipo.equals("AFN")) {
            JOptionPane.showMessageDialog(this, "Validación para AFN aún no implementada.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Validación para AFN-Lambda aún no implementada.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // ----------------------------------------------------------------------------------
    // ⬇️ LÓGICA DE GENERACIÓN DE EXPRESIÓN REGULAR (PARTE CLAVE) ⬇️
    // ----------------------------------------------------------------------------------

    private void generarExpresionRegular() {
        String tipo = (String) cmbTipoAutomata.getSelectedItem();
        
        // 1. Verificar si el tipo de autómata es compatible con la conversión implementada
        if (!tipo.equals("AFD")) {
            JOptionPane.showMessageDialog(this, "La conversión a Expresión Regular solo está implementada para AFD.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. Validar que el autómata sea un AFD válido
        if (!validarAFD_Logico()) { 
            mostrarError("El autómata debe ser un AFD válido para generar la Expresión Regular. Corrija los errores de validación.");
            return;
        }

        try {
            // 3. Ejecutar el Algoritmo de Kleene (Eliminación de Estados)
            KleeneConverter converter = new KleeneConverter(drawingPanel.getEstados(), drawingPanel.getTransiciones());
            ExpresionRegular er = converter.getExpresionRegular();

            // 4. Mostrar el resultado
            JTextArea textArea = new JTextArea(er.getER());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 200));

            JOptionPane.showMessageDialog(this, 
                                          scrollPane, 
                                          "Resultado del Teorema de Kleene (ER)", 
                                          JOptionPane.INFORMATION_MESSAGE);
                                          
        } catch (Exception ex) {
            // Manejar errores como "No se encontró estado inicial", etc.
            mostrarError("Error al generar la Expresión Regular: " + ex.getMessage());
        }
    }

    // ----------------------------------------------------------------------------------
    // ⬇️ LÓGICA DE VALIDACIÓN COMPARTIDA (Usada por validarAutomata y generarExpresionRegular) ⬇️
    // ----------------------------------------------------------------------------------
    
    // Lógica de validación que retorna un booleano y muestra el error.
    private boolean validarAFD_Logico() {
        List<Estado> estados = drawingPanel.getEstados();
        List<Transicion> transiciones = drawingPanel.getTransiciones();
        String alfabetoStr = txtAlfabeto.getText().replace(",", "").trim();
        
        // 1. Alfabeto
        Set<Character> alfabeto = new HashSet<>();
        for (char c : alfabetoStr.toCharArray()) {
            alfabeto.add(c);
        }
        if (alfabeto.isEmpty()) {
            mostrarError("Error: El alfabeto (Σ) no puede estar vacío.");
            return false;
        }

        // 2. Iniciales
        long numIniciales = estados.stream().filter(Estado::esInicial).count();
        if (numIniciales != 1) {
            mostrarError("Error de AFD: Debe haber exactamente un estado inicial.");
            return false;
        }
        
        // 3. Lambda
        boolean hayLambda = transiciones.stream().anyMatch(t -> t.getSimbolo().equals("λ"));
        if (hayLambda) {
            mostrarError("Error de AFD: No se permiten transiciones lambda (λ).");
            return false;
        }
        
        // 4. Determinismo y Completitud
        for (Estado e : estados) {
            for (Character s : alfabeto) {
                String simboloStr = String.valueOf(s);
                
                long numTransiciones = transiciones.stream()
                    .filter(t -> t.getOrigen() == e && t.getSimbolo().equals(simboloStr))
                    .count();

                if (numTransiciones != 1) {
                    mostrarError("Error de AFD (Determinismo): El estado '" + e.getNombre() + "' debe tener **una y solo una** transición para el símbolo '" + s + "'. Hay " + numTransiciones + ".");
                    return false;
                }
            }
        }
        
        // 5. Transiciones válidas (solo se permiten símbolos del alfabeto)
        for(Transicion t : transiciones) {
             if (t.getSimbolo().length() > 1 || !alfabeto.contains(t.getSimbolo().charAt(0))) {
                 mostrarError("Error: La transición '" + t.getSimbolo() + "' no pertenece al alfabeto Σ={" + alfabetoStr + "}.");
                 return false;
             }
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación/Conversión", JOptionPane.ERROR_MESSAGE);
    }


    private JScrollPane crearPanelInstrucciones() {
        // ... (Este método queda igual que tu código original) ...
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(getBackground()); 

        String instruccionesHTML = "<html>" +
            "<body style='font-family: Arial, sans-serif; font-size: 11pt; padding: 5px;'>" +
            "<h3>Instrucciones de Uso</h3>" +
            "<p><b>Definir Alfabeto:</b><br>" +
            "Escribir los símbolos (ej: a,b) en el campo 'Alfabeto' superior.</p>" +
            "<p><b>Añadir Estado:</b><br>" +
            "Clic izquierdo en un espacio vacío.</p>" +
            "<p><b>Configurar Estado:</b><br>" +
            "Doble clic sobre un estado para editar nombre, inicial o final.</p>" +
            "<p><b>Añadir Transición:</b><br>" +
            "Mantener <b>SHIFT</b>, luego clic y arrastrar desde el estado origen al estado destino.</p>" +
            "<hr><p><b><font color='red'>ELIMINAR ESTADO:</font></b><br>" +
            "<b>Clic derecho</b> sobre el estado.</p>" +
            "<p><b><font color='red'>ELIMINAR TRANSICIÓN:</font></b><br>" +
            "<b>Clic derecho</b> sobre el <u>símbolo</u> de la transición.</p>" +
            "</body></html>";
        
        textPane.setText(instruccionesHTML);
        textPane.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(250, 0)); 
        scrollPane.setBorder(BorderFactory.createTitledBorder("Ayuda"));
        
        return scrollPane;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AutomataGUI().setVisible(true);
        });
    }
}