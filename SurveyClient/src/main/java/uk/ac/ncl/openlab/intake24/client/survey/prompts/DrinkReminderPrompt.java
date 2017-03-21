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

package uk.ac.ncl.openlab.intake24.client.survey.prompts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.Function1;
import uk.ac.ncl.openlab.intake24.client.survey.Meal;
import uk.ac.ncl.openlab.intake24.client.survey.Prompt;
import uk.ac.ncl.openlab.intake24.client.survey.SurveyStageInterface;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;

public class DrinkReminderPrompt implements Prompt<Meal, MealOperation> {
    private final Meal meal;
    private final PromptMessages messages = GWT.create(PromptMessages.class);

    public DrinkReminderPrompt(Meal meal) {
        this.meal = meal;
    }

    @Override
    public SurveyStageInterface getInterface(final Callback1<MealOperation> onComplete,
                                             final Callback1<Function1<Meal, Meal>> onIntermediateStateChange) {
        final SafeHtml promptText = SafeHtmlUtils.fromSafeConstant(messages.drinkReminder_promptText((SafeHtmlUtils.htmlEscape(meal.name.toLowerCase()))));
        final String addDrinkText = SafeHtmlUtils.htmlEscape(messages.drinkReminder_addDrinkButtonLabel());
        final String noDrinkText = SafeHtmlUtils.htmlEscape(messages.drinkReminder_noDrinkButtonLabel());

        FlowPanel content = new FlowPanel();

        final HTMLPanel header = new HTMLPanel("h1", meal.name + " (" + meal.time.map(argument -> argument.toString()).getOrElse("Time unknown") + ")");

        content.add(header);

        content.add(new YesNoQuestion(promptText, addDrinkText, noDrinkText, new YesNoQuestion.ResultHandler() {
            @Override
            public void handleYes() {
                onComplete.call(MealOperation.editFoodsRequest(true));
            }

            @Override
            public void handleNo() {
                onComplete.call(MealOperation.update(argument -> argument.markConfirmedNoDrinks()));
            }
        }));

        return new SurveyStageInterface.Aligned(content, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP, SurveyStageInterface.DEFAULT_OPTIONS, DrinkReminderPrompt.class.getSimpleName());
    }

    @Override
    public String toString() {
        return "Drink reminder prompt";
    }
}