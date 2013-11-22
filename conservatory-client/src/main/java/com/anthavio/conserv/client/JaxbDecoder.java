package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.anthavio.conserv.client.ConservClient.ConfigurationDecorer;
import com.anthavio.conserv.model.Configuration;

/**
 * 
 * @author martin.vanek
 *
 */
public class JaxbDecoder implements ConfigurationDecorer {

	private JAXBContext context;

	public JaxbDecoder() {
		try {
			context = JAXBContext.newInstance(Configuration.class);
		} catch (JAXBException jaxbx) {
			throw new IllegalStateException("This should never happen", jaxbx);
		}
	}

	@Override
	public Configuration decode(InputStream stream) throws IOException {
		Unmarshaller unmarshaller;
		try {
			unmarshaller = context.createUnmarshaller();
			JAXBElement<Configuration> element = unmarshaller.unmarshal(new StreamSource(stream), Configuration.class);
			return element.getValue();
		} catch (JAXBException jaxbx) {
			throw new IllegalStateException("This should never happen", jaxbx);
		}
	}

}
