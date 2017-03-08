/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey.rules;

import org.pcollections.PSet;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.EditMealPrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;

public class ShowEditMeal implements PromptRule<Meal, MealOperation> {
    @Override
    public Option<Prompt<Meal, MealOperation>> apply(Meal data, SelectionMode selectionType, PSet<String> surveyFlags) {
        if (!data.freeEntryComplete() || selectionType == SelectionMode.MANUAL_SELECTION) {
            return new Option.Some<Prompt<Meal, MealOperation>>(new EditMealPrompt(data, false));
        } else {
            return new Option.None<Prompt<Meal, MealOperation>>();
        }
    }

    @Override
    public String toString() {
        return "Show edit meal prompt";
    }

    public static WithPriority<PromptRule<Meal, MealOperation>> withPriority(int priority) {
        return new WithPriority<PromptRule<Meal, MealOperation>>(new ShowEditMeal(), priority);
    }
}