package cz.webarchiv.SRW;

import cz.webarchiv.SRW.translators.CQLTranslator;
import cz.webarchiv.SRW.translators.SimpleCQLTranslator;
import java.util.Properties;
import junit.framework.TestCase;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParser;

/**
 *
 * @author xrosecky
 */
public class SimpleCQLTranslatorTest extends TestCase {
    
    public SimpleCQLTranslatorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParsing() throws Exception {
        CQLParser cqlParser = new CQLParser();
        CQLNode node = cqlParser.parse("(title=\"Rome and Julie\" and author=\"William Shakespeare\") OR Romeo");
        Properties properties = new Properties();
        properties.setProperty("operator.and", "AND");
        properties.setProperty("operator.or", "OR");
        properties.setProperty("key.srw.serverChoice", "");
        properties.setProperty("key.title", "titulek");
        properties.setProperty("key.author", "autor");
        properties.setProperty("key_value_separator", ":");
        CQLTranslator translator = new SimpleCQLTranslator(properties);
        String result = translator.translate(node);
        this.assertEquals("", result, "titulek:\"Rome and Julie\" AND autor:\"William Shakespeare\" OR Romeo");
    }

}
