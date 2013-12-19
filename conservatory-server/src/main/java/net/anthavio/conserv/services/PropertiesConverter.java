package net.anthavio.conserv.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.anthavio.NeverHappenException;
import net.anthavio.conserv.model.Property;
import net.anthavio.conserv.model.Property.ValueType;
import net.anthavio.util.PropertiesUtil;
import net.anthavio.util.PropertiesUtil.PropertyLine;

/**
 * 
 * @author martin.vanek
 *
 */
public class PropertiesConverter {

	private static final PropertiesConverter instance = new PropertiesConverter();

	public static PropertiesConverter instance() {
		return instance;
	}

	/**
	 * Convert from editable structure to canonical document 
	 */
	public String convert(List<PropertyLine> properties) {
		StringBuilder sb = new StringBuilder();
		for (PropertyLine propertyLine : properties) {
			sb.append(propertyLine.toFileLine()).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Convert from canonical document to editable structure 
	 */
	public List<PropertyLine> convert(String document) {
		List<PropertyLine> plines;
		try {
			plines = PropertiesUtil.load(new StringReader(document));
		} catch (IOException iox) {
			throw new NeverHappenException(iox);
		}
		return plines;
	}

	/**
	 * Convert from document to client structure
	 * Removes all comments not belonging to any property 
	 */
	public List<Property> convertForClient(String document) {
		List<PropertyLine> plines = convert(document);
		return convertForClient(plines);
	}

	public List<Property> convertForClient(List<PropertyLine> plines) {
		List<Property> retval = new ArrayList<Property>();
		for (int i = 0; i < plines.size(); ++i) {
			PropertyLine pline = plines.get(i);
			if (pline.isComment()) {
				if (pline.isProperty()) {
					//skip commented property
				} else {
					//if line comment is immediately before property, treat it as comment belonging to that property 
					if (isNextLineProperty(plines, i)) {
						String comment = pline.getName();
						pline = plines.get(++i); //get property on next line
						String name = pline.getName();
						String value = pline.getValue();
						retval.add(new Property(name, getValueType(name, value), value, comment));
					} else {
						//skip comment line
					}
				}
			} else if (pline.isProperty()) {
				String name = pline.getName();
				String value = pline.getValue();
				retval.add(new Property(name, getValueType(name, value), value, null));
			}// skip empty lines and mistakes
		}
		return retval;
	}

	private ValueType getValueType(String name, String value) {
		if (value == null || value.length() == 0) {
			if (name.contains("password")) {
				return ValueType.PASSWORD;
			} else {
				return ValueType.STRING;
			}
		} else {

			try {
				Integer.parseInt(value);
				return ValueType.INTEGER;
			} catch (NumberFormatException x) {
				//ok ok, it is not
			}

			try {
				Float.parseFloat(value);
				return ValueType.FLOAT;
			} catch (NumberFormatException x) {
				//ok ok, it is not
			}

			try {
				new URL(value);
				return ValueType.URL;
			} catch (MalformedURLException x) {
				//ok ok, it is not
			}

			//DATE_TIME must be tried before DATE! 

			try {
				new SimpleDateFormat(Property.DATE_TIME_FORMAT).parse(value);
				return ValueType.DATE_TIME;
			} catch (ParseException x) {
				//ok ok, it is not
			}

			try {
				new SimpleDateFormat(Property.DATE_FORMAT).parse(value);
				return ValueType.DATE;
			} catch (ParseException x) {
				//ok ok, it is not
			}

		}
		//extremely sophisticated algorithm for password type detection
		if (name.contains("password")) {
			return ValueType.PASSWORD;
		} else {
			return ValueType.STRING;
		}
	}

	private boolean isNextLineProperty(List<PropertyLine> plines, int i) {
		if (plines.size() > i + 1) {
			PropertyLine pline = plines.get(i + 1);
			return pline.isProperty() && !pline.isComment();
		}
		return false;
	}

}
