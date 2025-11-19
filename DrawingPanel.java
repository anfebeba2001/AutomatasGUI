import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
import java.util.Map;
import java.util.stream.Collectors;

class DrawingPanel extends JPanel { 

    private List<State> states = new ArrayList<>();
    private List<Transition> transitions = new ArrayList<>();
    
    public DrawingPanel() {
        setBackground(Color.WHITE);
    }
    
    public void setStates(List<State> states) { this.states = states; }
    public void setTransitions(List<Transition> transitions) { this.transitions = transitions; }
    
    public void clearAll() {
        states.clear();
        transitions.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, List<Transition>> groupedTransitions = transitions.stream()
            .collect(Collectors.groupingBy(t -> t.getSource().getName() + "->" + t.getDestination().getName()));

        Map<String, List<Transition>> processedTransitions = new HashMap<>();
        
        for (Map.Entry<String, List<Transition>> entry : groupedTransitions.entrySet()) {
            String key = entry.getKey();
            List<Transition> group = entry.getValue();
            
            String[] parts = key.split("->");
            String reverseKey = parts[1] + "->" + parts[0];
            
            if (processedTransitions.containsKey(reverseKey)) {
                continue; 
            }
            
            String symbolLabel = group.stream()
                .map(Transition::getSymbol)
                .collect(Collectors.joining(","));
            
            if (parts[0].equals(parts[1]) || !groupedTransitions.containsKey(reverseKey)) {
                drawArrow(g2d, group.get(0).getSource(), group.get(0).getDestination(), symbolLabel, false);
            } 
            else {
                List<Transition> reverseGroup = groupedTransitions.get(reverseKey);
                String reverseSymbolLabel = reverseGroup.stream()
                    .map(Transition::getSymbol)
                    .collect(Collectors.joining(","));
                
                drawArrow(g2d, group.get(0).getSource(), group.get(0).getDestination(), symbolLabel, true);
                drawArrow(g2d, reverseGroup.get(0).getSource(), reverseGroup.get(0).getDestination(), reverseSymbolLabel, true);
                
                processedTransitions.put(key, group);
                processedTransitions.put(reverseKey, reverseGroup);
            }
        }
        
        for (State e : states) {
            g2d.setStroke(new BasicStroke(2)); 
            
            Color fillColor;
            if (e.isInitial() && e.isFinal()) {
                fillColor = new Color(255, 165, 0); 
            } else if (e.isInitial()) {
                fillColor = new Color(0, 150, 255); 
            } else if (e.isFinal()) {
                fillColor = new Color(0, 150, 50); 
            } else {
                 fillColor = new Color(200, 200, 200); 
            }
            
            g2d.setColor(fillColor);
            g2d.fillOval(e.getX() - e.getRadius(), e.getY() - e.getRadius(), e.getRadius() * 2, e.getRadius() * 2);
            
            g2d.setColor(Color.BLACK);
            g2d.drawOval(e.getX() - e.getRadius(), e.getY() - e.getRadius(), e.getRadius() * 2, e.getRadius() * 2);
            
            g2d.setColor(Color.BLACK); 
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 14));
            g2d.drawString(e.getName(), e.getX() - g2d.getFontMetrics().stringWidth(e.getName()) / 2, e.getY() + g2d.getFontMetrics().getHeight() / 4);

            if (e.isFinal()) { 
                g2d.setColor(Color.BLACK);
                g2d.drawOval(e.getX() - e.getRadius() + 5, e.getY() - e.getRadius() + 5, e.getRadius() * 2 - 10, e.getRadius() * 2 - 10);
            }
            
