package cz.webarchiv.SRW.translators;

import org.z3950.zing.cql.CQLNode;

/**
 *
 * @author xrosecky
 */
public interface CQLTranslator {

    String translate(CQLNode node);

}
