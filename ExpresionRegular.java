public class ExpresionRegular {
    private String er;

    // Constantes para los elementos especiales
    public static final ExpresionRegular VACIO = new ExpresionRegular("Ø"); // Conjunto vacío
    public static final ExpresionRegular LAMBDA = new ExpresionRegular("λ"); // Cadena vacía (epsilon)

    public ExpresionRegular(String er) {
        // Normaliza "()" y otros casos triviales
        this.er = (er == null || er.trim().isEmpty()) ? LAMBDA.er : er.trim();
        if (this.er.equals("()")) this.er = LAMBDA.er;
    }

    public String getER() {
        return er;
    }
    
    // Método para la UNIÓN (Suma: R1 + R2)
    public ExpresionRegular union(ExpresionRegular other) {
        // Propiedades de la unión
        if (this.equals(other)) return this;
        if (this.equals(VACIO)) return other;
        if (other.equals(VACIO)) return this;

        // Normalizar la representación (ej: a+b en lugar de (a+b))
        String r1 = this.er;
        String r2 = other.er;
        
        // Evitar doble paréntesis si ya es una unión (ej: (a+b)+(c+d) -> a+b+c+d)
        if (r1.contains("+") && r1.startsWith("(") && r1.endsWith(")")) {
            r1 = r1.substring(1, r1.length() - 1);
        }
        if (r2.contains("+") && r2.startsWith("(") && r2.endsWith(")")) {
            r2 = r2.substring(1, r2.length() - 1);
        }
        
        // Ordenar alfabéticamente para canonicalizar: {a, b} -> a+b, {b, a} -> a+b
        String result;
        if (r1.compareTo(r2) < 0) {
            result = r1 + "+" + r2;
        } else {
            result = r2 + "+" + r1;
        }

        // Si la expresión original ya contenía operadores de Kleene o Concatenación, 
        // necesita paréntesis para la nueva unión, pero por simplicidad solo se envuelve
        // si tiene un operador de unión
        // return new ExpresionRegular("(" + r1 + "+" + r2 + ")");
        return new ExpresionRegular(result);
    }
    
    // Método para la CONCATENACIÓN (Producto: R1 · R2)
    public ExpresionRegular concatenacion(ExpresionRegular other) {
        if (this.equals(VACIO) || other.equals(VACIO)) return VACIO;
        if (this.equals(LAMBDA)) return other;
        if (other.equals(LAMBDA)) return this;

        String r1 = this.er;
        String r2 = other.er;
        
        // Agregar paréntesis si la expresión ya es una unión antes de concatenar
        if (r1.contains("+")) r1 = "(" + r1 + ")";
        if (r2.contains("+")) r2 = "(" + r2 + ")";

        return new ExpresionRegular(r1 + r2);
    }
    
    // Método para la CERRADURA DE KLEENE (Estrella: R*)
    public ExpresionRegular cerraduraKleene() {
        if (this.equals(VACIO) || this.equals(LAMBDA)) return LAMBDA; // Ø* = λ, λ* = λ
        
        String r = this.er;
        if (r.length() > 1 && !r.endsWith("*")) {
             r = "(" + r + ")";
        }
        return new ExpresionRegular(r + "*");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpresionRegular that = (ExpresionRegular) o;
        // La comparación debe ser más robusta, pero para el prototipo usamos la cadena
        return er.equals(that.er);
    }

    @Override
    public String toString() {
        return er;
    }
}