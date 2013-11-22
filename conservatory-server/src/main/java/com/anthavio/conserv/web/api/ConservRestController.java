package com.anthavio.conserv.web.api;

import java.net.URI;
import java.util.ArrayList;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anthavio.conserv.model.Configuration;
import com.anthavio.conserv.model.Property;

/**
 * 
 * @author martin.vanek
 *
 */
@Controller
public class ConservRestController {

	@RequestMapping(value = "config/{application}/{environment}", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody
	Configuration getConfigurationJson(@PathVariable String application, @PathVariable String environment) {
		ArrayList<Property> properties = new ArrayList<Property>();
		properties.add(new Property("binary.property", new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7,
				8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4,
				5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
		properties.add(new Property("string.property", "Some value"));
		properties.add(new Property("integer.property", 666));
		properties.add(new Property("url.property", URI.create("http://test-www.nature.com:8080/zxzxzx")));
		return new Configuration(application, environment, properties);
	}

	@RequestMapping(value = "config/{application}/{environment}", method = RequestMethod.GET, headers = "Accept=application/xml")
	public @ResponseBody
	JAXBElement<Configuration> getConfigurationXml(@PathVariable String application, @PathVariable String environment) {
		Configuration configuration = getConfigurationJson(application, environment);
		return new JAXBElement<Configuration>(new QName("configuration"), Configuration.class, configuration);
	}

	@RequestMapping(value = "config/{application}/{environment}", method = RequestMethod.GET)
	public @ResponseBody
	JAXBElement<Configuration> getConfiguration(@PathVariable String application, @PathVariable String environment) {
		Configuration configuration = getConfigurationJson(application, environment);
		return new JAXBElement<Configuration>(new QName("configuration"), Configuration.class, configuration);
	}
}
