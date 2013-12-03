package com.anthavio.conserv.web.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.anthavio.conserv.model.Configuration;
import com.anthavio.conserv.model.Property;
import com.anthavio.conserv.services.ConservService;
import com.anthavio.conserv.services.PropertiesConverter;
import com.anthavio.util.PropertiesUtil.PropertyLine;

/**
 * 
 * @author martin.vanek
 *
 */
@Controller
public class ConservRestController {

	@Autowired
	private ConservService service;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * http://www.javacodegeeks.com/2013/11/spring-rest-exception-handling-vol-2.html
	 * http://www.javacodegeeks.com/2013/02/exception-handling-for-rest-with-spring-3-2.html
	 * http://www.javacodegeeks.com/2013/03/exception-handling-with-the-spring-3-2-controlleradvice-annotation.html
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String error(HttpServletRequest req, Exception x) {
		logger.error("Argh!", x);
		return "Argh! " + x;
	}

	@ExceptionHandler(IncorrectResultSizeDataAccessException.class)
	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ResponseBody
	public String notFound(HttpServletRequest req, IncorrectResultSizeDataAccessException irsdax) {
		//irsdax.printStackTrace();
		/*
		Locale locale = LocaleContextHolder.getLocale();
		String errorMessage = messageSource.getMessage("error.no.smartphone.id", null, locale);
		errorMessage += ex.getSmartphoneId();
		String errorURL = req.getRequestURL().toString();
		return new ErrorInfo(errorURL, errorMessage);
		*/
		return irsdax.getMessage();
	}

	@RequestMapping(value = "config/{envCodeName}/{appCodeName}/{resourceCodeName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody
	Configuration getConfigurationJson(@PathVariable String envCodeName, @PathVariable String appCodeName,
			@PathVariable String resourceCodeName) {

		List<PropertyLine> configLines = service.loadFast(envCodeName, appCodeName, resourceCodeName);
		List<Property> properties = PropertiesConverter.instance().convertForClient(configLines);
		/*
		properties.add(new Property("string.property", "Some value"));
		properties.add(new Property("integer.property", 132456789));
		properties.add(new Property("date.property", new Date()));
		properties.add(new Property("url.property", URI.create("http://test-www.nature.com:8080/zxzxzx")));
		*/
		return new Configuration(appCodeName, envCodeName, properties);
	}

	@RequestMapping(value = "config/{envCodeName}/{appCodeName}/{resourceCodeName}", method = RequestMethod.GET, headers = "Accept=application/xml")
	public @ResponseBody
	JAXBElement<Configuration> getConfigurationXml(@PathVariable String envCodeName, @PathVariable String appCodeName,
			@PathVariable String resourceCodeName) {
		Configuration configuration = getConfigurationJson(envCodeName, appCodeName, resourceCodeName);
		return new JAXBElement<Configuration>(new QName("configuration"), Configuration.class, configuration);
	}

	@RequestMapping(value = "config/{envCodeName}/{appCodeName}/{resourceCodeName}", method = RequestMethod.GET)
	public @ResponseBody
	JAXBElement<Configuration> getConfiguration(@PathVariable String envCodeName, @PathVariable String appCodeName,
			@PathVariable String resourceCodeName) {
		Configuration configuration = getConfigurationJson(envCodeName, appCodeName, resourceCodeName);
		return new JAXBElement<Configuration>(new QName("configuration"), Configuration.class, configuration);
	}
}
