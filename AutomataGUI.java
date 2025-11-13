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
        add(controlPanel, BorderLayout.NORTH);

        add(crearPanelInstrucciones(), BorderLayout.EAST);
    }

    private void validarAutomata() {
        String tipo = (String) cmbTipoAutomata.getSelectedItem();
        
        if (tipo.equals("AFD")) {
            validarAFD();
        } else if (tipo.equals("AFN")) {
            JOptionPane.showMessageDialog(this, "Validación para AFN aún no implementada.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Validación para AFN-Lambda aún no implementada.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void validarAFD() {
        List<Estado> estados = drawingPanel.getEstados();
        List<Transicion> transiciones = drawingPanel.getTransiciones();
        String alfabetoStr = txtAlfabeto.getText().replace(",", "").trim();
        
        Set<Character> alfabeto = new HashSet<>();
        for (char c : alfabetoStr.toCharArray()) {
            alfabeto.add(c);
        }

        long numIniciales = estados.stream().filter(Estado::esInicial).count();
        if (numIniciales == 0) {
            mostrarError("Error de AFD: No hay ningún estado inicial definido.");
            return;
        }
        if (numIniciales > 1) {
            mostrarError("Error de AFD: Hay más de un estado inicial.");
            return;
        }
        
        boolean hayLambda = transiciones.stream().anyMatch(t -> t.getSimbolo().equals("λ"));
        if (hayLambda) {
            mostrarError("Error de AFD: No se permiten transiciones lambda (λ).");
            return;
        }

        for (Estado e : estados) {
            for (Character s : alfabeto) {
                String simboloStr = String.valueOf(s);
                
                long numTransiciones = transiciones.stream()
                    .filter(t -> t.getOrigen() == e && t.getSimbolo().equals(simboloStr))
                    .count();

                if (numTransiciones == 0) {
                    mostrarError("Error de AFD (Determinismo): Al estado '" + e.getNombre() + "' le falta una transición para el símbolo '" + s + "'.");
                    return;
                }
                if (numTransiciones > 1) {
                    mostrarError("Error de AFD (Determinismo): El estado '" + e.getNombre() + "' tiene múltiples transiciones para el símbolo '" + s + "'.");
                    return;
                }
            }
        }
        for(Transicion t : transiciones) {
            if (t.getSimbolo().length() > 1 || !alfabeto.contains(t.getSimbolo().charAt(0))) {
                 mostrarError("Error: La transición '" + t.getSimbolo() + "' no pertenece al alfabeto Σ={" + alfabetoStr + "}.");
                 return;
            }
        }

        JOptionPane.showMessageDialog(this, "¡Autómata Válido!", "Validación Exitosa (AFD)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación", JOptionPane.ERROR_MESSAGE);
    }


    private JScrollPane crearPanelInstrucciones() {
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