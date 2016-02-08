package sjg.xml;

/**
 * An xml attribute.
 *
 * @author Christian Hvid
 */

public class Attribute {
    private String name = "";
    private String value = "";

    /**
     * Returns the value of this attribute.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the value of this attribute as an integer - may throw an error if value is not an integer.
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new Error("Attribute '" + name + "' has value '" + value + "' which is not an integer");
        }
    }

    /**
     * Returns the name of this attribute.
     */
    public String getName() {
        return name;
    }

    Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
