/*
 * Copyright 2012 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.ui.client.widget.carousel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.collection.shared.LightArrayInt;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeEvent;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.widget.carousel.CarouselAppearance.CarouselCss;
import com.googlecode.mgwt.ui.client.widget.layout.LayoutPanel;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollEndEvent;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanel;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollRefreshEvent;
import com.googlecode.mgwt.ui.client.widget.touch.TouchWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * the carousel widget renders its children in a horizontal row. users can select a different child
 * by swiping between them
 * 
 * @author Daniel Kurka
 * 
 */
public class Carousel extends Composite implements HasWidgets, HasSelectionHandlers<Integer> {

  private static class CarouselIndicatorContainer extends Composite {
    private FlowPanel main;
    private final CarouselCss css;
    private ArrayList<CarouselIndicator> indicators;
    private int selectedIndex;

    public CarouselIndicatorContainer(CarouselCss css, int numberOfPages) {
      if (numberOfPages < 0) {
        throw new IllegalArgumentException();
      }
      this.css = css;
      main = new FlowPanel();
      initWidget(main);

      main.addStyleName(this.css.indicatorContainer());

      indicators = new ArrayList<Carousel.CarouselIndicator>(numberOfPages);
      selectedIndex = 0;

      for (int i = 0; i < numberOfPages; i++) {
        CarouselIndicator indicator = new CarouselIndicator(css);
        indicators.add(indicator);
        main.add(indicator);

      }

      setSelectedIndex(selectedIndex);
    }

    public void setSelectedIndex(int index) {
      if (indicators.isEmpty()) {
        selectedIndex = -1;
        return;
      }
      if (selectedIndex != -1) {
        indicators.get(selectedIndex).setActive(false);
      }
      selectedIndex = index;
      if (selectedIndex != -1) {
        indicators.get(selectedIndex).setActive(true);

      }
    }
  }

  private static class CarouselIndicator extends TouchWidget {
    private final CarouselCss css;

    public CarouselIndicator(CarouselCss css) {
      this.css = css;
      setElement(Document.get().createDivElement());

      addStyleName(css.indicator());

    }

    public void setActive(boolean active) {
      if (active) {
        addStyleName(css.indicatorActive());
      } else {
        removeStyleName(css.indicatorActive());
      }
    }
  }

  private FlowPanel main;
  private ScrollPanel scrollPanel;
  private FlowPanel container;
  private CarouselIndicatorContainer carouselIndicatorContainer;
  private boolean isVisibleCarouselIndicator = true;

  private int currentPage;

  private Map<Widget, Widget> childToHolder;
  private com.google.web.bindery.event.shared.HandlerRegistration refreshHandler;

  private static final CarouselImpl IMPL = GWT.create(CarouselImpl.class);

  private static final CarouselAppearance DEFAULT_APPEARANCE = GWT.create(CarouselAppearance.class);
  private final CarouselAppearance appearance;
  
  /**
   * Construct a carousel widget with the default css
   */
  public Carousel() {
    this(DEFAULT_APPEARANCE);
  }

