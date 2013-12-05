package com.anthavio.conserv.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.h2.server.TcpServer;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.anthavio.conserv.dbmodel.Application;
import com.anthavio.conserv.dbmodel.ConfigDocument;
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * 
 * @author martin.vanek
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = Application.class/*, repositoryFactoryBeanClass = AbstractDaoFactoryBean.class*/)
@ComponentScan(basePackageClasses = ConservService.class, excludeFilters = @ComponentScan.Filter(value = Configuration.class, type = FilterType.ANNOTATION))
public class ServicesSpringConfig {

	private final String workDir = "./work";

	@Autowired
	private Environment environment;

	@PostConstruct
	public void init() throws Exception {
		//http://stackoverflow.com/questions/6068831/solr-vs-hibernate-search-which-to-choose-and-when
		//http://docs.jboss.org/hibernate/search/4.4/reference/en-US/html_single
		/*
		EntityManager jpaEntityManager = entityManagerFactory().createEntityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(jpaEntityManager);
		//MassIndexer indexer = fullTextEntityManager.createIndexer();
		MassIndexerProgressMonitor monitor = new SimpleIndexingProgressMonitor();
		MassIndexer indexer = fullTextEntityManager.createIndexer(ConfigDocument.class).batchSizeToLoadObjects(25)
				.cacheMode(CacheMode.NORMAL).threadsToLoadObjects(5).idFetchSize(150).threadsForSubsequentFetching(20)
				.progressMonitor(monitor);
		indexer.startAndWait();
		jpaEntityManager.close();
		*/

	}

