/*
 * Copyright 2010 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.ui.client.widget.input.search;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.widget.base.HasPlaceHolder;
import com.googlecode.mgwt.ui.client.widget.button.ButtonBase;

/**
 * A search widget
 * 
 * <h2>Styling</h2> The DOM structure looks like:
 * 
 * <pre>
 * &lt;div class="mgwt-SearchBox">
 * 	&lt;form class="mgwt-SearchBox-round">
 * 		&lt;input class="mgwt-SearchBox-input" />
 * 	&lt;/form>
 * 	&lt;div class="mgwt-SearchBox-clear"/>
 * &lt;/div>
 * </pre>
 * 
 * 
 * 
 * If the clear element is touched its class is changed to
 * .mgwt-SearchBox-clear-active
 * 
 * @author Daniel Kurka Date: 30.05.2010
 * 
 */

public class MSearchBox extends Composite implements HasChangeHandlers, HasText, HasName, HasValue<String>, HasPlaceHolder, HasAllKeyHandlers {
  public static class MSearchBoxButton extends ButtonBase {
    public MSearchBoxButton() {
      this(DEFAULT_APPEARANCE);
    }
    
    public MSearchBoxButton(MSearchBoxAppearance appearance) {
      super(appearance);
      setElement(appearance.uiBinder().createAndBindUi(this));
    }
  }
  
  private class SearchBoxChangeHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			if (!MGWT.getOsDetection().isDesktop()) {
				if (textBox.getValue().length() > 0) {
					clearButton.setVisible(true);
				} else {
				  clearButton.setVisible(false);
				}
			}
		}
	}

	private static final MSearchBoxAppearance DEFAULT_APPEARANCE = GWT.create(MSearchBoxAppearance.class);
	
	@UiField
	protected TextBox textBox;
	
	@UiField
	protected ButtonBase clearButton;
	
	@UiField
	protected FormPanel form;
	
	private HandlerRegistration clearButtonHandler;
	private HandlerRegistration boxHandler;
  private MSearchBoxAppearance appearance;
	

	public MSearchBox() {
		this(DEFAULT_APPEARANCE);
	}

  public MSearchBox(MSearchBoxAppearance appearance) {
    this.appearance = appearance;
    initWidget(this.appearance.uiBinderBox().createAndBindUi(this));

    form.addSubmitHandler(new SubmitHandler() {
      @Override
      public void onSubmit(SubmitEvent event) {
        event.cancel();
      }
    });

    textBox.getElement().setAttribute("autocapitalize", "off");
    textBox.getElement().setAttribute("autocorrect", "off");
    textBox.getElement().setAttribute("type", "search");
    setPlaceHolder("Search");
  }

	@Override
	protected void onAttach() {
		super.onAttach();

		clearButtonHandler = clearButton.addTapHandler(new TapHandler() {
      
      @Override
      public void onTap(TapEvent event) {
        clearSearchField();
      }
    });

		boxHandler = textBox.addKeyUpHandler(new SearchBoxChangeHandler());
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		clearButtonHandler.removeHandler();
		boxHandler.removeHandler();
	}

	@Override
	public void setPlaceHolder(String text) {
		textBox.getElement().setAttribute("placeholder", text);
	}

	private void clearSearchField() {
		textBox.setValue("",true);
		clearButton.setVisible(false);
	}

	@Override
	public String getPlaceHolder() {
		return textBox.getElement().getAttribute("placeholder");
	}

	@Override
	public String getText() {
		return textBox.getText();
	}

	@Override
	public void setText(String text) {
		textBox.setText(text);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> stringValueChangeHandler) {
		return textBox.addValueChangeHandler(stringValueChangeHandler);
	}

	@Override
	public void setName(String name) {
		textBox.setName(name);
	}

	@Override
	public String getName() {
		return textBox.getName();
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return textBox.addChangeHandler(handler);
	}

	@Override
	public String getValue() {
		return textBox.getValue();
	}

	@Override
	public void setValue(String value) {
		textBox.setValue(value);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		textBox.setValue(value, fireEvents);
	}

	@Override
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return textBox.addKeyUpHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return textBox.addKeyDownHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return textBox.addKeyPressHandler(handler);
	}

	@UiFactory
	protected MSearchBoxAppearance getAppearance() {
		return appearance;
	}
}
