package cz.webarchiv.SRW;

import cz.webarchiv.SRW.opensearch.OpenSearchClient;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author xrosecky
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.put("opensearch.url", "http://war.webarchiv.cz:8080/nutch/opensearch.xml");
        properties.put("opensearch.xsl_template", "/home/app/nutch.xsl");
        properties.put("opensearch.total_records_xpath", "//totalResults/text()");
        OpenSearchClient client = new OpenSearchClient(properties);
        OpenSearchClient.QueryResult result = client.search("trest", 0, 10);
        for (String res : result.results) {
            System.out.println(res);
        }
    }
    
    public static void tmain(String[] args) throws Exception {
        SRWDatabase db = new SRWDatabase();

        OpenSearchClient client = null; // new OpenSearchClient(new URL("http://war.webarchiv.cz:8080/nutch/opensearch.xml"), new File("C:\\Users\\Vašek\\foo.xsl"));
        System.err.println("connected");
        List<String> results = client.search("test", 0, 10).results;
        for (String result : results) {
            System.err.println(result);
        }
    }
    /*
    public static void main(String[] args) throws Exception {
    URL url = new URL("http://www.war.webarchiv.cz:8080/nutch/opensearch?query=test");
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = builder.parse(url.openStream());
    NodeList nodes = ((Element) ((NodeList) document.getElementsByTagName("channel")).item(0)).getElementsByTagName("item");
    for (int i = 0; i != nodes.getLength(); i++) {
    Element elm = (Element) nodes.item(i);
    String link = ((Node) elm.getElementsByTagName("link").item(0)).getTextContent();
    String date = ((Node) elm.getElementsByTagName("nutch:date").item(0)).getTextContent();
    System.out.println(date+"/"+link);
    }
    }
     */
}
