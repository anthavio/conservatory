package com.anthavio.conserv.web.vaadin.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.anthavio.conserv.web.vaadin.AccessControlService;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Component
@Scope("prototype")
@VaadinView(LoginView.NAME)
public class LoginView extends CustomComponent implements View, Button.ClickListener {

	public static final String NAME = "login";

	@Autowired
	private AccessControlService access;

	private final TextField usernameInput;

	private final PasswordField passwordInput;

	private final Button loginButton;

	public LoginView() {
		setSizeFull();

		// Create the user input field
		usernameInput = new TextField("User:");
		usernameInput.setWidth("300px");
		usernameInput.setRequired(true);
		usernameInput.setInputPrompt("Your username (eg. joe@email.com)");
		usernameInput.addValidator(new EmailValidator("Username must be an email address"));
		usernameInput.setInvalidAllowed(false);

		// Create the password input field
		passwordInput = new PasswordField("Password:");
		passwordInput.setWidth("300px");
		passwordInput.addValidator(new PasswordValidator());
		passwordInput.setRequired(true);
		passwordInput.setValue("");
		passwordInput.setNullRepresentation("");

		// Create login button
		loginButton = new Button("Login", this);

		// Add both to a panel
		VerticalLayout fields = new VerticalLayout(usernameInput, passwordInput, loginButton);
		fields.setCaption("Please login to access the application. (test@test.com/passw0rd)");
		fields.setSpacing(true);
		fields.setMargin(new MarginInfo(true, true, true, false));
		fields.setSizeUndefined();

		// The view root layout
		VerticalLayout viewLayout = new VerticalLayout(fields);
		viewLayout.setSizeFull();
		viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
		viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
		setCompositionRoot(viewLayout);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		usernameInput.focus();
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (!usernameInput.isValid() || !passwordInput.isValid()) {
			return;
		}

		boolean isLoggedIn = access.login(usernameInput.getValue(), passwordInput.getValue());

		if (!isLoggedIn) {
			// Wrong password clear the password field and refocuses it
			this.passwordInput.setValue(null);
			this.passwordInput.focus();
		}
	}

	private static final class PasswordValidator extends AbstractValidator<String> {

		public PasswordValidator() {
			super("The password provided is not valid");
		}

		@Override
		protected boolean isValidValue(String value) {
			// Password must be at least 8 characters long and contain at least one number
			if (value != null && (value.length() < 8 || !value.matches(".*\\d.*"))) {
				return false;
			}
			return true;
		}

		@Override
		public Class<String> getType() {
			return String.class;
		}
	}

}
