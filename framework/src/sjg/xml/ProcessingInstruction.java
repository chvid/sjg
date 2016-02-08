package sjg.xml;

/**
 * Represent an XML processing instruction.
 *
 * @author Christian Hvid
 */

public class ProcessingInstruction {
    private String text;

    /**
     * Returns the text contents of this processing instruction.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the textual representation of this pi.
     */
    public String toString() {
        return "<?" + text + "?>";
    }

    /**
     * Constructs a new data processing instruction with the given contents.
     */
    public ProcessingInstruction(String text) {
        this.text = text;
    }
}
