/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package org.workcraft.gwt.imagemap.client;

import com.google.gwt.user.client.Window;
import org.workcraft.gwt.imagemap.shared.ImageMapObject;
import org.workcraft.gwt.imagemap.shared.Point;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

import java.util.logging.Logger;

public class ImageMap extends Composite {

  public static interface ResultHandler {
    public void handleResult(int choice);
  }

  private final org.workcraft.gwt.imagemap.shared.ImageMap definition;
  private final FlowPanel imageDiv;
  private final MouseMoveHandler mouseMoveHandler;
  private final ClickHandler clickHandler;

  Image activeOverlay = null;
  int activeArea = -1;
  int lastActiveArea = -1;
  boolean hasFocus = false;

  private void clearOverlay() {
    if (activeOverlay != null) {
      activeOverlay.removeFromParent();
      activeOverlay = null;
    }
  }

  private void setActiveArea(int index) {
    clearOverlay();

    if (index != -1) {
      final Image overlay = new Image(definition.objects[index].overlayUrl);
      overlay.addStyleName("imagemap-overlay");

      imageDiv.add(overlay);

      overlay.addMouseMoveHandler(mouseMoveHandler);
      overlay.addClickHandler(clickHandler);

      activeOverlay = overlay;

      lastActiveArea = index;
    }

    activeArea = index;
  }

  private void next() {
    if (activeArea != -1) {
      int nextActive = activeArea + 1;
      if (nextActive == definition.objects.length)
        nextActive = 0;
      setActiveArea(nextActive);
    }
  }

  private void prev() {
    if (activeArea != -1) {
      int nextActive = activeArea - 1;
      if (nextActive == -1)
        nextActive = definition.objects.length - 1;
      setActiveArea(nextActive);
    }
  }

  private void prefetchImages() {
    for (ImageMapObject a : definition.objects) {
      Image.prefetch(a.overlayUrl);
    }
  }

  public ImageMap(final org.workcraft.gwt.imagemap.shared.ImageMap definition, final ResultHandler handler) {

    this.definition = definition;

    imageDiv = new FlowPanel();
    imageDiv.addStyleName("imagemap-container");

    imageDiv.getElement().setTabIndex(1);

    final Image baseImage = new Image(definition.baseImageUrl);
    baseImage.addStyleName("imagemap-base-image");

    imageDiv.add(baseImage);

    mouseMoveHandler = new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {

        double mouseX = (double)event.getRelativeX(baseImage.getElement()) / baseImage.getOffsetWidth();
        double mouseY = ((double)event.getRelativeY(baseImage.getElement()) - Window.getScrollTop()) / baseImage.getOffsetWidth();

        int mouseOverArea = -1;

        for (int i = 0; i < definition.objects.length; i++) {

          if (definition.objects[i].outline.isInside(new Point(mouseX, mouseY))) {
            mouseOverArea = i;
            break;
          }
        }

        if (mouseOverArea != activeArea) {
          if (hasFocus && mouseOverArea == -1) {
            setActiveArea(lastActiveArea);
          } else
            setActiveArea(mouseOverArea);
        }
      }
    };

    clickHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        handler.handleResult(definition.objects[activeArea].id);
      }
    };

    baseImage.addMouseMoveHandler(mouseMoveHandler);

    addDomHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        // System.out.println (event.getCharCode());

        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
          handler.handleResult(activeArea);
        }

        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_LEFT) {
          prev();
          event.preventDefault();
        }
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_RIGHT) {
          next();
          event.preventDefault();
        }
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
          next();
          event.preventDefault();
        }
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
          prev();
          event.preventDefault();
        }
      }
    }, KeyDownEvent.getType());

    addDomHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        hasFocus = true;
        if (activeArea == -1)
          setActiveArea(0);
      }
    }, FocusEvent.getType());

    addDomHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        hasFocus = false;
        setActiveArea(-1);
      }
    }, BlurEvent.getType());

    initWidget(imageDiv);

    prefetchImages();
  }


  @Override
  public String toString() {
    return "ImageMap{" +
            "definition=" + definition +
            ", imageDiv=" + imageDiv +
            ", mouseMoveHandler=" + mouseMoveHandler +
            ", clickHandler=" + clickHandler +
            ", activeOverlay=" + activeOverlay +
            ", activeArea=" + activeArea +
            ", lastActiveArea=" + lastActiveArea +
            ", hasFocus=" + hasFocus +
            '}';
  }
}