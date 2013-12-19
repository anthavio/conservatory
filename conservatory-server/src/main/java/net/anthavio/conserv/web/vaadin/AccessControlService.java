package net.anthavio.conserv.web.vaadin;

import net.anthavio.conserv.web.vaadin.view.LoginView;
import net.anthavio.conserv.web.vaadin.view.MainView;

import org.springframework.stereotype.Component;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Relies heavily on Vaadin ThreadLocals - VaadinSession.getCurrent() and UI.getCurrent() 
 * 
 * @author martin.vanek
 *
 */
@Component
public class AccessControlService implements ViewChangeListener {

	public static final String VIEW_AFTER_LOGIN_KEY = "VIEW_AFTER_LOGIN";

	public static final String USER_KEY = "user";

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		VaadinSession session = VaadinSession.getCurrent(); //ThreadLocal

		boolean isLoggedIn = session.getAttribute(USER_KEY) != null;
		boolean isLoginView = event.getNewView() instanceof LoginView;

		boolean allow;

		if (isLoggedIn) {
			if (isLoginView) {
				allow = false;//authenticated tries to view login view
			} else {
				allow = true; //authenticated view (access control logic could be here)
			}
		} else {
			if (isLoginView) {
				allow = true; //anonymous views login view
			} else {
				allow = false;//intercept view change
				session.setAttribute(VIEW_AFTER_LOGIN_KEY, event.getViewName());
				UI.getCurrent().getNavigator().navigateTo(LoginView.NAME);
			}
		}

		return allow;
		/*
		if (!isLoggedIn && !isLoginView) {
			session.setAttribute(VIEW_AFTER_LOGIN_KEY, event.getViewName());
			UI.getCurrent().getNavigator().navigateTo(LoginView.NAME);
			return false; //cancel because of view interception
		} else if (isLoggedIn && isLoginView) {
			return false; // cancel if someone tries to access to login view while logged in
		}
		return true;
		*/
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
		// nothing to do... 
	}

	public boolean login(String username, String password) {

		//fake validation
		boolean isValid = true;//username.equals("test@test.com") && password.equals("passw0rd");

		if (isValid) {
			VaadinSession session = VaadinSession.getCurrent(); //ThreadLocal
			session.setAttribute(USER_KEY, username);
			String viewName = (String) session.getAttribute(VIEW_AFTER_LOGIN_KEY);
			if (viewName == null) {
				viewName = MainView.NAME;
			}
			UI.getCurrent().getNavigator().navigateTo(viewName);
		}

		return isValid;
	}
}
