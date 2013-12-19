package net.anthavio.conserv.services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anthavio.conserv.dbmodel.ConfigDocument;
import net.anthavio.conserv.model.Property;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author martin.vanek
 *
 */
@Service
public class IndexSearchService {

	public static final String INDEX = "conserve";

	public static final String TYPE = ConfigDocument.class.getSimpleName();

	@Autowired
	private Client esClient;

	public static XContentBuilder getConfigDocumentMapping() throws IOException {
		//http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/mapping-core-types.html
		//http://elasticsearch-users.115913.n3.nabble.com/Creating-mappings-via-the-Java-API-td3638628.html
		XContentBuilder xbMapping = XContentFactory.jsonBuilder()//
				.startObject().startObject(TYPE).startObject("properties")//
				.startObject("ID").field("type", "long").endObject()//
				.startObject("ID_CONFIG_DEPLOY").field("type", "long").endObject()//
				.startObject("CREATED_AT").field("type", "date").endObject()//
				.startObject("CLOB_VALUE").field("type", "string").endObject()//
				.endObject().endObject().endObject();//
		return xbMapping;
	}

	public void indexDocument(ConfigDocument document) {
		if (document.getId() == null) {
			throw new IllegalArgumentException("Id not set: " + document);
		}
		HashMap<String, Object> json = new HashMap<String, Object>();
		//Use DB name conventions
		json.put("ID", document.getId());
		json.put("ID_CONFIG_DEPLOY", document.getIdConfigDeploy());
		json.put("CREATED_AT", document.getCreatedAt());
		json.put("CLOB_VALUE", document.getValue());

		IndexRequest request = new IndexRequest(INDEX, TYPE, document.getId() + "").source(json);
		IndexResponse response = esClient.index(request).actionGet();
		//System.out.println(response.getId());
		//IndexResponse response = esClient
		//		.prepareIndex("conserve", ConfigDocument.class.getSimpleName(), document.getId() + "").execute().actionGet();
	}

	public List<ConfigDocument> searchDocument(String searchExpression) {
		/*
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(ConfigDocument.class)
				.get();
		org.apache.lucene.search.Query query = qb.keyword().onFields("value").matching(searchExpression).createQuery();
		javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(query, ConfigDocument.class);
		return jpaQuery.getResultList();
		*/

		//http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/search.html
		QueryBuilder qb = QueryBuilders.matchQuery("CLOB_VALUE", searchExpression);
		SearchResponse response = esClient.prepareSearch().setIndices(INDEX).setTypes(TYPE).setQuery(qb).execute()
				.actionGet();

		System.out.println(response);
		SearchHits searchHits = response.getHits();
		SearchHit[] hits = searchHits.getHits();
		List<ConfigDocument> retval = new ArrayList<ConfigDocument>(hits.length);
		for (SearchHit hit : hits) {
			//SearchHit field type mapping is not probably out of box  - http://stackoverflow.com/questions/15070407/elasticsearch-equivalent-of-solr-getbeans
			//convert fields manually then....
			long id = Long.parseLong(hit.getId());
			Map<String, Object> fields = hit.getSource();
			Long idConfigDeploy = ((Integer) fields.get("ID_CONFIG_DEPLOY")).longValue();
			Date createdAt;
			try {
				createdAt = new SimpleDateFormat(Property.DATE_TIME_FORMAT).parse((String) fields.get("CREATED_AT"));
			} catch (ParseException px) {
				throw new IllegalStateException("Cannot parse " + fields.get("CREATED_AT"), px);
			}
			String clobValue = (String) fields.get("CLOB_VALUE");
			retval.add(new ConfigDocument(id, idConfigDeploy, createdAt, clobValue));
		}
		return retval;
	}
}
