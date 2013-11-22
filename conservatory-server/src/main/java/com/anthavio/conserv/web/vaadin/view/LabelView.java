package com.anthavio.conserv.web.vaadin.view;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * @author xpoft
 */
@Component
@Scope("prototype")
@VaadinView(LabelView.NAME)
public class LabelView extends Panel implements View {
	public static final String NAME = "label";

	@PostConstruct
	public void PostConstruct() {
		setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		layout.addComponent(new Label("It's a label!"));
		layout.addComponent(new Button("Go back", new Button.ClickListener() {
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
