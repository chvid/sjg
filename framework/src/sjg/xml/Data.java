package sjg.xml;

/**
 * Represent a data section of an XML-document.
 *
 * @author Christian Hvid
 */

public class Data {
    private String text;

    /**
     * Returns the data of this data section.
     */
    public String toString() {
        return text;
    }

    /**
     * Constructs a new data section containing the given data.
     */
    public Data(String text) {
        this.text = text;
    }
}
