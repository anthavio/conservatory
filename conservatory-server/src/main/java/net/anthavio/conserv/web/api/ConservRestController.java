package net.anthavio.conserv.web.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.anthavio.conserv.dbmodel.ConfigDocument;
import net.anthavio.conserv.dbmodel.ConfigResource.ConfigResourceType;
import net.anthavio.conserv.model.Config;
import net.anthavio.conserv.model.Property;
import net.anthavio.conserv.services.ConservService;
import net.anthavio.conserv.services.PropertiesConverter;

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

	/**
	 * Config file as is stored - text/plain
	 */
	@RequestMapping(value = "config/{envCodeName}/{appCodeName}/{resourceCodeName}", method = RequestMethod.GET)
	public @ResponseBody
	String getConfigurationPlain(@PathVariable String envCodeName, @PathVariable String appCodeName,
			@PathVariable String resourceCodeName, HttpServletResponse response) throws IOException {

		//return new ResponseEntity<String>(body, responseHeaders, status);

		ConfigDocument document = service.loadAtOnce(envCodeName, appCodeName, resourceCodeName);

		ConfigResourceType type = document.getConfigDeploy().getConfigResource().getType();
		//response.setContentType(type.getMimeType() + ";charset=utf-8");
		//response.setCharacterEncoding("utf-8");

		if (type == ConfigResourceType.PROPERTIES) {
			//metadata properties
			StringBuilder sb = new StringBuilder(document.getValue());
			sb.append('\n');
			sb.append("conserv.environment = ").append(envCodeName).append('\n');
			sb.append("conserv.application = ").append(appCodeName).append('\n');
			sb.append("conserv.resource = ").append(resourceCodeName).append('\n');
			String createdAt = new SimpleDateFormat(Property.DATE_TIME_FORMAT).format(document.getCreatedAt());
			sb.append("conserv.createdAt = ").append(createdAt).append('\n');
		}
		response.setDateHeader("Last-Modified", document.getCreatedAt().getTime());
		return document.getValue();
	}

	@RequestMapping(value = "config/{envCodeName}/{appCodeName}/{resourceCodeName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody
	Config getConfigurationJson(@PathVariable String envCodeName, @PathVariable String appCodeName,
			@PathVariable String resourceCodeName) {

		ConfigDocument document = service.loadAtOnce(envCodeName, appCodeName, resourceCodeName);
		ConfigResourceType type = document.getConfigDeploy().getConfigResource().getType();
		if (type != ConfigResourceType.PROPERTIES) {
			throw new IllegalStateException("Illegal ConfigResourceType " + type);
		}
		List<Property> properties = PropertiesConverter.instance().convertForClient(document.getValue());

		return new Config(envCodeName, appCodeName, resourceCodeName, document.getCreatedAt(), properties);
	}

	@RequestMapping(value = "config/{envCodeName}/{appCodeName}/{resourceCodeName}", method = RequestMethod.GET, headers = "Accept=application/xml")
	public @ResponseBody
	JAXBElement<Config> getConfigurationXml(@PathVariable String envCodeName, @PathVariable String appCodeName,
			@PathVariable String resourceCodeName) {
		Config config = getConfigurationJson(envCodeName, appCodeName, resourceCodeName);
		return new JAXBElement<Config>(new QName("configuration"), Config.class, config);
	}

	@RequestMapping(value = "search/{searchExpression}", method = RequestMethod.GET)
	public @ResponseBody
	String search(@PathVariable String searchExpression) {
		List<ConfigDocument> documents = service.searchDocument(searchExpression);
		return documents.toString();
	}
}
