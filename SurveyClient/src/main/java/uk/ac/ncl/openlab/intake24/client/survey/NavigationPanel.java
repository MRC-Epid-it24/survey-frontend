/*
This file is part of Intake24.

Copyright 2015, 2016 Newcastle University.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This file is based on Intake24 v1.0.

© Crown copyright, 2012, 2013, 2014

Licensed under the Open Government Licence 3.0: 

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.attributeparsers.AttributeParsers;
import com.google.gwt.uibinder.attributeparsers.LengthAttributeParser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Focusable;
import org.workcraft.gwt.shared.client.Callback;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.CollectionUtils.WithIndex;
import uk.ac.ncl.openlab.intake24.client.BrowserConsole;
import uk.ac.ncl.openlab.intake24.client.UnorderedList;
import uk.ac.ncl.openlab.intake24.client.ui.Layout;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;

public class NavigationPanel extends Composite {
    private static final SurveyMessages messages = SurveyMessages.Util.getInstance();

    private final FlowPanel mealsPanel;
    private FlowPanel headerButton;
    private FlowPanel headerContainer;
    private Callback requestAddMeal;
    private Callback1<Selection> requestSelection;

    public void stateChanged(final Survey state) {
        mealsPanel.clear();

        Button addMealButton = WidgetFactory.createButton(messages.addMealLabel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                requestAddMeal.call();
            }
        });

        addMealButton.getElement().setId("intake24-add-meal-button");

        UnorderedList<MealPanel> mealList = new UnorderedList<MealPanel>();
        mealList.addStyleName("intake24-meal-list");
        mealList.getElement().setTabIndex(0);

        for (WithIndex<Meal> m : state.mealsSortedByTime) {
            MealPanel p = new MealPanel(m.value, m.index, state.selectedElement, new Callback1<Selection>() {
                @Override
                public void call(Selection arg1) {
                    requestSelection.call(arg1);
                }
            });
            p.getElement().setTabIndex(0);
            mealList.addItem(p);
        }

        headerContainer = new FlowPanel();
        headerContainer.addStyleName("intake24-meals-panel-header-container");

        headerButton = new FlowPanel();
        headerButton.addStyleName("intake24-meals-panel-header-button");

        HTMLPanel header = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.navPanelHeader()));
        header.addStyleName("intake24-meals-panel-header");

        headerContainer.add(headerButton);
        headerContainer.add(header);

        mealsPanel.add(headerContainer);
        mealsPanel.add(mealList);
        mealsPanel.add(addMealButton);

        headerButton.sinkEvents(Event.ONCLICK | Event.ONTOUCHSTART);

        headerButton.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                togglePanel();
            }
        }, ClickEvent.getType());

        headerButton.addHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(TouchStartEvent touchStartEvent) {
                togglePanel();

            }
        }, TouchStartEvent.getType());

        onWindowResize();
    }

    private void togglePanel() {
        mealsPanel.getElement().toggleClassName("showing");
    }

    private void onWindowResize() {
        int panelHeight = mealsPanel.getOffsetHeight();

        FlowPanel mainContent = Layout.getMainContentPanel();

        String stringHeight = mainContent.getElement().getStyle().getProperty("minHeight");

        int curMinHeight = 0;

        if (stringHeight != null && stringHeight.endsWith("px")) {
            curMinHeight = Integer.parseInt(stringHeight.substring(0, stringHeight.length() - 2));
        }

        int adjustedMinHeight = Math.max(curMinHeight, panelHeight);

        mainContent.getElement().getStyle().setPropertyPx("minHeight", adjustedMinHeight);
    }

    public void setCallbacks(Callback1<Selection> requestSelection, Callback requestAddMeal) {
        this.requestSelection = requestSelection;
        this.requestAddMeal = requestAddMeal;
    }

    public NavigationPanel(Survey initialState) {
        mealsPanel = new FlowPanel();
        mealsPanel.getElement().setId("intake24-meals-panel");
        initWidget(mealsPanel);


        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent resizeEvent) {
                onWindowResize();
            }
        });

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                onWindowResize();
            }
        });

        stateChanged(initialState);
    }
}