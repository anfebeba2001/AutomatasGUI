import java.util.*;
import java.util.stream.Collectors;

public abstract class RegularExpression implements Comparable<RegularExpression> {

    public static final RegularExpression EMPTY = new Empty();
    public static final RegularExpression LAMBDA = new Lambda();
    public static RegularExpression symbol(String s) {
        return new Symbol(s);
    }

    public static RegularExpression union(RegularExpression... exprs) {
        Set<RegularExpression> set = new TreeSet<>();
        for (RegularExpression e : exprs) {
            if (e instanceof Union) set.addAll(((Union) e).elements);
            else set.add(e);
        }
        set.remove(EMPTY);

        if (set.isEmpty()) return EMPTY;
        if (set.size() == 1) return set.iterator().next();

        return new Union(set);
    }

    public static RegularExpression concat(RegularExpression a, RegularExpression b) {
        if (a == EMPTY || b == EMPTY) return EMPTY;
        if (a == LAMBDA) return b;
        if (b == LAMBDA) return a;

        List<RegularExpression> list = new ArrayList<>();
        if (a instanceof Concat) list.addAll(((Concat) a).elements);
        else list.add(a);
        
        if (b instanceof Concat) list.addAll(((Concat) b).elements);
        else list.add(b);

        return new Concat(list);
    }

    public static RegularExpression star(RegularExpression e) {
        if (e == EMPTY) return LAMBDA;
        if (e == LAMBDA) return LAMBDA;
        if (e instanceof Star) return e;     
        return new Star(e);
    }

    public abstract String toString();
 
    @Override 
    public abstract int compareTo(RegularExpression o); 
    private static class Empty extends RegularExpression {
        public String toString() { return "Ø"; }
        public int compareTo(RegularExpression o) { return o instanceof Empty ? 0 : -1; }
    }

    private static class Lambda extends RegularExpression {
        public String toString() { return "λ"; }
        public int compareTo(RegularExpression o) { 
            if (o instanceof Empty) return 1;
            return o instanceof Lambda ? 0 : -1; 
        }
    }

    private static class Symbol extends RegularExpression {
        final String val;
        Symbol(String v) { this.val = v; }
        public String toString() { return val; }
        public int compareTo(RegularExpression o) {
            if (o instanceof Empty || o instanceof Lambda) return 1;
            if (o instanceof Symbol) return val.compareTo(((Symbol)o).val);
            return -1;
        }
        public boolean equals(Object o) { return o instanceof Symbol && val.equals(((Symbol)o).val); }
        public int hashCode() { return val.hashCode(); }
    }

    private static class Union extends RegularExpression {
        final Set<RegularExpression> elements;
        Union(Set<RegularExpression> e) { this.elements = e; }
        public String toString() {
            return elements.stream().map(Object::toString).collect(Collectors.joining("+"));
        }
        public int compareTo(RegularExpression o) {
            if (o instanceof Union) return this.toString().compareTo(o.toString());  
            return 1;
        }
        public boolean equals(Object o) { return o instanceof Union && elements.equals(((Union)o).elements); }
        public int hashCode() { return elements.hashCode(); }
    }

    private static class Concat extends RegularExpression {
        final List<RegularExpression> elements;
        Concat(List<RegularExpression> e) { this.elements = e; }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (RegularExpression e : elements) {
                String s = e.toString();
            
                boolean needsParens = e instanceof Union; 
                if (needsParens) sb.append("(").append(s).append(")");
                else sb.append(s);
            }
            return sb.toString();
        }
        public int compareTo(RegularExpression o) {
            if (o instanceof Star || o instanceof Union) return -1;
            if (o instanceof Concat) return this.toString().compareTo(o.toString());
            return 1;
        }
        public boolean equals(Object o) { return o instanceof Concat && elements.equals(((Concat)o).elements); }
        public int hashCode() { return elements.hashCode(); }
    }

    private static class Star extends RegularExpression {
        final RegularExpression inner;
        Star(RegularExpression i) { this.inner = i; }
        public String toString() {
            String s = inner.toString();
            if (inner instanceof Union || inner instanceof Concat) return "(" + s + ")*";
            return s + "*";
        }
        public int compareTo(RegularExpression o) { return (o instanceof Star) ? inner.compareTo(((Star)o).inner) : 1; }
        public boolean equals(Object o) { return o instanceof Star && inner.equals(((Star)o).inner); }
        public int hashCode() { return inner.hashCode(); }
    }
}