	@Bean(destroyMethod = "close")
	public Client esClient() throws IOException {
		//Investigate https://github.com/dadoonet/spring-elasticsearch
		//http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html
		String es = environment.getProperty("es", "internal");
		if (es.equals("internal")) {
			Node node = esInternalNode();
			Client client = node.client();
			client.admin().cluster().prepareHealth().setWaitForYellowStatus().setTimeout(TimeValue.timeValueMinutes(1))
					.execute().actionGet();
			return client;
		} else if (es.equals("transport")) {
			//http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html#transportclient
			Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "myClusterName").build(); //TODO configuration elasticsearch.yml
			TransportClient client = new TransportClient(settings);
			client.addTransportAddress(new InetSocketTransportAddress("host1", 9300));
			client.addTransportAddress(new InetSocketTransportAddress("host2", 9300));
			return client;
		} else {
			throw new IllegalArgumentException("Unussported es type: '" + es + "'");
		}
	}

	@Bean(destroyMethod = "close")
	public Node esInternalNode() throws IOException {
		// http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/setup-configuration.html
		ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
				.put("node.name", "node-local-" + System.currentTimeMillis())//
				.put("cluster.name", "cluster-local-" + NetworkUtils.getLocalAddress().getHostName())//
				.put("cluster.routing.schedule", "50ms")//
				.put("gateway.type", "local")//
				.put("node.local", true)//
				.put("node.data", true)//
				.put("path.data", workDir + "/elasticsearch/data")//
				.put("path.work", workDir + "/elasticsearch/work")//
				.put("path.logs", workDir + "/elasticsearch/logs")//
				//.put("index.store.type", "memory")//
				//.put("index.store.fs.memory.enabled", "true")//
				.put("index.number_of_shards", "1")//
				.put("index.number_of_replicas", "0");//

		//if (settings != null) {
		//    builder.put(settings);
		//}

		Settings settings = builder.build();
		Node node = NodeBuilder.nodeBuilder().settings(settings).node();
		Client client = node.client();
		IndicesAdminClient indexAdmin = node.client().admin().indices();

		XContentBuilder xbMapping = IndexSearchService.getConfigDocumentMapping();
		String index = IndexSearchService.INDEX;
		String type = ConfigDocument.class.getSimpleName();

		IndicesExistsResponse existsResponse = indexAdmin.prepareExists(index).execute().actionGet();
		//http://stackoverflow.com/questions/12367877/change-settings-and-mappings-on-existing-index-in-elasticsearch
		if (!existsResponse.isExists()) {
			//create index with ConfigDocument Mapping...
			indexAdmin.prepareCreate(index).addMapping(type, xbMapping).execute().actionGet();
		}

		//configuration and mapping can be downloaded and checked
		ClusterStateResponse response = client.admin().cluster().prepareState().setFilterAll().setFilterMetaData(false)
				.setFilterIndices(index).execute().actionGet();
		IndexMetaData indexMetaData = response.getState().metaData().index(index);
		Settings isettings = indexMetaData.settings();
		MappingMetaData mapping = indexMetaData.mappings().get(type);
		System.out.println(mapping.sourceAsMap()); //it is here!
		/*
		PutMappingResponse actionGet = admin.indices().preparePutMapping(IndexSearchService.ES_INDEX_NAME)
				.setType(ConfigDocument.class.getSimpleName()).setSource(xbMapping).execute().actionGet();
		System.out.println(actionGet.isAcknowledged());
		*/
		return node;
	}

	/*
		@PreDestroy
		public void destroy() {
			h2Server().stop();
		}
	*/
	@Bean
	public DataSource dataSource() {

		String db = environment.getProperty("db", "internal");

		if (db.equals("datasource") || db.equals("ds")) {
			String driver = environment.getProperty("jdbcDriver");
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException cnfx) {
				throw new IllegalStateException("jdbc driver " + driver + " not found in classpath");
			}
			String jdbcUrl = environment.getProperty("jdbcUrl");
			String jdbcUsername = environment.getProperty("jdbcUsername");
			String jdbcPassword = environment.getProperty("jdbcPassword");
			return buildDataSource(driver, jdbcUrl, jdbcUsername, jdbcPassword);

		} else if (db.equals("internal")) {
			//"jdbc:h2:/data/conserv;IFEXISTS=TRUE"
			Server h2Server = h2Server();
			String driverClass = "org.h2.Driver";
			String url = "jdbc:h2:tcp://localhost:" + h2Server.getService().getPort() + "/conserv";
			String username = "sa";
			String password = "sa";
			return buildDataSource(driverClass, url, username, password);
		} else if (db.equals("memory")) {
			EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
			//builder.addScript("dbschema.sql");
			//builder.addScript("dbdata.sql");
			EmbeddedDatabase h2mem = builder.setType(EmbeddedDatabaseType.H2).build();
			return h2mem;
		} else {
			throw new IllegalArgumentException("Unsuported db value " + db);
		}
	}

	private DataSource buildDataSource(String driverClass, String url, String username, String password) {
		BoneCPDataSource dataSource = new BoneCPDataSource();
		dataSource.setJdbcUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setMinConnectionsPerPartition(3);
		dataSource.setMaxConnectionsPerPartition(10);
		dataSource.setConnectionTestStatement("SELECT 1");
		dataSource.setIdleConnectionTestPeriodInMinutes(30);
		return dataSource;
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2Server() {
		String tcpPort = "9002";
		String baseDir = "./work/h2db";
		String args = "-tcp,-tcpAllowOthers,-tcpDaemon,-tcpPort," + tcpPort + ",-baseDir," + baseDir;
		TcpServer service = new TcpServer();
		Server server;
		try {
			server = new Server(service, args.split(","));
		} catch (SQLException x) {
			throw new IllegalArgumentException("Failed to start H2 server", x);
		}
		return server;
	}

	@Bean(name = "entityManagerFactory")
	public EntityManagerFactory entityManagerFactory() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(Application.class.getPackage().getName());
		factory.setDataSource(dataSource());
		Properties jpaProperties = new Properties();
		/*
		jpaProperties.setProperty("hibernate.generate_statistics", "true");

		jpaProperties.setProperty("hibernate.cache.region.factory_class",
				"org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
		jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
		jpaProperties.setProperty("hibernate.cache.use_query_cache", "true");
		jpaProperties.setProperty("hibernate.cache.use_structured_entries", "true");
		jpaProperties.setProperty("net.sf.ehcache.configurationResourceName", "ehcache.xml");
		*/

		/*
		//http://musingsofaprogrammingaddict.blogspot.co.uk/2010/01/jpa-2-and-bean-validation-in-action.html
		jpaProperties.setProperty("javax.persistence.validation.group.pre-persist", "com.anthavio.conserv.dbmodel.PrePresistChecks");
		jpaProperties.setProperty("javax.persistence.validation.group.pre-update", "com.anthavio.conserv.dbmodel.PreUpdateChecks");
		jpaProperties.setProperty("javax.persistence.validation.group.pre-remove", "com.anthavio.conserv.dbmodel.PreDeleteChecks");
		 */

		//http://mprabhat.wordpress.com/2012/09/30/full-text-search-with-hibernate-search-4-1-lucene-and-jpa/
		jpaProperties.setProperty("hibernate.search.default.directory_provider", "filesystem"); //XXX not clusterable feature
		jpaProperties.setProperty("hibernate.search.default.indexBase", "./work/lucene/indexes");

		factory.setJpaProperties(jpaProperties);
		factory.afterPropertiesSet();

		return factory.getObject();
	}

	@Bean
	public HibernateExceptionTranslator hibernateExceptionTranslator() {
		return new HibernateExceptionTranslator();
	}

	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager() {

		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory());
		return txManager;
	}

}
