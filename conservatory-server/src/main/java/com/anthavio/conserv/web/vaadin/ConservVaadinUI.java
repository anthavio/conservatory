package com.anthavio.conserv.web.vaadin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.DiscoveryNavigator;

import com.anthavio.conserv.web.vaadin.view.LoginView;
import com.vaadin.annotations.Title;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;

@Title("Conservatory")
@Component("ConservVaadinUI")
@Scope("prototype")
//@Theme("myTheme")
public class ConservVaadinUI extends UI implements ErrorHandler {
	/*
	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}
	*/
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient ApplicationContext applicationContext;

	@Autowired
	private SessionCounter sessionCounter;

	@Autowired
	private ApplicationCounter applicationCounter;

	@Autowired
	private AccessControlService access;

	@Override
	protected void init(final VaadinRequest request) {
		setSizeFull();
		//getSession().getConfiguration().getHeartbeatInterval()

		getSession().setErrorHandler(this);
		//DiscoveryNavigator automaticaly gathers Spring managed @VaadinView anotated Views  
		DiscoveryNavigator navigator = new DiscoveryNavigator(this, this);

		getNavigator().addViewChangeListener(access);

		//Notification.show(String.format("Session counter: %d, application counter: %d", sessionCounter.getCount(),
		//		applicationCounter.getCount()));

		Button logout = new Button("Logout");
		logout.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getNavigator().navigateTo(LoginView.NAME);
				//getUI().getPage().setLocation("/myapp/logoutpage.html");
				getSession().close();
			}
		});
	}

	@Override
	public void error(com.vaadin.server.ErrorEvent event) {
		//Use event.getThrowable() to decide action to be taken
		DefaultErrorHandler.doDefault(event);
	}
	/*
		@Override
		public boolean beforeViewChange(ViewChangeEvent event) {
			boolean isLoggedIn = getSession().getAttribute(LoginView.USER_KEY) != null;
			boolean isLoginView = event.getNewView() instanceof LoginView;

			if(isLoggedIn)
			if (!isLoggedIn && !isLoginView) {
				getSession().setAttribute(LoginView.VIEW_AFTER_LOGIN_KEY, event.getViewName());
				getNavigator().navigateTo(LoginView.NAME);
				return false; //cancel because of view interception

			} else if (isLoggedIn && isLoginView) {
				return false; // cancel if someone tries to access to login view while logged in
			}

			return true;
		}

		@Override
		public void afterViewChange(ViewChangeEvent event) {
			// nothing to do... 
		}
		*/
}
