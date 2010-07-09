package cz.webarchiv.SRW.opensearch;

import cz.webarchiv.SRW.Util;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Vaclav Rosecky xrosecky@gmail.com
 * project WebArchiv - Archive of the Czech Web
 * www.webarchiv.cz
 * 
 */
public class OpenSearchClient {

    private static final String SEARCH_TERMS = "{searchTerms}";
    private static final String COUNT = "{count?}";
    private static final String START_INDEX = "{startIndex?}";

    private static final List<String> parameters = Arrays.asList(
            SEARCH_TERMS, COUNT, START_INDEX);

    private final String openSearchURL;
    private final File xsl;
    private final String getRecordsXPathExpression;
    private final String getTotalRecordsXpathExpression;
    private final NamespaceContext namespaceContext;
    private final List<String> supportedParameters = new ArrayList<String>();

    public class QueryResult {
        public List<String> results;
        public int totalResults;
        public QueryResult() {
            results = new ArrayList<String>();
            totalResults = 0;
        }
    }

    private class MyNamespaceContext implements NamespaceContext {

        final Map<String, String> namespaces;

        public MyNamespaceContext(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return namespaces.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }

    }

    public OpenSearchClient(Properties props) throws Exception {
        this.openSearchURL = loadDescription(new URL(Util.getRequired(props, "opensearch.url")));
        this.xsl = new File(Util.getRequired(props, "opensearch.xsl_template"));
        this.getRecordsXPathExpression = props.getProperty("opensearch.records_xpath", "//channel/item");
        this.getTotalRecordsXpathExpression = Util.getRequired(props, "opensearch.total_records_xpath");
        Map<String, String> namespaces = Util.parseMap(props, "opensearch.namespace.");
        // namespaces.put("nutch", "http://www.nutch.org/opensearchrss/1.0/");
        // namespaces.put("opensearch", "http://a9.com/-/spec/opensearchrss/1.0/");
        this.namespaceContext = new MyNamespaceContext(namespaces);
        this.loadSupportedParams();
    }

    private String loadDescription(URL openSearchDescription) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(openSearchDescription.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            String query = "/OpenSearchDescription/Url[@type='application/rss+xml']/@template";
            String queryURL = (String) xpath.evaluate(query, document, XPathConstants.STRING);
            System.err.format("Open search query url is:%s", queryURL);
            return queryURL;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadSupportedParams() {
        for (String param : parameters) {
            if (openSearchURL.contains(param)) {
                supportedParameters.add(param);
            }
        }
    }

    public QueryResult search(String query, long offset, long numOfResults) {
        if (supportedParameters.contains(START_INDEX) && supportedParameters.contains(COUNT)) {
            String queryURL = this.openSearchURL;
            queryURL = queryURL.replace(SEARCH_TERMS, query);
            queryURL = queryURL.replace(START_INDEX, String.valueOf(offset));
            queryURL = queryURL.replace(COUNT, String.valueOf(numOfResults));
            return executeQuery(queryURL);
        } else {
            System.err.println("Paging is not supported.");
        }
        return new QueryResult();
    }

    private QueryResult executeQuery(String query) {
        System.out.println("The query is" + query);
        try {
            QueryResult queryResult = new QueryResult();
            URL url = new URL(query);
            DocumentBuilderFactory fact  = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(true);
            DocumentBuilder builder  = fact.newDocumentBuilder();
            Document document = builder.parse(url.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(this.namespaceContext);
            NodeList nodes = (NodeList) xpath.evaluate(getRecordsXPathExpression, document, XPathConstants.NODESET);
            for (int i = 0; i != nodes.getLength(); i++) {
                Element elm = (Element) nodes.item(i);
                queryResult.results.add(applyXSLT(elm));
            }
            String totalResultsStr = (String) xpath.evaluate(getTotalRecordsXpathExpression, document, XPathConstants.STRING);
            queryResult.totalResults = Integer.valueOf(totalResultsStr);
            System.out.println(queryResult.totalResults);
            return queryResult;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String applyXSLT(Node node) throws Exception {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Source input = new StreamSource(new StringReader(dumpNodeAsString(node)));
        Source xslSource = new StreamSource(xsl);
        return transform(input, xslSource);

    }

    private String dumpNodeAsString(Node node) throws Exception {
        return transform(new DOMSource(node), null);
    }

    private String transform(Source input, Source xsltSource) throws Exception {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        Transformer trans = null;
        if (xsltSource == null) {
            trans = transFactory.newTransformer();
        } else {
            trans = transFactory.newTransformer(xsltSource);
        }
        System.err.println(trans.getClass().getName());
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.transform(input, result);
        return sw.toString();
    }

}
