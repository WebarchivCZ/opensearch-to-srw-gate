package cz.webarchiv.SRW.translators;

import cz.webarchiv.SRW.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLNotNode;
import org.z3950.zing.cql.CQLOrNode;
import org.z3950.zing.cql.CQLTermNode;

/**
 *
 * @author xrosecky
 */
public class SimpleCQLTranslator implements CQLTranslator {

    protected final Map<String, String> translateKeys;
    protected final Map<Class, String> translateBinOp = new HashMap<Class, String>();
    protected final String keyValueSeparator;
    protected final EscapeRule escapeRule;

    protected final static String keyStartsWith = "key.";

    public SimpleCQLTranslator(Properties props) {
        translateBinOp.put(CQLAndNode.class, props.getProperty("operator.and"));
        translateBinOp.put(CQLOrNode.class, props.getProperty("operator.or"));
        translateBinOp.put(CQLNotNode.class, props.getProperty("operator.not"));
        translateKeys = Util.parseMap(props, keyStartsWith);
        escapeRule = new DoubleQuoteEscapeRule();
        keyValueSeparator = props.getProperty("key_value_separator");
    }

    @Override
    public String translate(CQLNode node) {
        StringBuilder sb = new StringBuilder();
        translate(node, sb);
        return sb.toString();
    }

    private void translate(CQLNode node, StringBuilder sb) {
        String operator = translateBinOp.get(node.getClass());
        if (operator != null) {
            CQLBooleanNode cbn = (CQLBooleanNode) node;
            translate(cbn.left, sb);
            sb.append(" " + operator + " ");
            translate(cbn.right, sb);
        } else if (node instanceof CQLTermNode) {
            CQLTermNode ctn = (CQLTermNode) node;
            if (translateKeys.containsKey(ctn.getQualifier())) {
                String index = translateKeys.get(ctn.getQualifier());
                String value = escapeRule.escape(ctn.getTerm());
                if (index == null || index.equals("")) {
                    sb.append(value);
                } else {
                    sb.append(index + keyValueSeparator + value);
                }
            } else {
                throw new IllegalArgumentException("Searching for " + ctn.getQualifier() + " is not supported.");
            }
        } else {
            throw new IllegalArgumentException(node.getClass().getCanonicalName() + " is not supported.");
        }
    }
    
}
