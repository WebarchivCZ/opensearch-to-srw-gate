package cz.webarchiv.SRW;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.lang.StringEscapeUtils;
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
public class OpenSearchClientOld {

    private static final List<String> parameters = Arrays.asList(
            "{searchTerms}", "{startPage?}", "{count?}", "{startIndex?}");

    private static class QueryResult {

        List<String> results = new ArrayList<String>();
        int page = 0;
        int index = 0;
        int fetchAtOnce = 10;
        int numOfFetchedResults = 0;
        int numOfResults = 0;
    }
    private String openSearchURL;
    private final List<String> supportedParameters = new ArrayList<String>();
    private final Map<String, QueryResult> results = new HashMap<String, QueryResult>();

    public OpenSearchClientOld(URL openSearchDescription) {
        this.openSearchURL = loadDescription(openSearchDescription);
        System.err.println("the search URL is" + openSearchURL);
        loadSupportedParams();
    }

    private String loadDescription(URL openSearchDescription) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (openSearchDescription == null) {
                openSearchDescription = new URL("http://war.webarchiv.cz/nutch/opensearch.xml");
            }
            Document document = builder.parse(openSearchDescription.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            String query = "/OpenSearchDescription/Url[@type='application/rss+xml']/@template";
            return (String) xpath.evaluate(query, document, XPathConstants.STRING);
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

    public List<String> search(String query, long offset, long numOfResults) {
        long desiredSize = offset + numOfResults;
        QueryResult result = results.get(query);
        if (result == null) {
            result = new QueryResult();
            results.put(query, result);
        }
        System.err.println("in search");
        while (result.results.size() < desiredSize) {
            System.err.println("cycling");
            if (result.numOfResults > desiredSize) {
                break;
            }
            if (supportedParameters.contains("{startPage?}")) {
                fetchByPageNumber(query, result);
            } else if (supportedParameters.contains("{startIndex?}")) {
                fetchByIndexNumber(query, result);
            } else {
                break;
            }
        }
        List<String> subset = new ArrayList<String>();
        for (long i = offset; i < min(desiredSize, result.results.size()); i++) {
            subset.add(result.results.get((int) i));
        }
        return subset;
    }

    private long min(long a, int b) {
        return (a < b) ? a : b;
    }

    private void fetchByPageNumber(String query, QueryResult result) {
        String resultURL = openSearchURL;
        try {
            resultURL = resultURL.replace("{searchTerms}", URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
        }
        resultURL = resultURL.replace("{startPage?}", Integer.toString(result.page));
        result.page++;
        resultURL = resultURL.replace("{count?}", Integer.toString(result.fetchAtOnce));
        resultURL = resultURL.replace("{startIndex?}", "");
        executeQuery(resultURL, result);
    }

    private void fetchByIndexNumber(String query, QueryResult result) {
        String resultURL = openSearchURL;
        try {
            resultURL = resultURL.replace("{searchTerms}", URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
        }
        resultURL = resultURL.replace("{startIndex?}", Integer.toString(result.index));
        resultURL = resultURL.replace("{count?}", Integer.toString(result.fetchAtOnce));
        resultURL = resultURL.replace("{startPage?}", "");
        int count = executeQuery(resultURL, result);
        result.index += count;
    }

    private int executeQuery(String query, QueryResult result) {
        System.out.println("The query is" + query);
        try {
            List<String> queryResults = new ArrayList<String>();
            // String escapedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(query);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(url.openStream());
            NodeList nodes = ((Element) ((NodeList) document.getElementsByTagName("channel")).item(0)).getElementsByTagName("item");
            for (int i = 0; i != nodes.getLength(); i++) {
                Element elm = (Element) nodes.item(i);
                String liveLink = ((Node) elm.getElementsByTagName("link").item(0)).getTextContent();

                String title = ((Node) elm.getElementsByTagName("title").item(0)).getTextContent();
                String description = ((Node) elm.getElementsByTagName("description").item(0)).getTextContent();
                String site = ((Node) elm.getElementsByTagName("nutch:site").item(0)).getTextContent();
                String date = ((Node) elm.getElementsByTagName("nutch:date").item(0)).getTextContent();
                String waybackLink = "http://hostiwar.webarchiv.cz:8080/wayback/" + date + "/" + liveLink;
                String allVersionsLink = "http://hostiwar.webarchiv.cz:8080/wayback/*" + "/" + liveLink;
                StringBuilder sb = new StringBuilder();
                sb.append("<srw_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:srw_dc=\"info:srw/schema/1/dc-v1.1\">");
                sb.append("    <dc:title>" + StringEscapeUtils.escapeXml(title) + "</dc:title>");
                sb.append("    <dc:publisher>" + StringEscapeUtils.escapeXml(site) + "</dc:publisher>");
                sb.append("    <dc:date>" + StringEscapeUtils.escapeXml(date) + "</dc:date>");
                sb.append("    <dc:description>" + StringEscapeUtils.escapeXml(description) + "</dc:description>");
                sb.append("    <dc:identifier>" + StringEscapeUtils.escapeXml(allVersionsLink) + "</dc:identifier>");
                sb.append("    <dc:identifier>" + StringEscapeUtils.escapeXml(waybackLink) + "</dc:identifier>");
                sb.append("    <dc:identifier>" + StringEscapeUtils.escapeXml(liveLink) + "</dc:identifier>");
                sb.append("</srw_dc:dc>");
                queryResults.add(sb.toString());
            }
            String size = document.getElementsByTagName("opensearch:totalResults").item(0).getTextContent();
            result.numOfResults = Integer.parseInt(size);
            result.results.addAll(queryResults);
            return queryResults.size();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String applyXSLT(Element element) throws Exception {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(element),
                new StreamResult(buffer));
        String str = buffer.toString();
        Source input = new StreamSource(new StringReader(str));
        Source xsltSource = new StreamSource(new File("foo"));
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        Transformer trans = transFactory.newTransformer(xsltSource);
        trans.transform(input, result);
        return sw.toString();
    }
    /*
    public List<String> search(String query, long offset, long numOfResults) {
    try {
    List<String> results = new ArrayList<String>();
    String escapedQuery = URLEncoder.encode(query, "UTF-8");
    String urlAsString = String.format(openSearchURL, escapedQuery, offset, numOfResults);
    URL url = new URL(urlAsString);
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = builder.parse(url.openStream());
    NodeList nodes = ((Element) ((NodeList) document.getElementsByTagName("channel")).item(0)).getElementsByTagName("item");
    for (int i = 0; i != nodes.getLength(); i++) {
    Element elm = (Element) nodes.item(i);
    String liveLink = ((Node) elm.getElementsByTagName("link").item(0)).getTextContent();
    
    String title = ((Node) elm.getElementsByTagName("title").item(0)).getTextContent();
    String description = ((Node) elm.getElementsByTagName("description").item(0)).getTextContent();
    String site = ((Node) elm.getElementsByTagName("nutch:site").item(0)).getTextContent();
    String date = ((Node) elm.getElementsByTagName("nutch:date").item(0)).getTextContent();
    String waybackLink = "http://hostiwar.webarchiv.cz:8080/wayback/" + date + "/" + liveLink;
    String allVersionsLink = "http://hostiwar.webarchiv.cz:8080/wayback/*" + "/" + liveLink;
    StringBuilder sb = new StringBuilder();
    sb.append("<srw_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:srw_dc=\"info:srw/schema/1/dc-v1.1\">");
    sb.append("    <dc:title>" + StringEscapeUtils.escapeXml(title) + "</dc:title>");
    sb.append("    <dc:publisher>" + StringEscapeUtils.escapeXml(site) + "</dc:publisher>");
    sb.append("    <dc:date>" + StringEscapeUtils.escapeXml(date) + "</dc:date>");
    sb.append("    <dc:description>" + StringEscapeUtils.escapeXml(description) + "</dc:description>");
    sb.append("    <dc:identifier>" + StringEscapeUtils.escapeXml(allVersionsLink) + "</dc:identifier>");
    sb.append("    <dc:identifier>" + StringEscapeUtils.escapeXml(waybackLink) + "</dc:identifier>");
    sb.append("    <dc:identifier>" + StringEscapeUtils.escapeXml(liveLink) + "</dc:identifier>");
    sb.append("</srw_dc:dc>");
    results.add(sb.toString());
    }
    return results;
    } catch (Exception ex) {
    throw new RuntimeException(ex);
    }
    }
    
    public int getSearchResultSize(String query) {
    try {
    List<String> results = new ArrayList<String>();
    String escapedQuery = URLEncoder.encode(query, "UTF-8");
    String urlAsString = String.format(openSearchURL, escapedQuery, 0, 1);
    URL url = new URL(urlAsString);
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = builder.parse(url.openStream());
    String size = document.getElementsByTagName("opensearch:totalResults").item(0).getTextContent();
    return Integer.parseInt(size);
    } catch (Exception ex) {
    throw new RuntimeException(ex);
    }
    }
     */
}
