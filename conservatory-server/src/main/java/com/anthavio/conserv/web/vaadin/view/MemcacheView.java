package com.anthavio.conserv.web.vaadin.view;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

@Component
@Scope("prototype")
@VaadinView(value = MemcacheView.NAME, cached = true)
public class MemcacheView extends Panel implements View {

	public static final String NAME = "memcache";

	public static enum Units {

		Seconds(TimeUnit.SECONDS), Minutes(TimeUnit.MINUTES), Hours(TimeUnit.HOURS), Days(TimeUnit.DAYS);

		private final TimeUnit tu;

		private Units(TimeUnit tu) {
			this.tu = tu;
		}

		public int getSeconds(int value) {
			return (int) tu.toSeconds(value);
		}
	}

	//@Autowired
	//private NpisCoreSpringConfig config;

	private Label labelUrl = new Label();

	private TextField keyInput = new TextField("Key");

	private TextField expiryInput = new TextField("Expiry");

	private Button buttonGet = new Button("GET");

	private Button buttonSet = new Button("SET");

	private ComboBox unitsCombo = new ComboBox("Units", Arrays.asList(Units.values()));

	/*
		@Autowired
		private MemcachedClient client;

		private AceEditor editor = new AceEditor();

		public MemcacheView() {
			setSizeFull();
			VerticalLayout layout = new VerticalLayout();
			layout.setSpacing(true);
			layout.setMargin(true);

			keyInput.setRequired(true);
			keyInput.addShortcutListener(new ShortcutListener("Enter", KeyCode.ENTER, null) {
				@Override
				public void handleAction(Object sender, Object target) {
					if (target == keyInput) {
						String value = keyInput.getValue();
						if (!StringUtil.isEmpty(value)) {
							search(value);
						}
					}
				}
			});

			keyInput.setNullSettingAllowed(false);
			keyInput.addTextChangeListener(new TextChangeListener() {

				@Override
				public void textChange(TextChangeEvent event) {
					doButtonsState(event.getText(), editor.getValue(), expiryInput.getValue());
				}
			});

			buttonGet.setEnabled(false);
			buttonGet.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					String value = keyInput.getValue();
					if (!StringUtil.isEmpty(value)) {
						search(value);
					}
				}
			});

			expiryInput.addTextChangeListener(new TextChangeListener() {

				@Override
				public void textChange(TextChangeEvent event) {
					doButtonsState(keyInput.getValue(), editor.getValue(), event.getText());
				}
			});
			//StringToIntegerConverter
			expiryInput.setRequired(true);
			expiryInput.setInvalidAllowed(false);
			expiryInput.setNullSettingAllowed(false);
			expiryInput.setValue("10");
			expiryInput.setConverter(new StringToIntegerConverter());
			//expiryInput.addValidator(new IntegerValidator("Not a number"));

			unitsCombo.setRequired(true);
			unitsCombo.setTextInputAllowed(false);
			unitsCombo.setNullSelectionAllowed(false);
			unitsCombo.select(Units.Minutes);

			buttonSet.setEnabled(false);
			buttonSet.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					final String key = keyInput.getValue();
					final String value = editor.getValue();
					int expiry = Integer.parseInt(expiryInput.getValue());
					Units units = (Units) unitsCombo.getValue();
					final int x = units.getSeconds(expiry);

					String message = "Really set " + key + " for " + expiry + " " + units + " ?";
					MessageBoxListener listener = new MessageBoxListener() {

						@Override
						public void buttonClicked(ButtonId buttonId) {
							if (buttonId == ButtonId.YES) {
								client.set(key, x, value);
							}

						}
					};
					MessageBox.showPlain(Icon.WARN, "Cache set", message, listener, ButtonId.YES, ButtonId.NO);
				}
			});

			editor.setHeight(300, Unit.PIXELS);
			editor.setWidth(800, Unit.PIXELS);

			editor.addTextChangeListener(new TextChangeListener() {

				@Override
				public void textChange(TextChangeEvent event) {
					doButtonsState(keyInput.getValue(), event.getText(), expiryInput.getValue());
				}
			});

			layout.addComponent(labelUrl);
			layout.addComponent(new HorizontalLayout(keyInput, buttonGet));
			layout.addComponent(editor);
			layout.addComponent(new HorizontalLayout(expiryInput, unitsCombo, buttonSet));
			setContent(layout);
		}

		private void doButtonsState(String key, String value, String expiry) {
			boolean keyOk = !StringUtil.isEmpty(key);

			buttonGet.setEnabled(keyOk);

			boolean valueOk = !StringUtil.isEmpty(value);

			boolean expiryOk;
			try {
				Integer.parseInt(expiry);
				expiryOk = true;
			} catch (Exception x) {
				expiryOk = false;
			}

			buttonSet.setEnabled(keyOk && valueOk && expiryOk);
		}

		@PostConstruct
		public void init() {
			labelUrl.setValue(config.getMemcachedUrl());
			labelUrl.setCaption("Memcached Server");
		}

		public void search(String cacheKey) {
			Object value = client.get(cacheKey);
			if (value == null) {
				Notification.show("Nothing found for key '" + cacheKey + "'", Notification.Type.WARNING_MESSAGE);
			} else if (value instanceof String) {
				editor.setValue((String) value);
			} else if (value instanceof CacheEntry) {
				CacheEntry entry = (CacheEntry) value;
				//entry.getSinceDate()
				value = entry.getValue();
				if (value instanceof String) {
					editor.setValue((String) value);
				} else {
					Notification.show("Value for key '" + cacheKey + "' is of unsupported type " + value.getClass(),
							Notification.Type.WARNING_MESSAGE);
				}
			} else {
				Notification.show("Value for key '" + cacheKey + "' is of unsupported type " + value.getClass(),
						Notification.Type.WARNING_MESSAGE);
			}
		}

		@Override
		public void enter(ViewChangeEvent event) {
			//For http://xxx.yyy.zzz/npis/vaadin#!memcache/zxzx/ababab Parameters string is "zxzx/ababab"
			String parameters = event.getParameters();
			if (!StringUtil.isEmpty(parameters)) {
				keyInput.setValue(parameters);
				buttonGet.setEnabled(true);
				search(parameters);
			}
		}
	*/

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}
}
