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
package com.googlecode.mgwt.ui.client.widget.list;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.googlecode.mgwt.ui.client.theme.base.ListCss;


/**
 * @author Daniel Kurka
 * 
 */
public class WidgetList extends Composite implements HasWidgets {

	private static class ULFlowPanel extends ComplexPanel {

		public ULFlowPanel() {
			setElement(Document.get().createULElement());
		}

		@Override
		public void add(Widget w) {
			add(w, getElement());
		}
	}

	private Panel container;
	private Map<Widget, Widget> map;
	protected final ListCss css;

	public WidgetList() {
		this(MGWTStyle.getDefaultClientBundle().getListCss());
	}

	public WidgetList(ListCss css) {
		this.css = css;
		css.ensureInjected();
		container = new ULFlowPanel();
		initWidget(container);

		setStylePrimaryName(css.listCss());

		map = new HashMap<Widget, Widget>();
	}

	@Override
	public void add(Widget w) {
		WidgetListEntry widgetListEntry = new WidgetListEntry(css);
		widgetListEntry.add(w);
		map.put(w, widgetListEntry);
		container.add(widgetListEntry);

	}

	@Override
	public void clear() {
		container.clear();
		map.clear();

	}

	@Override
	public Iterator<Widget> iterator() {
		return map.values().iterator();
	}

	@Override
	public boolean remove(Widget w) {
		Widget entry = map.remove(w);
		if (entry == null)
			return false;

		return container.remove(entry);

	}

	public void setRound(boolean round) {
		if (round) {
			addStyleName(css.round());
		} else {
			removeStyleName(css.round());
		}
	}

}