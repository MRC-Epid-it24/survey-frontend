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

package uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns;


import uk.ac.ncl.openlab.intake24.client.api.survey.SurveyParameters;
import uk.ac.ncl.openlab.intake24.client.api.survey.UserData;
import uk.ac.ncl.openlab.intake24.client.survey.CompoundFoodTemplateManager;
import uk.ac.ncl.openlab.intake24.client.survey.RecipeManager;
import uk.ac.ncl.openlab.intake24.client.survey.Rules;
import uk.ac.ncl.openlab.intake24.client.survey.SurveyInterfaceManager;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptManager;


public class October2019Behaviour extends October2019 {
    public static final String ID = "ndns1019_behaviour";

    public October2019Behaviour(String locale, SurveyParameters surveyParameters, final SurveyInterfaceManager interfaceManager, UserData userData) {
        super(locale, surveyParameters, interfaceManager, userData);
    }

    @Override
    protected Rules getRules(PortionSizeScriptManager scriptManager, CompoundFoodTemplateManager templateManager, RecipeManager recipeManager) {
        Rules baseRules = defaultRules(scriptManager, templateManager, recipeManager);
        Rules ndnsRules = super.getRules(scriptManager, templateManager, recipeManager);

        return new Rules(
                baseRules.mealPromptRules
                        .plus(AskAboutEatLocation.withPriority(9)),
                ndnsRules.foodPromptRules,
                ndnsRules.extendedFoodPromptRules,
                ndnsRules.surveyPromptRules,
                ndnsRules.selectionRules
        );
    }
}
