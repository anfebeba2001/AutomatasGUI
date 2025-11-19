public class RegularExpression {
    private String regex;

    public static final RegularExpression EMPTY = new RegularExpression("Ø");
    public static final RegularExpression LAMBDA = new RegularExpression("λ");

    public RegularExpression(String regex) {
        this.regex = (regex == null || regex.isEmpty()) ? "Ø" : regex;
    }

    public String toString() {
        return regex;
    }

    public RegularExpression union(RegularExpression other) {
        if (this.regex.equals("Ø")) return other;
        if (other.regex.equals("Ø")) return this;
        if (this.regex.equals(other.regex)) return this;

        return new RegularExpression("(" + this.regex + "+" + other.regex + ")");
    }

    public RegularExpression concatenate(RegularExpression other) {
        if (this.regex.equals("Ø") || other.regex.equals("Ø")) return EMPTY;
        if (this.regex.equals("λ")) return other;
        if (other.regex.equals("λ")) return this;

        return new RegularExpression(this.regex + other.regex);
    }

    public RegularExpression star() {
        if (this.regex.equals("Ø") || this.regex.equals("λ")) return LAMBDA;
        
        String inner = this.regex;
        if (inner.length() > 1 && inner.startsWith("(") && inner.endsWith(")")) {
            return new RegularExpression(inner + "*");
        } else if (inner.length() == 1) {
            return new RegularExpression(inner + "*");
        }
        
        return new RegularExpression("(" + inner + ")*");
    }
}