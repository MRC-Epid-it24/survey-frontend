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


import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.workcraft.gwt.shared.client.*;
import uk.ac.ncl.openlab.intake24.client.LogRecorder;
import uk.ac.ncl.openlab.intake24.client.PredefinedMeals;
import uk.ac.ncl.openlab.intake24.client.ProcessMilkInHotDrinks;
import uk.ac.ncl.openlab.intake24.client.api.auth.AuthCache;
import uk.ac.ncl.openlab.intake24.client.api.survey.SurveyParameters;
import uk.ac.ncl.openlab.intake24.client.api.survey.UserData;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.survey.rules.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Basic implementation of a survey scheme. Uses the default prompt rules (see
 * <b>defaultRules</b>), and starting meals (see <b>startingSurveyData</b>).
 */
public abstract class BasicScheme implements SurveyScheme {
    final static double MAX_AGE_HOURS = 12.0;

    final protected SurveyInterfaceManager interfaceManager;
    private StateManager stateManager;
    final protected PromptManager defaultPromptManager;
    final protected SelectionManager defaultSelectionManager;
    final protected PortionSizeScriptManager defaultScriptManager;
    final protected CompoundFoodTemplateManager defaultTemplateManager;
    final protected RecipeManager recipeManager;
    final protected LogRecorder log;
    final protected String locale;
    final protected SurveyParameters surveyParameters;
    final protected UserData userData;

    final protected TreePVector<Function1<Survey, Survey>> basicPostProcess = TreePVector
            .<Function1<Survey, Survey>>empty()
            .plus(new ProcessMilkInHotDrinks());

    protected static Logger logger = Logger.getLogger("BasicScheme");

    protected IntakeSurvey cachedSurveyPage = null;

    protected Survey startingSurveyData() {
        return new Survey(PredefinedMeals.getStartingMealsForCurrentLocale(), new Selection.EmptySelection(SelectionMode.AUTO_SELECTION),
                System.currentTimeMillis(), System.currentTimeMillis(), HashTreePSet.<String>empty(), HashTreePMap.<String, String>empty());
    }

    protected Survey postProcess(Survey data, PVector<Function1<Survey, Survey>> functions) {
        Survey postProcessed = data;

        for (Function1<Survey, Survey> f : functions)
            postProcessed = f.apply(postProcessed);

        return postProcessed;
    }

    final protected Rules defaultRules(PortionSizeScriptManager scriptManager, CompoundFoodTemplateManager templateManager,
                                       RecipeManager recipeManager) {
        return new Rules(
                // meal associatedFoods
                TreePVector.<WithPriority<PromptRule<Meal, MealOperation>>>empty()
                        .plus(AskForMealTime.withPriority(40))
                        .plus(ShowEditMeal.withPriority(30))
                        .plus(ShowDrinkReminderPrompt.withPriority(20))
                        .plus(ShowReadyMealsPrompt.withPriority(10)),

                // food associatedFoods
                TreePVector.<WithPriority<PromptRule<FoodEntry, FoodOperation>>>empty()
                        .plus(ShowBrandNamePrompt.withPriority(-1))
                        .plus(ShowNextPortionSizeStep.withPriority(scriptManager, 0))
                        .plus(ChoosePortionSizeMethod.withPriority(1))
                        .plus(AskForMissingFoodDescription.withPriority(2))
                        .plus(SplitFood.withPriority(4))
                        .plus(InformFoodComplete.withPriority(-100)),

                // extended food propmts
                TreePVector.<WithPriority<PromptRule<Pair<FoodEntry, Meal>, MealOperation>>>empty()
                        .plus(ShowEditIngredientsPrompt.withPriority(3))
                        .plus(AskToLookupFood.withPriority(3, locale, surveyParameters.searchSortingAlgorithm, surveyParameters.searchMatchScoreWeight, false, recipeManager))
                        .plus(ShowSameAsBeforePrompt.withPriority(3, getSchemeId(), getDataVersion(), scriptManager, templateManager, getMilkPercentageOptions()))
                        .plus(ShowHomeRecipeServingsPrompt.withPriority(2))
                        .plus(ShowTemplateRecipeSavePrompt.withPriority(1, recipeManager))
                        .plus(ShowCompoundFoodPrompt.withPriority(0, locale))
                        .plus(ShowAssociatedFoodPrompt.withPriority(0, locale, Option.none()))
                        .plus(ShowBreadLinkedFoodAmountPrompt.withPriority(0))

                ,
                // global associatedFoods

                TreePVector.<WithPriority<PromptRule<Survey, SurveyOperation>>>empty()
                        .plus(ConfirmCompletion.withPriority(0))
                        .plus(ShowEnergyValidationPrompt.withPriority(40, 0.0))
                        .plus(ShowTimeGapPrompt.withPriority(50, 180, new Time(9, 0), new Time(21, 0)))
                        .plus(ShowEmptySurveyPrompt.withPriority(100))

                ,

                // selection rules
                TreePVector.<WithPriority<SelectionRule>>empty()
                        .plus(SelectForPortionSize.withPriority(3))
                        .plus(SelectRawFood.withPriority(2))
                        .plus(SelectFoodForAssociatedPrompts.withPriority(1))
                        .plus(SelectIncompleteFreeEntryMeal.withPriority(1))
                        .plus(SelectMealWithNoDrink.withPriority(1))
                        .plus(SelectUnconfirmedMeal.withPriority(1))
                        .plus(SelectMealForReadyMeals.withPriority(1)));
    }

