package cz.webarchiv.SRW;

import ORG.oclc.os.SRW.Record;
import ORG.oclc.os.SRW.RecordIterator;
import java.util.Iterator;
import java.util.List;

public class SRWRecordIterator implements RecordIterator {

    private Iterator<String> results = null;
    private String schemaId = null;

    public SRWRecordIterator(List<String> results, String schemaId) {
        this.results = results.iterator();
        this.schemaId = schemaId;
    }

    @Override
    public Record nextRecord() {
        return new Record(results.next(), schemaId);
    }

    @Override
    public boolean hasNext() {
        return results.hasNext();
    }

    @Override
    public Object next() {
        return nextRecord();
    }

    @Override
    public void remove() {
        results.remove();
    }

    @Override
    public void close() {
    }
}
