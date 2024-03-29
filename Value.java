public class Value {

    public static Value VOID = new Value(new Object());
    public static Value BREAK = new Value("break");
    public static Value CONT = new Value("continue");

    final Object value;
    public boolean brk;
    public boolean cont;
    
    public Value(Object value) {
        this.value = value;
        this.brk=false;
        this.cont=false;
    }

    public Value(Object value, boolean brk){
        this.value = value;
        this.brk=brk;
    }

    public Value(Object value, boolean brk, boolean cont){
        this.value = value;
        this.brk=brk;
        this.cont=cont;
    }

    public Boolean asBoolean() {
        return (Boolean)value;
    }

    public Double asDouble() {
        return (Double)value;
    }

    public String asString() {
        return String.valueOf(value);
    }

    public boolean isDouble() {
        return value instanceof Double;
    }

    public boolean isString() {
        return value instanceof String;
    }
    
    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    @Override
    public int hashCode() {

        if(value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if(value == o) {
            return true;
        }

        if(value == null || o == null || o.getClass() != value.getClass()) {
            return false;
        }

        Value that = (Value)o;

        return this.value.equals(that.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}