    public BasicScheme(String locale, SurveyParameters surveyParameters, final SurveyInterfaceManager interfaceManager, UserData userData) {
        this.surveyParameters = surveyParameters;
        this.userData = userData;
        this.log = new LogRecorder();
        this.interfaceManager = interfaceManager;
        this.locale = locale;

        interfaceManager.setCallbacks(new Callback1<Survey>() {
            @Override
            public void call(Survey updatedState) {
                if (updatedState.flags.contains(Survey.FLAG_SKIP_HISTORY))
                    stateManager.updateState(updatedState.clearFlag(Survey.FLAG_SKIP_HISTORY), false);
                else
                    stateManager.updateState(updatedState, true);
                showNextPage();
            }
        }, new Callback2<Survey, Boolean>() {
            @Override
            public void call(Survey updatedState, Boolean makeHistoryEntry) {
                stateManager.updateState(updatedState, makeHistoryEntry);
            }
        });

        defaultScriptManager = new PortionSizeScriptManager(getPortionSizeScriptConstructors());

        defaultTemplateManager = new CompoundFoodTemplateManager(HashTreePMap.<String, TemplateFoodData>empty()
                .plus("sandwich", FoodTemplates.sandwich)
                .plus("salad", FoodTemplates.salad));

        recipeManager = new RecipeManager(getSchemeId(), getDataVersion(), defaultScriptManager, defaultTemplateManager);

        stateManager = new StateManager(getSchemeId(), getDataVersion(),
                surveyParameters.storeUserSessionOnServer, new Callback() {
            @Override
            public void call() {
                showNextPage();
            }
        }, defaultScriptManager);

        final Rules rules = getRules(defaultScriptManager, defaultTemplateManager, recipeManager);

        defaultPromptManager = new RuleBasedPromptManager(rules);

        // final SelectionManager selectionManager = new
        // RuleBasedSelectionManager(rules.selectionRules);

        defaultSelectionManager = new PromptAvailabilityBasedSelectionManager(defaultPromptManager);

        if (surveyParameters.storeUserSessionOnServer) {
            StateManagerUtil.getSavedStateFromServerOrStorage(AuthCache.getCurrentUserId(), getSchemeId(),
                    getDataVersion(), defaultScriptManager, defaultTemplateManager,
                    new MethodCallback<Option<Survey>>() {
                        @Override
                        public void onFailure(Method method, Throwable throwable) {

                        }

                        @Override
                        public void onSuccess(Method method, Option<Survey> surveyOption) {
                            setInitialState(surveyOption);
                        }
                    });
        } else {
            setInitialState(StateManagerUtil.getLatestState(AuthCache.getCurrentUserId(), getSchemeId(),
                    getDataVersion(), defaultScriptManager, defaultTemplateManager));
        }
    }

    protected PVector<StandardUnitDef> getMilkPercentageOptions() {
        return DefaultPortionSizeScripts.defaultMilkInHotDrinkPercentages;
    }

    protected Map<String, PortionSizeScriptConstructor> getPortionSizeScriptConstructors() {
        Map<String, PortionSizeScriptConstructor> defaultConstructors = DefaultPortionSizeScripts.getConstructors();

        defaultConstructors.put(
                MilkInHotDrinkPortionSizeScript.name,
                () -> new MilkInHotDrinkPortionSizeScriptLoader(getMilkPercentageOptions()));

        return defaultConstructors;
    }

    private void setInitialState(Option<Survey> surveyOpt) {
        Survey initialState = surveyOpt.accept(new Option.Visitor<Survey, Survey>() {
            @Override
            public Survey visitSome(Survey data) {
                if (Window.Location.getParameter("ignoreMaxAge") == null && getSurveyExpired(data)) {
                    logger.warning("Saved state is older than " + MAX_AGE_HOURS + " hours and has expired.");
                    return startingSurveyData();
                } else {
                    return data.clearCompletionConfirmed()
                            .clearEnergyValueConfirmed();
                }
            }

            @Override
            public Survey visitNone() {
                // log.info("No saved state, starting new survey.");
                return startingSurveyData();
            }
        });

        stateManager.updateState(initialState, false);

        showNextPage();
    }

    protected StateManager getStateManager() {
        return stateManager;
    }


    @Override
    public void showNextPage() {
        final Survey state = getStateManager().getCurrentState();
        // Logger log = Logger.getLogger("showNextPage");
        // log.info(SurveyXmlSerialiser.toXml(state));

        if (!state.flags.contains(WelcomePage.FLAG_WELCOME_PAGE_SHOWN)) {
            String welcomePageHtml = surveyParameters.description.getOrElse(SurveyMessages.INSTANCE.welcomePage_welcomeText());
            interfaceManager.show(new WelcomePage(welcomePageHtml, state));
        } else if (!state.completionConfirmed()) {
            if (cachedSurveyPage == null)
                cachedSurveyPage = new IntakeSurvey(getStateManager(), defaultPromptManager, defaultSelectionManager, defaultScriptManager);
            interfaceManager.show(cachedSurveyPage);
        } else {
            String finalPageHtml = this.surveyParameters.finalPageHtml.getOrElse(SurveyMessages.INSTANCE.finalPage_text());
            interfaceManager.show(new FlatFinalPage(finalPageHtml, postProcess(state, basicPostProcess), log.log));
        }
    }

    protected abstract Rules getRules(PortionSizeScriptManager scriptManager, CompoundFoodTemplateManager templateManager,
                                      RecipeManager recipeManager);

    @Override
    public abstract String getDataVersion();

    @Override
    public abstract String getSchemeId();

    @Override
    public Boolean getSurveyExpired(Survey survey) {
        Double age = (System.currentTimeMillis() - survey.startTime) / 3600000.0;
        logger.fine("Saved state is " + age + " hours old.");
        return age > MAX_AGE_HOURS;
    }

    @Override
    public List<Anchor> navBarLinks() {
        return Collections.emptyList();
    }

    @Override
    public List<Anchor> navBarUserInfo() {
        return Collections.emptyList();
    }
}
