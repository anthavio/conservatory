package com.anthavio.conserv.web;

import java.sql.SQLException;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.h2.server.TcpServer;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * Web application root spring config
 * 
 * @author martin.vanek
 *
 */
@Configuration
//@PropertySource("classpath:npis-export.properties")
@EnableJpaRepositories(basePackageClasses = Application.class)
@EnableTransactionManagement
public class WebSpringConfig {

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private Environment environment;

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

		} else if (db.equals("memory")) {
			EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
			//builder.addScript("dbschema.sql");
			//builder.addScript("dbdata.sql");
			EmbeddedDatabase h2mem = builder.setType(EmbeddedDatabaseType.H2).build();
			return h2mem;
		} else if (db.equals("internal")) {
			//"jdbc:h2:/data/conserv;IFEXISTS=TRUE"
			Server h2Server = h2Server();
			String driverClass = "org.h2.Driver";
			String url = "jdbc:h2:tcp://localhost:" + h2Server.getService().getPort() + "/conserv";
			String username = "sa";
			String password = "sa";
			return buildDataSource(driverClass, url, username, password);
		} else {
			throw new IllegalArgumentException("Unsuported da value " + db);
		}
	}

	private DataSource buildDataSource(String driverClass, String url, String username, String password) {
		BoneCPDataSource dataSource = new BoneCPDataSource();
		dataSource.setJdbcUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setMaxConnectionsPerPartition(10);
		dataSource.setConnectionTestStatement("SELECT 1");
		dataSource.setIdleConnectionTestPeriodInMinutes(30);
		return dataSource;
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public Server h2Server() {
		String tcpPort = "9002";
		String baseDir = "h2db";
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

	/**
	 * From @PropertySource("...") inject @Value("${...}")
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
