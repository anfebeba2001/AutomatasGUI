import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D; // Importar para el arco
import java.util.ArrayList; 
import java.util.Iterator;
import java.util.List; 
import javax.swing.*;

class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener {

    private final List<Estado> estados;
    private final List<Transicion> transiciones;
    
    private Estado estadoSeleccionado;
    private Estado origenArrastrandoTransicion;
    
    private int mouseOffsetX, mouseOffsetY; 

    // --- NUEVO: Referencias a los controles de la GUI principal ---
    private JComboBox<String> cmbTipoAutomata;
    private JTextField txtAlfabeto;

    public DrawingPanel(JComboBox<String> cmbTipo, JTextField txtAlfabeto) {
        this.estados = new ArrayList<>();
        this.transiciones = new ArrayList<>();
        this.cmbTipoAutomata = cmbTipo;
        this.txtAlfabeto = txtAlfabeto;
        
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // (limpiarTodo, getEstados, getTransiciones, paintComponent no cambian)
    // ... (copia los de tu versión anterior) ...
    public void limpiarTodo() {
        estados.clear();
        transiciones.clear();
        repaint();
    }
    public List<Estado> getEstados() { return estados; }
    public List<Transicion> getTransiciones() { return transiciones; }
    
    @Override
    protected void paintComponent(Graphics g) {
        // (Este método es el mismo de la respuesta anterior,
        // con la corrección del color de relleno)
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Transicion t : transiciones) {
            drawArrow(g2d, t.getOrigen(), t.getDestino(), t.getSimbolo());
        }
        
        if (origenArrastrandoTransicion != null && estadoSeleccionado == null) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); 
            g2d.drawLine(origenArrastrandoTransicion.getX(), origenArrastrandoTransicion.getY(), 
                         getMousePosition() != null ? getMousePosition().x : origenArrastrandoTransicion.getX(), 
                         getMousePosition() != null ? getMousePosition().y : origenArrastrandoTransicion.getY());
        }

        for (Estado e : estados) {
            g2d.setStroke(new BasicStroke(2)); 
            Color colorEstado = e.isSeleccionado() ? new Color(50, 150, 255) : Color.BLACK;
            g2d.setColor(colorEstado);
            g2d.fillOval(e.getX() - e.getRadio(), e.getY() - e.getRadio(), e.getRadio() * 2, e.getRadio() * 2);
            
            g2d.setColor(Color.WHITE); 
            g2d.drawString(e.getNombre(), e.getX() - g2d.getFontMetrics().stringWidth(e.getNombre()) / 2, e.getY() + g2d.getFontMetrics().getHeight() / 4);

            if (e.esFinal()) { 
                g2d.setColor(Color.WHITE);
                g2d.drawOval(e.getX() - e.getRadio() + 5, e.getY() - e.getRadio() + 5, e.getRadio() * 2 - 10, e.getRadio() * 2 - 10);
            }
            
            if (e.esInicial()) { 
                g2d.setColor(Color.BLACK);
                g2d.drawLine(e.getX() - e.getRadio() - 20, e.getY(), e.getX() - e.getRadio(), e.getY());
                g2d.drawLine(e.getX() - e.getRadio() - 20, e.getY(), e.getX() - e.getRadio() - 15, e.getY() - 5);
                g2d.drawLine(e.getX() - e.getRadio() - 20, e.getY(), e.getX() - e.getRadio() - 15, e.getY() + 5);
            }
        }
    }


    /**
     * Dibuja una flecha (o un bucle) para una transición.
     * --- ACTUALIZADO ---
     */
    private void drawArrow(Graphics2D g2d, Estado origen, Estado destino, String simbolo) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));

        // --- NUEVO: Comprobar si es un autoenlace (bucle) ---
        if (origen == destino) {
            drawSelfLoop(g2d, origen, simbolo);
            return; // No hacer nada más
        }
        
        // (El código de abajo es el original para flechas normales)
        int x1 = origen.getX();
        int y1 = origen.getY();
        int x2 = destino.getX();
        int y2 = destino.getY();

        double angle = Math.atan2(y2 - y1, x2 - x1);
        int sx = (int) (x1 + origen.getRadio() * Math.cos(angle));
        int sy = (int) (y1 + origen.getRadio() * Math.sin(angle));
        int ex = (int) (x2 - destino.getRadio() * Math.cos(angle));
        int ey = (int) (y2 - destino.getRadio() * Math.sin(angle));
        
        g2d.drawLine(sx, sy, ex, ey);

        // ... (resto del método drawArrow, igual que antes) ...
        int arrowSize = 10;
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(ex, ey);
        arrowHead.addPoint((int) (ex - arrowSize * Math.cos(angle - Math.PI / 6)), (int) (ey - arrowSize * Math.sin(angle - Math.PI / 6)));
        arrowHead.addPoint((int) (ex - arrowSize * Math.cos(angle + Math.PI / 6)), (int) (ey - arrowSize * Math.sin(angle + Math.PI / 6)));
        g2d.fill(arrowHead);
        
        int midX = (sx + ex) / 2;
        int midY = (sy + ey) / 2;
        
        Font originalFont = g2d.getFont();
        g2d.setFont(originalFont.deriveFont(Font.BOLD, 14));
        AffineTransform originalTransform = g2d.getTransform();
        
        g2d.translate(midX, midY);
        double textAngle = (angle > Math.PI/2 || angle < -Math.PI/2) ? angle + Math.PI : angle;
        g2d.rotate(textAngle);

        int textWidth = g2d.getFontMetrics().stringWidth(simbolo);
        int textHeight = g2d.getFontMetrics().getHeight();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(-textWidth/2 - 2, -textHeight/2 - 5, textWidth + 4, textHeight);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(simbolo, -textWidth/2, textHeight/4 - 5); 
        g2d.setTransform(originalTransform);
        g2d.setFont(originalFont);
    }

    /**
     * --- NUEVO: Método para dibujar autoenlaces (bucles) ---
     */
    private void drawSelfLoop(Graphics2D g2d, Estado estado, String simbolo) {
        int x = estado.getX();
        int y = estado.getY();
        int r = estado.getRadio();
        
        // Dimensiones y posición del arco (un óvalo sobre el estado)
        int arcW = r;
        int arcH = r * 2;
        int arcX = x - arcW / 2;
        int arcY = y - r - arcH; // Colocado encima del estado

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        
        // Dibujar el arco (el bucle)
        Arc2D.Double arc = new Arc2D.Double(arcX, arcY, arcW, arcH, -70, -220, Arc2D.OPEN);
        g2d.draw(arc);

        // Dibujar la punta de la flecha en el inicio del arco
        double startAngle = Math.toRadians(-70);
        int ex = (int) (arcX + arcW / 2 + (arcW / 2 * Math.cos(startAngle)));
        int ey = (int) (arcY + arcH / 2 + (arcH / 2 * Math.sin(startAngle)));
        double angle = Math.toRadians(20); // Ángulo de la flecha

        int arrowSize = 10;
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(ex, ey);
        arrowHead.addPoint((int) (ex - arrowSize * Math.cos(angle - Math.PI / 6)), (int) (ey - arrowSize * Math.sin(angle - Math.PI / 6)));
        arrowHead.addPoint((int) (ex - arrowSize * Math.cos(angle + Math.PI / 6)), (int) (ey - arrowSize * Math.sin(angle + Math.PI / 6)));
        g2d.fill(arrowHead);
        
        // Dibujar el símbolo de la transición
        Font originalFont = g2d.getFont();
        g2d.setFont(originalFont.deriveFont(Font.BOLD, 14));
        int textWidth = g2d.getFontMetrics().stringWidth(simbolo);
        int textHeight = g2d.getFontMetrics().getHeight();
        int textX = x - textWidth / 2;
        int textY = arcY - textHeight / 2 + 5; // Encima del arco

        g2d.setColor(Color.WHITE);
        g2d.fillRect(textX - 2, textY - textHeight + 5, textWidth + 4, textHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawString(simbolo, textX, textY);
        g2d.setFont(originalFont);
    }
    
    // (mouseClicked y handleRightClick no cambian)
    // ... (copia los de tu versión anterior) ...
    @Override
    public void mouseClicked(MouseEvent e) {
        
        if (e.getButton() == MouseEvent.BUTTON3) {
            handleRightClick(e);
            return; 
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() == 1) { 
                boolean estadoClickeado = false;
                for (Estado estado : estados) {
                    if (estado.contienePunto(e.getX(), e.getY())) {
                        estadoClickeado = true;
                        break;
                    }
                }
                if (!estadoClickeado) { 
                    String nombre = "q" + estados.size();
                    estados.add(new Estado(nombre, e.getX(), e.getY()));
                }
            } else if (e.getClickCount() == 2) {  
                for (Estado estado : estados) {
                    if (estado.contienePunto(e.getX(), e.getY())) {
                        JTextField nombreField = new JTextField(estado.getNombre());
                        JCheckBox inicialCheck = new JCheckBox("¿Es estado inicial?", estado.esInicial());
                        JCheckBox finalCheck = new JCheckBox("¿Es estado final?", estado.esFinal());
                        
                        Object[] message = {
                            "Nombre:", nombreField,
                            inicialCheck,
                            finalCheck
                        };

                        int option = JOptionPane.showConfirmDialog(this, message, "Editar Estado", JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            String nuevoNombre = nombreField.getText().trim();
                            if (!nuevoNombre.isEmpty()) {
                                estado.nombre = nuevoNombre;
                            }
                            
                            // *** AQUÍ ESTÁ LA LÓGICA DE ESTADO INICIAL ÚNICO ***
                            if (inicialCheck.isSelected()) {
                                // Desmarcar cualquier otro estado inicial
                                estados.forEach(s -> s.setInicial(false));
                                estado.setInicial(true);
                            } else {
                                // Si desmarcan el inicial, se queda sin inicial
                                estado.setInicial(false);
                            }
                            
                            estado.setFinal(finalCheck.isSelected());
                        }
                        break;
                    }
                }
            }
            repaint();
        }
    }
    
    private void handleRightClick(MouseEvent e) {
        // (Este método es el mismo de la respuesta anterior)
        for (Estado estado : estados) {
            if (estado.contienePunto(e.getX(), e.getY())) {
                int a = JOptionPane.showConfirmDialog(this, 
                    "¿Eliminar estado '" + estado.getNombre() + "'?\n(También se eliminarán sus transiciones)", 
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                
                if (a == JOptionPane.YES_OPTION) {
                    Iterator<Transicion> it = transiciones.iterator();
                    while (it.hasNext()) {
                        Transicion t = it.next();
                        if (t.getOrigen() == estado || t.getDestino() == estado) {
                            it.remove();
                        }
                    }
                    estados.remove(estado);
                    repaint();
                    return; 
                }
            }
        }
        
        Iterator<Transicion> it = transiciones.iterator();
        while (it.hasNext()) {
            Transicion t = it.next();
            // Evitar comprobar hitbox de autoenlace (más complejo)
            if(t.getOrigen() == t.getDestino()) continue; 
            
            int x1 = t.getOrigen().getX(); int y1 = t.getOrigen().getY();
            int x2 = t.getDestino().getX(); int y2 = t.getDestino().getY();
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int sx = (int) (x1 + t.getOrigen().getRadio() * Math.cos(angle));
            int sy = (int) (y1 + t.getOrigen().getRadio() * Math.sin(angle));
            int ex = (int) (x2 - t.getDestino().getRadio() * Math.cos(angle));
            int ey = (int) (y2 - t.getDestino().getRadio() * Math.sin(angle));
            int midX = (sx + ex) / 2;
            int midY = (sy + ey) / 2;
            
            int w = getFontMetrics(getFont().deriveFont(Font.BOLD, 14)).stringWidth(t.getSimbolo()) + 10;
            int h = getFontMetrics(getFont().deriveFont(Font.BOLD, 14)).getHeight() + 10;
            
            Rectangle hitBox = new Rectangle(midX - w/2, midY - h/2 - 5, w, h);

            if (hitBox.contains(e.getX(), e.getY())) {
                int a = JOptionPane.showConfirmDialog(this, 
                    "¿Eliminar transición '" + t.getSimbolo() + "' de " + t.getOrigen().getNombre() + " a " + t.getDestino().getNombre() + "?", 
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                
                if (a == JOptionPane.YES_OPTION) {
                    it.remove(); 
                    repaint();
                    return;
                }
            }
        }
    }


    // (mousePressed no cambia)
    @Override
    public void mousePressed(MouseEvent e) {
        // (Este método es el mismo de la respuesta anterior)
        if (e.getButton() == MouseEvent.BUTTON1) {
            for (Estado estado : estados) {
                if (estado.contienePunto(e.getX(), e.getY())) {
                    if (e.isShiftDown()) {
                        origenArrastrandoTransicion = estado;
                        estadoSeleccionado = null;
                    } else {
                        estadoSeleccionado = estado;
                        mouseOffsetX = e.getX() - estado.getX();
                        mouseOffsetY = e.getY() - estado.getY();
                        estado.setSeleccionado(true);
                        repaint(); 
                    }
                    return; 
                }
            }
        }
    }


    /**
     * --- ACTUALIZADO: con validación de alfabeto ---
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (estadoSeleccionado != null && e.getButton() == MouseEvent.BUTTON1) { 
            estadoSeleccionado.setSeleccionado(false);
            estadoSeleccionado = null;
            repaint();
        } else if (origenArrastrandoTransicion != null && e.getButton() == MouseEvent.BUTTON1) {
            for (Estado destino : estados) {
                if (destino.contienePunto(e.getX(), e.getY())) {
                    String simboloInput = JOptionPane.showInputDialog(this,
                            "Símbolo(s) para " + origenArrastrandoTransicion.getNombre() + " -> " + destino.getNombre() + ":\n(Use ',' para varios; deje vacío para λ)", "");
                    
                    if (simboloInput != null) { // Si el usuario no presionó "Cancelar"
                        
                        // --- NUEVO: Validación de Alfabeto ---
                        String tipoAutomata = (String) cmbTipoAutomata.getSelectedItem();
                        String alfabetoStr = txtAlfabeto.getText().replace(",", "").trim();
                        
                        String[] simbolos = simboloInput.trim().isEmpty() ? 
                                            new String[]{"λ"} : // Si está vacío, es lambda
                                            simboloInput.split(","); // Si no, separar por comas

                        for (String s : simbolos) {
                            String simbolo = s.trim();
                            if (simbolo.isEmpty()) continue;

                            // 1. Validar Lambda
                            if (simbolo.equals("λ")) {
                                if (tipoAutomata.equals("AFD")) {
                                    JOptionPane.showMessageDialog(this, "Error: Un AFD no puede tener transiciones lambda (λ).", "Error de Símbolo", JOptionPane.ERROR_MESSAGE);
                                    continue; // No añadir esta transición
                                }
                            }
                            // 2. Validar contra el Alfabeto
                            else if (alfabetoStr.isEmpty() || !alfabetoStr.contains(simbolo)) {
                                JOptionPane.showMessageDialog(this, 
                                    "Error: El símbolo '" + simbolo + "' no pertenece al alfabeto Σ={" + (alfabetoStr.isEmpty() ? "vacío" : alfabetoStr) + "}.", 
                                    "Error de Símbolo", JOptionPane.ERROR_MESSAGE);
                                continue; // No añadir esta transición
                            }
                            
                            // Si pasa la validación, añadir la transición
                            transiciones.add(new Transicion(origenArrastrandoTransicion, destino, simbolo));
                        }
                    }
                    break; // Salir del bucle de estados
                }
            }
            origenArrastrandoTransicion = null;
            repaint();
        }
    }

    // (mouseDragged, mouseEntered, mouseExited, mouseMoved no cambian)
    // ... (copia los de tu versión anterior) ...
    @Override
    public void mouseDragged(MouseEvent e) {
        if (estadoSeleccionado != null) {
            estadoSeleccionado.setX(e.getX() - mouseOffsetX);
            estadoSeleccionado.setY(e.getY() - mouseOffsetY);
            repaint();
        } else if (origenArrastrandoTransicion != null) {
            repaint();
        }
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}