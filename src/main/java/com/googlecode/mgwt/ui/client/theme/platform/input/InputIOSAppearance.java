package com.googlecode.mgwt.ui.client.theme.platform.input;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;

public class InputIOSAppearance implements InputAppearance {

  static {
    Resources.INSTANCE.css().ensureInjected();
  }

  interface Resources extends ClientBundle {

    Resources INSTANCE = GWT.create(Resources.class);

    @Source({"input-base.css", "input-ios.css"})
    InputCss css();
  }

  @Override
  public InputCss css() {
    return Resources.INSTANCE.css();
  }

}