            if (e.isInitial()) { 
                g2d.setColor(Color.BLACK);
                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 20));
                g2d.drawString(">", e.getX() - e.getRadius() - 15, e.getY() + 5);
            }
        }
    }
    
    private void drawArrow(Graphics2D g2d, State source, State destination, String symbol, boolean isBidirectional) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));

        if (source == destination) {
            drawSelfLoop(g2d, source, symbol);
            return;
        }
        
        int x1 = source.getX();
        int y1 = source.getY();
        int x2 = destination.getX();
        int y2 = destination.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        
        double offsetDistance = isBidirectional ? 10 : 0; 

        double perpAngle = angle + Math.PI / 2;
        double offsetX = offsetDistance * Math.cos(perpAngle);
        double offsetY = offsetDistance * Math.sin(perpAngle);
        
        int mx1 = x1 + (int) offsetX;
        int my1 = y1 + (int) offsetY;
        int mx2 = x2 + (int) offsetX;
        int my2 = y2 + (int) offsetY;

        double startAngle = Math.atan2(my2 - my1, mx2 - mx1);
        int sx = (int) (mx1 + source.getRadius() * Math.cos(startAngle));
        int sy = (int) (my1 + source.getRadius() * Math.sin(startAngle));
        int ex = (int) (mx2 - destination.getRadius() * Math.cos(startAngle));
        int ey = (int) (my2 - destination.getRadius() * Math.sin(startAngle));
        
        g2d.drawLine(sx, sy, ex, ey);

        int arrowSize = 10;
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(ex, ey);
        arrowHead.addPoint((int) (ex - arrowSize * Math.cos(startAngle - Math.PI / 6)), (int) (ey - arrowSize * Math.sin(startAngle - Math.PI / 6)));
        arrowHead.addPoint((int) (ex - arrowSize * Math.cos(startAngle + Math.PI / 6)), (int) (ey - arrowSize * Math.sin(startAngle + Math.PI / 6)));
        g2d.fill(arrowHead);
        
        int midX = (sx + ex) / 2;
        int midY = (sy + ey) / 2;
        
        if (isBidirectional) {
             midX += (int) (5 * Math.cos(perpAngle)); 
             midY += (int) (5 * Math.sin(perpAngle)); 
        }

        Font originalFont = g2d.getFont();
        g2d.setFont(originalFont.deriveFont(Font.BOLD, 14));
        AffineTransform originalTransform = g2d.getTransform();
        
        g2d.translate(midX, midY);
        double textAngle = (startAngle > Math.PI/2 || startAngle < -Math.PI/2) ? startAngle + Math.PI : startAngle;
        g2d.rotate(textAngle);

        int textWidth = g2d.getFontMetrics().stringWidth(symbol);
        int textHeight = g2d.getFontMetrics().getHeight();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(-textWidth/2 - 2, -textHeight/2 - 5, textWidth + 4, textHeight);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(symbol, -textWidth/2, textHeight/4 - 5); 
        g2d.setTransform(originalTransform);
        g2d.setFont(originalFont);
    }

    private void drawSelfLoop(Graphics2D g2d, State state, String symbol) {
        int x = state.getX();
        int y = state.getY();
        int r = state.getRadius();
        
        int loopRadius = r;
        int loopX = x - loopRadius;
        int loopY = y - loopRadius - 20; 

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        
        g2d.drawOval(loopX, loopY, 2 * loopRadius, 2 * loopRadius);

        double angle = Math.toRadians(270); 
        int px = x - r;
        int py = y;
        
        int arrowSize = 10;
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(px, py);
        arrowHead.addPoint((int) (px + arrowSize * Math.cos(angle - Math.PI / 6)), (int) (py + arrowSize * Math.sin(angle - Math.PI / 6)));
        arrowHead.addPoint((int) (px + arrowSize * Math.cos(angle + Math.PI / 6)), (int) (py + arrowSize * Math.sin(angle + Math.PI / 6)));
        g2d.fill(arrowHead);
        
        Font originalFont = g2d.getFont();
        g2d.setFont(originalFont.deriveFont(Font.BOLD, 14));
        int textWidth = g2d.getFontMetrics().stringWidth(symbol);
        int textHeight = g2d.getFontMetrics().getHeight();
        int textX = x - textWidth / 2;
        int textY = loopY - 5; 

        g2d.setColor(Color.WHITE);
        g2d.fillRect(textX - 2, textY - textHeight + 5, textWidth + 4, textHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawString(symbol, textX, textY);
        g2d.setFont(originalFont);
    }
}