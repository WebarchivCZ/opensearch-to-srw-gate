package cz.webarchiv.SRW;

import cz.webarchiv.SRW.opensearch.OpenSearchClient;
import ORG.oclc.os.SRW.QueryResult;
import ORG.oclc.os.SRW.RecordIterator;
import gov.loc.www.zing.srw.ExtraDataType;

public class SRWQueryResult extends QueryResult {

    protected final String query;
    protected final OpenSearchClient client;

    public SRWQueryResult(String query, OpenSearchClient client) {
        this.query = query;
        this.client = client;
    }

    @Override
    public long getNumberOfRecords() {
        try {
            OpenSearchClient.QueryResult result = client.search(query, 0, 5);
            return result.totalResults;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @Override
    public QueryResult getSortedResult(String sortKeys) {
        return this;
    }

    @Override
    public RecordIterator newRecordIterator(long whichRec, int numRecs,
            String schemaId, ExtraDataType edt) throws InstantiationException {
        OpenSearchClient.QueryResult result = client.search(query, whichRec-1, numRecs);
        return new SRWRecordIterator(result.results, schemaId);
    }

}
