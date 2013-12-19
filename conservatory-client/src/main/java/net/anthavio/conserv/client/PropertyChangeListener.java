package net.anthavio.conserv.client;

import net.anthavio.conserv.model.Property;

/**
 * 
 * @author martin.vanek
 *
 */
public interface PropertyChangeListener {

	/**
	 * Parameter can be null when property is being added or removed
	 */
	public void propertyChanged(Property oldValue, Property newValue);

	public void propertyAdded(Property property);

	public void propertyRemoved(Property property);
}
