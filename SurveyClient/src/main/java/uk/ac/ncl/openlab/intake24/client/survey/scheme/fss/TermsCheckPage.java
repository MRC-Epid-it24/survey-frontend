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

package uk.ac.ncl.openlab.intake24.client.survey.scheme.fss;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.Callback2;
import uk.ac.ncl.openlab.intake24.client.api.auth.AuthCache;
import uk.ac.ncl.openlab.intake24.client.api.survey.UserData;
import uk.ac.ncl.openlab.intake24.client.api.uxevents.UxEventsHelper;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.ui.LogoutPage;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;


public class TermsCheckPage implements SurveyStage<Survey> {
    public static final String TERMS_CHECK_DONE = "terms-check-done";

    private static final SurveyMessages surveyMessages = SurveyMessages.Util.getInstance();

    private final Survey initialData;

    public TermsCheckPage(Survey initialData) {
        this.initialData = initialData;
    }

    @Override
    public SimpleSurveyStageInterface getInterface(final Callback1<Survey> onComplete,
                                                   Callback2<Survey, Boolean> onIntermediateStateChange) {

        FlowPanel container = new FlowPanel();
        container.getElement().addClassName("intake24-survey-container");

        FlowPanel header = new FlowPanel();
        header.getElement().addClassName("intake24-survey-header");

        HTMLPanel h1 = new HTMLPanel("h1", surveyMessages.nameCheckPage_header());
        header.add(h1);

        FlowPanel content = new FlowPanel();
        content.getElement().addClassName("intake24-survey-content");

        HTMLPanel text1 = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(
                "<p>Throughout this food diary the questions will be phrased as \"what did you have to eat for ...\"</p>" +
                "<p>This is in relation to what <strong>the child selected in the survey you just completed had to eat yesterday.</strong> " +
                "Please respond for the child named throughout.</p>"));

        HTMLPanel text2 = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(
                "<p>I can confirm that I will respond in relation to the food consumed yesterday by the child mentioned " +
                        "in the survey (please note this might be you if you are the child named in the survey).</p>"));

        content.add(text1);
        content.add(text2);

        FlowPanel buttons = new FlowPanel("div");
        final Button yesButton = WidgetFactory.createGreenButton("Yes, I agree", "termsCheckPageButtonYes", new ClickHandler() {
            public void onClick(ClickEvent event) {
                onComplete.call(initialData.withFlag(TERMS_CHECK_DONE));
            }
        }, "intake24-button-md");

        buttons.add(yesButton);

        content.add(buttons);

        container.add(header);
        container.add(content);

        return new SimpleSurveyStageInterface(container, TermsCheckPage.class.getSimpleName());
    }
}
