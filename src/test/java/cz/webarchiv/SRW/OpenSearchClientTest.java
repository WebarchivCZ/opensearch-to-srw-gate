package cz.webarchiv.SRW;

import cz.webarchiv.SRW.opensearch.OpenSearchClient;
import java.util.Properties;
import junit.framework.TestCase;

/**
 *
 * @author xrosecky
 */
public class OpenSearchClientTest extends TestCase {

    public OpenSearchClientTest(String testName) {
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
        Properties properties = new Properties();
        properties.put("opensearch.url", "http://war.webarchiv.cz:8080/nutch/opensearch.xml");
        properties.put("opensearch.xsl_template", "/home/xrosecky/nutch.xsl");
        properties.put("opensearch.total_records_xpath", "//opensearch:totalResults/text()");
        properties.put("opensearch.namespace.nutch", "http://www.nutch.org/opensearchrss/1.0/");
        properties.put("opensearch.namespace.opensearch", "http://a9.com/-/spec/opensearchrss/1.0/");
        OpenSearchClient client = new OpenSearchClient(properties);
        OpenSearchClient.QueryResult result = client.search("trest", 0, 10);
        for (String res : result.results) {
            System.out.println(res);
        }
        
    }
    
}
