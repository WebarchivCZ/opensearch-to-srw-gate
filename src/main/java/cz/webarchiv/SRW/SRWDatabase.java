package cz.webarchiv.SRW;

import cz.webarchiv.SRW.translators.SimpleCQLTranslator;
import ORG.oclc.os.SRW.QueryResult;
import ORG.oclc.os.SRW.TermList;
import cz.webarchiv.SRW.opensearch.OpenSearchClient;
import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLTermNode;

/**
 *
 * @author xrosecky
 */
public class SRWDatabase extends ORG.oclc.os.SRW.SRWDatabase {

    static Log log = LogFactory.getLog(SRWDatabase.class);
    String schemaID = "info:srw/schema/1/dc-v1.1";

    @Override
    public String getExtraResponseData(QueryResult result, SearchRetrieveRequestType type) {
        return null;
    }

    @Override
    public QueryResult getQueryResult(String query, SearchRetrieveRequestType type) throws InstantiationException {
        try {
            SimpleCQLTranslator translator = new SimpleCQLTranslator(this.dbProperties);
            CQLNode node = parser.parse(query);
            String nutchQuery = translator.translate(node);
            log.info("Nutch query is " + nutchQuery);
            return new SRWQueryResult(nutchQuery, new OpenSearchClient(this.dbProperties));
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            log.error(ex);
            SRWQueryResult qr = new SRWQueryResult(query, null);
            qr.addDiagnostic(0, ex.toString());
            return qr;
        }

    }

    @Override
    public String getIndexInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("<indexInfo>\n");
        sb.append("  <set identifier='info:srw/cql-context-set/1/cql-v1.1' name='cql'>\n");
        sb.append("    <index>\n");
        sb.append("      <title>Any</title>\n");
        sb.append("      <map><name set='cql'>any</name></map>\n");
        sb.append("    </index>\n");
        sb.append("  </set>\n");
        sb.append("</indexInfo>\n");
        return sb.toString();
    }

    @Override
    public String getSchemaID(String schemaName) {
        return schemaID;
    }

    @Override
    public String getSchemaInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("<schemaInfo>\n");
        sb.append("  <schema identifier='").append(schemaID).append("'\n");
        sb.append("     location='http://www.loc.gov/zing/srw/dc-schema.xsd'\n");
        sb.append("     sort='false' retrieve='true' name='DC'>\n");
        sb.append("     <title>DC: Dublin Core Elements</title>\n");
        sb.append("  </schema>\n");
        sb.append("</schemaInfo>\n");
        return sb.toString();
    }

    @Override
    public void init(String dbname, String srwHome, String dbHome,
            String dbPropertiesFileName, Properties dbProperties) {
        super.initDB(dbname, srwHome, dbHome, dbPropertiesFileName, dbProperties);
    }

    @Override
    public TermList getTermList(CQLTermNode node, int arg1, int arg2, ScanRequestType arg3) {
        throw new UnsupportedOperationException("GetTermList() not supported yet.");
    }

    @Override
    public boolean supportsSort() {
        return false;
    }
}
