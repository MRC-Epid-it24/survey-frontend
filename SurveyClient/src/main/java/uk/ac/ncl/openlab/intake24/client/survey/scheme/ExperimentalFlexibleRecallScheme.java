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

package uk.ac.ncl.openlab.intake24.client.survey.scheme;

import org.pcollections.TreePVector;
import org.workcraft.gwt.shared.client.Option;
import org.workcraft.gwt.shared.client.Pair;
import uk.ac.ncl.openlab.intake24.client.api.survey.SurveyParameters;
import uk.ac.ncl.openlab.intake24.client.api.survey.UserData;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptManager;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.survey.rules.*;

public class ExperimentalFlexibleRecallScheme extends DefaultScheme {

    private final static double MAX_FLEXIBLE_AGE_HOURS = 24.0;

    public static final String ID = "experimental-flexible-recall";

    public ExperimentalFlexibleRecallScheme(SurveyParameters surveyParameters, String locale, final SurveyInterfaceManager interfaceManager, UserData userData) {
        super(surveyParameters, locale, interfaceManager, userData);
    }

    @Override
    protected Rules getRules(PortionSizeScriptManager scriptManager, CompoundFoodTemplateManager templateManager, RecipeManager recipeManager) {
        return new Rules(
                // meal associatedFoods
                TreePVector.<WithPriority<PromptRule<Meal, MealOperation>>>empty()
                        .plus(AskForMealTimeFlexibleRecall.withPriority(4, getStateManager()))
                        .plus(ShowEditMeal.withPriority(3))
                        .plus(ShowDrinkReminderPrompt.withPriority(2)),
//                        .plus(ShowReadyMealsPrompt.withPriority(0)),

                // food associatedFoods
                TreePVector.<WithPriority<PromptRule<FoodEntry, FoodOperation>>>empty()
//                        .plus(ShowBrandNamePromptFlexibleRecall.withPriority(-1))
                        .plus(ShowNextPortionSizeStepFlexibleRecall.withPriority(scriptManager, 0))
                        .plus(ChoosePortionSizeMethodFlexibleRecall.withPriority(1))
                        .plus(AskForMissingFoodDescriptionFlexibleRecall.withPriority(2))
                        .plus(ShowSimpleHomeRecipePromptFlexibleRecall.withPriority(2))
                        .plus(AskIfHomeRecipeFlexibleRecall.withPriority(3))
                        .plus(SplitFoodFlexibleRecall.withPriority(4))
                        .plus(InformFoodComplete.withPriority(-100)),

                // extended food propmts
                TreePVector.<WithPriority<PromptRule<Pair<FoodEntry, Meal>, MealOperation>>>empty()
                        .plus(ShowEditIngredientsPrompt.withPriority(3))
                        .plus(AskToLookupFoodFlexibleRecall.withPriority(3, locale, "paRules", 0, recipeManager))
                        .plus(ShowSameAsBeforePrompt.withPriority(3, getSchemeId(), getDataVersion(), scriptManager, templateManager, getMilkPercentageOptions()))
                        .plus(ShowHomeRecipeServingsPrompt.withPriority(2))
                        .plus(ShowTemplateRecipeSavePrompt.withPriority(1, recipeManager))
                        .plus(ShowCompoundFoodPromptFlexibleRecall.withPriority(0, locale))
                        .plus(ShowAssociatedFoodPrompt.withPriority(0, locale, Option.none()))
                        .plus(ShowBreadLinkedFoodAmountPrompt.withPriority(0))

                ,
                // global associatedFoods

                TreePVector.<WithPriority<PromptRule<Survey, SurveyOperation>>>empty()
                        .plus(ConfirmCompletionFlexibleRecall.withPriority(0, getStateManager()))
                        .plus(ShowEnergyValidationPrompt.withPriority(1, 0.0))
                        .plus(ShowEmptySurveyPrompt.withPriority(1))
                        .plus(ShowTimeGapPrompt.withPriority(2, 180, new Time(9, 0), new Time(21, 0)))

                ,

                // selection rules
                TreePVector.<WithPriority<SelectionRule>>empty()
                        .plus(SelectForPortionSize.withPriority(3))
                        .plus(SelectRawFood.withPriority(2))
                        .plus(SelectFoodForAssociatedPrompts.withPriority(1))
                        .plus(SelectIncompleteFreeEntryMeal.withPriority(1))
                        .plus(SelectMealWithNoDrink.withPriority(1))
                        .plus(SelectUnconfirmedMeal.withPriority(1)));
//                        .plus(SelectMealForReadyMeals.withPriority(1)));

    }

    @Override
    public Boolean getSurveyExpired(Survey survey) {
        Double age = (System.currentTimeMillis() - survey.lastSaved) / 3600000.0;
        return age > MAX_FLEXIBLE_AGE_HOURS;
    }
}
