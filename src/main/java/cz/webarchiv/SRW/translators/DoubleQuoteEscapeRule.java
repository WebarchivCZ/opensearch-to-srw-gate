package cz.webarchiv.SRW.translators;

/**
 *
 * @author xrosecky
 */
public class DoubleQuoteEscapeRule extends EscapeRule {

    private static final String doubleQuote = "\"";
    private static final String doubleQuoteEscaped = "\\\"";

    @Override
    public String escape(String str) {
        if (str.contains(" ")) {
            return doubleQuote + str.replaceAll(doubleQuote, doubleQuoteEscaped) + doubleQuote;
        } else {
            return str.replaceAll(doubleQuote, doubleQuoteEscaped);
        }
    }

}
