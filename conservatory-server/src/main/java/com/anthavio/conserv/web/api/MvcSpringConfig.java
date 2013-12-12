package com.anthavio.conserv.web.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Mvc servlet spring config
 * 
 * @author martin.vanek
 *
 */
@Configuration
@ComponentScan(value = "com.anthavio.conserv.web.api", excludeFilters = @ComponentScan.Filter(value = Configuration.class, type = FilterType.ANNOTATION))
@EnableWebMvc
public class MvcSpringConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
		registry.addResourceHandler("/assets/**").addResourceLocations("/assets/");
		/*
		registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
				.setCachePeriod(31556926);
		registry.addResourceHandler("/css/**").addResourceLocations("/css/").setCachePeriod(31556926);
		registry.addResourceHandler("/img/**").addResourceLocations("/img/").setCachePeriod(31556926);
		registry.addResourceHandler("/js/**").addResourceLocations("/js/").setCachePeriod(31556926);
		*/
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		//set com.google.protobuf.spring.http.ProtobufHttpMessageConverter
		try {
			Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
			jaxb2Marshaller.setSupportJaxbElementClass(true);
			jaxb2Marshaller.setClassesToBeBound(com.anthavio.conserv.model.Config.class);
			jaxb2Marshaller.afterPropertiesSet();
			converters.add(new MarshallingHttpMessageConverter(jaxb2Marshaller));
		} catch (Exception x) {
			throw new IllegalStateException("This should never happend", x);
		}

		converters.add(new MappingJackson2HttpMessageConverter());

		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
		//stringConverter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_PLAIN));
		converters.add(stringConverter);
		//org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
		//org.springframework.http.converter.xml.MarshallingHttpMessageConverter
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LocaleChangeInterceptor()).addPathPatterns("/*");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		//registry.addViewController("/").setViewName("hello");
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean(name = "multipartResolver")
	//multipartResolver is MAGIC ID!!!!
	public CommonsMultipartResolver MultipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("utf-8");
		resolver.setMaxUploadSize(10 * 1024 * 1024); // 10 MB
		return resolver;
	}

	@Bean
	public ContentNegotiatingViewResolver setUpContentNegotiatingViewResolver() {
		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();

		resolver.setOrder(1);

		List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();
		//viewResolvers.add(getThymeleafViewResolver());
		viewResolvers.add(getInternalResourceViewResolver());
		resolver.setViewResolvers(viewResolvers);
		/*
		Map<String, String> mediaTypes = new HashMap<String, String>();
		mediaTypes.put("json", "application/json");
		mediaTypes.put("xml", "application/xml");
		resolver.setMediaTypes(mediaTypes);
		
		List<View> defaultViews = new ArrayList<View>();
		defaultViews.add(new MappingJacksonJsonView());
		resolver.setDefaultViews(defaultViews);
		*/
		return resolver;
	}

	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		resolver.setOrder(3);
		return resolver;
	}

	/*
	@Bean
	public ThymeleafViewResolver getThymeleafViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(getSpringTemplateEngine());
		resolver.setOrder(2);
		resolver.setViewClass(org.thymeleaf.spring3.view.ThymeleafView.class);
		resolver.setContentType("text/html; charset=UTF-8");
		resolver.setCharacterEncoding("UTF-8");

		return resolver;
	}

	//@Bean
	private SpringTemplateEngine getSpringTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(getThymeleafTemplateResolver());
		Set<IDialect> dialects = new HashSet<IDialect>();
		dialects.add(new nz.net.ultraq.web.thymeleaf.LayoutDialect());
		dialects.add(new com.github.dandelion.datatables.thymeleaf.dialect.DataTablesDialect());
		engine.setAdditionalDialects(dialects);
		return engine;
	}

	//@Bean
	private ServletContextTemplateResolver getThymeleafTemplateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/WEB-INF/thyme/");
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(false);
		return resolver;
	}
	*/
}