  /**
   * Construct a carousel widget with a given css
   * 
   * @param css the css to use
   */
  public Carousel(CarouselAppearance appearance) {

    this.appearance = appearance;
    childToHolder = new HashMap<Widget, Widget>();
    main = new FlowPanel();
    initWidget(main);

    main.addStyleName(this.appearance.css().carousel());

    scrollPanel = new ScrollPanel();
    scrollPanel.addStyleName(this.appearance.css().carouselScroller());

    main.add(scrollPanel);

    container = new FlowPanel();
    container.addStyleName(this.appearance.css().carouselContainer());

    scrollPanel.setWidget(container);

    scrollPanel.setSnap(true);
    scrollPanel.setMomentum(false);
    scrollPanel.setShowScrollBarX(false);
    scrollPanel.setShowScrollBarY(false);
    scrollPanel.setScrollingEnabledY(true);
    scrollPanel.setAutoHandleResize(false);

    currentPage = 0;

    scrollPanel.addScrollEndHandler(new ScrollEndEvent.Handler() {

      @Override
      public void onScrollEnd(ScrollEndEvent event) {
        int page = scrollPanel.getCurrentPageX();

        carouselIndicatorContainer.setSelectedIndex(page);
        currentPage = page;
        SelectionEvent.fire(Carousel.this, currentPage);

      }
    });

    MGWT.addOrientationChangeHandler(new OrientationChangeHandler() {

      @Override
      public void onOrientationChanged(OrientationChangeEvent event) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

          @Override
          public void execute() {
            refresh();

          }
        });
      }
    });

    addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {

        carouselIndicatorContainer.setSelectedIndex(currentPage);

      }
    });

    if (MGWT.getOsDetection().isDesktop()) {
      Window.addResizeHandler(new ResizeHandler() {

        @Override
        public void onResize(ResizeEvent event) {
          Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
              refresh();

            }
          });

        }
      });
    }

  }

  @Override
  public void add(Widget w) {

    LayoutPanel widgetHolder = new LayoutPanel();
    widgetHolder.addStyleName(this.appearance.css().carouselHolder());
    widgetHolder.add(w);

    childToHolder.put(w, widgetHolder);

    container.add(widgetHolder);

  }

  @Override
  public void clear() {
    container.clear();
    childToHolder.clear();

  }

  @Override
  public Iterator<Widget> iterator() {
    Set<Widget> keySet = childToHolder.keySet();
    return keySet.iterator();
  }

  @Override
  public boolean remove(Widget w) {
    Widget holder = childToHolder.remove(w);
    if (holder != null) {
      return container.remove(holder);
    }
    return false;

  }

  @Override
  protected void onAttach() {
    super.onAttach();
    refresh();
  }

  /**
   * refresh the carousel widget, this is necessary after changing child elements
   */
  public void refresh() {

    IMPL.adjust(main, container);

    scrollPanel.setScrollingEnabledX(true);
    scrollPanel.setScrollingEnabledY(false);

    scrollPanel.setShowScrollBarX(false);
    scrollPanel.setShowScrollBarY(false);

    if (carouselIndicatorContainer != null) {
      carouselIndicatorContainer.removeFromParent();

    }

    int widgetCount = container.getWidgetCount();

    carouselIndicatorContainer = new CarouselIndicatorContainer(this.appearance.css(), widgetCount);

    if(isVisibleCarouselIndicator){
      main.add(carouselIndicatorContainer);
    }

    if (currentPage >= widgetCount) {
      currentPage = widgetCount - 1;
    }

    carouselIndicatorContainer.setSelectedIndex(currentPage);

    scrollPanel.refresh();

    refreshHandler = scrollPanel.addScrollRefreshHandler(new ScrollRefreshEvent.Handler() {

      @Override
      public void onScrollRefresh(ScrollRefreshEvent event) {
        refreshHandler.removeHandler();
        refreshHandler = null;

        scrollPanel.scrollToPage(currentPage, 0, 0);

      }
    });

  }

  public void setSelectedPage(int index) {
    LightArrayInt pagesX = scrollPanel.getPagesX();
    if (index < 0 || index >= pagesX.length()) {
      throw new IllegalArgumentException("invalid value for index: " + index);
    }
    currentPage = index;
    scrollPanel.scrollToPage(index, 0, 300);
  }

  public int getSelectedPage() {
    return currentPage;
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public ScrollPanel getScrollPanel() {
    return scrollPanel;
  }

  /**
   * 
   * @author Daniel Kurka
   * 
   */
  public static interface CarouselImpl {

    /**
     * 
     * @param main
     * @param container
     */
    void adjust(FlowPanel main, FlowPanel container);

  }

  /**
   * 
   * @author Daniel Kurka
   * 
   */
  public static class CarouselImplSafari implements CarouselImpl {

    @Override
    public void adjust(FlowPanel main, FlowPanel container) {
      int widgetCount = container.getWidgetCount();

      double sizeFactor = 100d / widgetCount;

      for (int i = 0; i < widgetCount; i++) {
        container.getWidget(i).setWidth(sizeFactor + "%");
      }

      container.setWidth((widgetCount * 100) + "%");

    }

  }

  /**
   * 
   * @author Daniel Kurka
   * 
   */
  public static class CarouselImplGecko implements CarouselImpl {

    @Override
    public void adjust(FlowPanel main, FlowPanel container) {
      int widgetCount = container.getWidgetCount();
      int offsetWidth = main.getOffsetWidth();

      container.setWidth(widgetCount * offsetWidth + "px");

      for (int i = 0; i < widgetCount; i++) {
        container.getWidget(i).setWidth(offsetWidth + "px");
      }

    }

  }

  /**
   * Set if carousel indicator is displayed.
   */
  public void setShowCarouselIndicator(boolean isVisibleCarouselIndicator) {
    if (!isVisibleCarouselIndicator && carouselIndicatorContainer != null) {
      carouselIndicatorContainer.removeFromParent();
    }

    this.isVisibleCarouselIndicator = isVisibleCarouselIndicator;
  }

  @UiFactory
  protected CarouselAppearance getAppearance() {
	return appearance;
  }
}
