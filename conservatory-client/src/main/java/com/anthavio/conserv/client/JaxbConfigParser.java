package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import com.anthavio.conserv.model.Config;

/**
 * 
 * @author martin.vanek
 *
 */
public class JaxbConfigParser implements ConfigParser {

	private JAXBContext context;

	public JaxbConfigParser() {
		try {
			context = JAXBContext.newInstance(Config.class);
		} catch (JAXBException jaxbx) {
			throw new IllegalStateException("This should never happen", jaxbx);
		}
	}

	public Format getFormat() {
		return Format.XML;
	}

	@Override
	public Config parse(Reader reader) throws IOException {
		Unmarshaller unmarshaller;
		try {
			unmarshaller = context.createUnmarshaller();
			JAXBElement<Config> element = unmarshaller.unmarshal(new StreamSource(reader), Config.class);
			return element.getValue();
		} catch (JAXBException jaxbx) {
			throw new IllegalStateException("This should never happen", jaxbx);
		}
	}

	/**
	 * Well, for testing mostly...
	 */
	public String marshall(Config config) {
		Marshaller marshaller;
		try {
			marshaller = context.createMarshaller();
			StringWriter sw = new StringWriter();
			JAXBElement<Config> element = new JAXBElement<Config>(new QName("configuration"), Config.class, config);
			marshaller.marshal(element, sw);
			return sw.toString();
		} catch (JAXBException jaxbx) {
			throw new IllegalStateException("This should never happen", jaxbx);
		}
	}

}
