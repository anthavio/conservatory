package net.anthavio.conserv.web.vaadin.view;

import java.security.GeneralSecurityException;

import javax.annotation.PostConstruct;

import net.anthavio.conserv.web.vaadin.ChooseLanguage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinMessageSource;
import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * @author xpoft
 */
@Component
@Scope("prototype")
@VaadinView(ErrorView.NAME)
public class ErrorView extends Panel implements View {
	private static Logger logger = LoggerFactory.getLogger(ErrorView.class);

	public static final String NAME = "error";

	@Autowired
	private ChooseLanguage chooseLanguage;

	@Autowired
	private VaadinMessageSource messageSource;

	@PostConstruct
	public void PostConstruct() throws GeneralSecurityException {
		setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		layout.addComponent(chooseLanguage);

		layout.addComponent(new Label(messageSource.getMessage("error_view.reload")));

		layout.addComponent(new Button(messageSource.getMessage("error_view.turn_off"), new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				Notification.show("one");
			}
		}));

		layout.addComponent(new Button(messageSource.getMessage("main.go_back"), new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				Page.getCurrent().setUriFragment("!" + MainView.NAME);
			}
		}));

		setContent(layout);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}
}
