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

import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.workcraft.gwt.shared.client.*;
import uk.ac.ncl.openlab.intake24.client.GoogleAnalytics;
import uk.ac.ncl.openlab.intake24.client.IEHack;
import uk.ac.ncl.openlab.intake24.client.LoadingPanel;
import uk.ac.ncl.openlab.intake24.client.api.foods.*;
import uk.ac.ncl.openlab.intake24.client.api.uxevents.*;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.HelpMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;

import java.util.ArrayList;

public class FoodLookupPrompt implements Prompt<Pair<FoodEntry, Meal>, MealOperation> {
    private final static int MAX_RESULTS = 30;
    private final static PromptMessages messages = PromptMessages.Util.getInstance();
    private final static HelpMessages helpMessages = HelpMessages.Util.getInstance();

    private final String algorithmId;
    private final int matchScoreWeight;
    private final Boolean categoryBrowseRanked;
    private final FoodEntry food;
    private final Meal meal;
    private final RecipeManager recipeManager;

    private final String locale;

    private static PortionSizeMethod cachedWeightPotionSizeMethod = null;

    // This has to go through the API server because it needs to return the correct image URL
    // for the portion size selection screen even though it does not have any parameters
    public static void getWeightPortionSizeMethod(final Callback1<PortionSizeMethod> onComplete) {
        if (cachedWeightPotionSizeMethod != null)
            onComplete.call(cachedWeightPotionSizeMethod);
        else
            FoodDataService.INSTANCE.getWeightPortionSizeMethod(new MethodCallback<PortionSizeMethod>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    throw new RuntimeException("Failed to get the weight portion size method", exception);
                }

                @Override
                public void onSuccess(Method method, PortionSizeMethod response) {
                    cachedWeightPotionSizeMethod = response;
                    onComplete.call(cachedWeightPotionSizeMethod);
                }
            });
    }

    public FoodLookupPrompt(final String locale, final String algorithmId, int matchScoreWeight,
                            final Boolean categoryBrowseRanked, final FoodEntry food, final Meal meal, RecipeManager recipeManager) {
        this.locale = locale;
        this.algorithmId = algorithmId;
        this.matchScoreWeight = matchScoreWeight;
        this.categoryBrowseRanked = categoryBrowseRanked;
        this.food = food;
        this.meal = meal;
        this.recipeManager = recipeManager;
    }

    private class LookupInterface extends SurveyStageInterface.Aligned {
        private final TextBox searchText;
        private final Button searchButton;
        private final RecipeBrowser recipeBrowser;
        private final FoodBrowser foodBrowser;

        private String lastSearchTerm = "";

        private void showLoading(String description) {
            content.clear();
            content.add(new LoadingPanel(messages.foodLookup_loadingMessage(SafeHtmlUtils.htmlEscape(description))));
        }

        private void lookup(final String description) {
            showLoading(description);

            lastSearchTerm = description;

            final SafeHtml headerText = SafeHtmlUtils
                    .fromSafeConstant(messages.foodLookup_searchResultsHeader(SafeHtmlUtils.htmlEscape(description)));

            MethodCallback<LookupResult> lookupCallback = new MethodCallback<LookupResult>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    showWithSearchHeader(headerText, WidgetFactory.createDefaultErrorMessage());
                }

                @Override
                public void onSuccess(Method method, LookupResult result) {
                    recipeBrowser.lookup(description);
                    foodBrowser.showLookupResult(result, messages.foodLookup_resultsDataSetName());

                    FlowPanel div = new FlowPanel();

                    div.add(recipeBrowser);
                    div.add(foodBrowser);

                    showWithSearchHeader(headerText, div);

                    ArrayList<String> existing = new ArrayList<>();

                    for (FoodEntry fe : meal.foods)
                        if (fe.isEncoded())
                            existing.add(fe.asEncoded().data.code);

                    UxEventsHelper.postSearchResultsReceived(new SearchResult(description, existing, algorithmId, result));
                }
            };

            ArrayList<String> existingFoods = this.getExistingFoods();

            if (food.flags.contains(RawFood.FLAG_RECIPE_INGREDIENT))
                FoodLookupService.INSTANCE.lookupForRecipes(locale, algorithmId, matchScoreWeight, description, existingFoods, MAX_RESULTS, lookupCallback);
            else if (food.flags.contains(RawFood.FLAG_FOOD_SUPPLEMENT)) {
                FoodLookupService.INSTANCE.lookupInCategory(locale, algorithmId, matchScoreWeight, description, SpecialData.CATEGORY_FOOD_SUPPLEMENTS,
                        existingFoods, MAX_RESULTS, lookupCallback);
            } else {
                FoodLookupService.INSTANCE.lookup(locale, algorithmId, matchScoreWeight, description, existingFoods, MAX_RESULTS, lookupCallback);
            }
        }

        private void browse(final String categoryCode, final String categoryName) {
            content.clear();

            final FlowPanel promptPanel = WidgetFactory.createPromptPanel(SafeHtmlUtils.fromSafeConstant(messages.foodLookup_browseHeader(SafeHtmlUtils.htmlEscape(categoryName), SafeHtmlUtils.htmlEscape(meal.name.toLowerCase()))));

            content.add(promptPanel);

            content.add(foodBrowser);

            foodBrowser.browse(categoryCode, SafeHtmlUtils.htmlEscape(categoryName));
        }

        private void showWithSearchHeader(SafeHtml headerText, Widget stuff) {
            final FlowPanel searchPanel = new FlowPanel();

            searchPanel.addStyleName("intake24-food-lookup-search-panel");

            final FlowPanel textboxContainer = new FlowPanel();
            textboxContainer.addStyleName("intake24-food-lookup-textbox-container");
            textboxContainer.add(searchText);

            searchPanel.add(searchButton);
            searchPanel.add(textboxContainer);

            final FlowPanel promptPanel = WidgetFactory.createPromptPanel(headerText, WidgetFactory.createHelpButton(new ClickHandler() {
                @Override
                public void onClick(ClickEvent arg0) {
                    String promptType = FoodLookupPrompt.class.getSimpleName();
                    GoogleAnalytics.trackHelpButtonClicked(promptType);
                    ShepherdTour.startTour(buildShepherdTour(), promptType);
                }
            }));

            ShepherdTour.makeShepherdTarget(promptPanel);

            content.clear();
            content.add(promptPanel);
            content.add(searchPanel);
            content.add(stuff);

            IEHack.forceReflowDeferred();
        }

        LookupInterface(final Meal meal, final FoodEntry food, final Callback1<MealOperation> onComplete) {
            super(new FlowPanel(), HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP, SurveyStageInterface.DEFAULT_OPTIONS, FoodLookupPrompt.class.getSimpleName());

            searchText = new TextBox();
            searchText.setText(food.description());
            searchText.addStyleName("intake24-food-lookup-textbox");

            searchText.getElement().setId("intake24-food-lookup-textbox");
            ShepherdTour.makeShepherdTarget(searchText);

            searchText.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                        lookup(searchText.getText());

                        UxEventsHelper.postSearchButtonClicked(
                                new SearchButtonData(
                                        searchText.getText(),
                                        Viewport.getCurrent(),
                                        ContainerPosition.fromElement("intake24-food-browser-foods-container"),
                                        ContainerPosition.fromElement("intake24-food-browser-categories-container"),
                                        ContainerPosition.fromElement("intake24-food-browser-buttons-container")));
                    }
                }
            });

            searchButton = WidgetFactory.createButton(messages.foodLookup_searchButtonLabel(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent arg0) {
                    lookup(searchText.getText());

                    UxEventsHelper.postSearchButtonClicked(
                            new SearchButtonData(
                                    searchText.getText(),
                                    Viewport.getCurrent(),
                                    ContainerPosition.fromElement("intake24-food-browser-foods-container"),
                                    ContainerPosition.fromElement("intake24-food-browser-categories-container"),
                                    ContainerPosition.fromElement("intake24-food-browser-buttons-container")));
                }
            });

            searchButton.addStyleName("intake24-food-lookup-search-button");

            searchButton.getElement().setId("intake24-food-lookup-search-button");
            ShepherdTour.makeShepherdTarget(searchButton);

            Option<Pair<String, String>> limitToCategory;

            if (food.flags.contains(RawFood.FLAG_RECIPE_INGREDIENT))
                limitToCategory = Option.some(Pair.create(SpecialData.CATEGORY_CODE_RECIPE_INGREDIENTS, "All recipe ingredients"));
            else
                limitToCategory = Option.none();

            ArrayList<String> existingFoods = this.getExistingFoods();

            foodBrowser = new FoodBrowser(locale, new Callback2<FoodData, Integer>() {
                @Override
                public void call(final FoodData foodData, Integer index) {
                    food.link.linkedTo.accept(new Option.SideEffectVisitor<UUID>() {
                        @Override
                        public void visitSome(UUID id) {
                            FoodEntry parentFood = meal.foods.get(meal.foodIndex(id));

                            if (parentFood.isCompound())
                                getWeightPortionSizeMethod(new Callback1<PortionSizeMethod>() {
                                    @Override
                                    public void call(PortionSizeMethod psm) {
                                        onComplete.call(MealOperation.replaceFood(meal.foodIndex(food), new EncodedFood(foodData.withRecipePortionSizeMethods(psm), food.link, lastSearchTerm)));
                                    }
                                });
                            else {
                                onComplete.call(MealOperation.replaceFood(meal.foodIndex(food), new EncodedFood(foodData, food.link, lastSearchTerm)));
                            }
                        }

                        @Override
                        public void visitNone() {
                            onComplete.call(MealOperation.replaceFood(meal.foodIndex(food), new EncodedFood(foodData, food.link, lastSearchTerm)));
                        }
                    });

                    UxEventsHelper.postSearchResultSelected(
                            new SearchResultSelectionData(
                                    Viewport.getCurrent(),
                                    ContainerPosition.fromElement("intake24-food-browser-foods-container"),
                                    ContainerPosition.fromElement("intake24-food-browser-categories-container"),
                                    ContainerPosition.fromElement("intake24-food-browser-buttons-container").getOrDie(),
                                    Option.some(new FoodHeader(foodData.code, foodData.localDescription)),
                                    Option.none(),
                                    index));

                }
            }, new Callback1<String>() {
                @Override
                public void call(String code) {
                    if (code.equals(SpecialData.FOOD_CODE_SANDWICH))
                        onComplete.call(MealOperation.replaceFood(meal.foodIndex(food),
                                new TemplateFood(FoodLink.newUnlinked(), SafeHtmlUtils.htmlEscape(messages.foodBrowser_sandwichShortName()), false, FoodTemplates.sandwich)));
                    else if (code.equals(SpecialData.FOOD_CODE_SALAD))
                        onComplete.call(MealOperation.replaceFood(meal.foodIndex(food),
                                new TemplateFood(FoodLink.newUnlinked(), SafeHtmlUtils.htmlEscape(messages.foodBrowser_saladShortName()), false, FoodTemplates.salad)));

                }
            }, new Callback() {
                @Override
                public void call() {
                    MissingFood missingFood = new MissingFood(food.link, food.description(), food.isDrink(), Option.<MissingFoodDescription>none());
                    onComplete.call(MealOperation.replaceFood(meal.foodIndex(food),
                            food.link.linkedTo.isEmpty() ? missingFood : missingFood.withFlag(MissingFood.NOT_HOME_RECIPE_FLAG)));
                }

            }, Option.<SkipFoodHandler>none(), true, limitToCategory, categoryBrowseRanked ? Option.some(algorithmId) : Option.none(), Option.some(existingFoods));

            recipeBrowser = new RecipeBrowser(new Callback1<Recipe>() {
                @Override
                public void call(final Recipe recipe) {
                    onComplete.call(MealOperation.update(new Function1<Meal, Meal>() {
                        @Override
                        public Meal apply(Meal meal) {

                            Pair<TemplateFood, PVector<FoodEntry>> cloned = TemplateFood.clone(Pair.create(recipe.mainFood, recipe.ingredients));

                            Meal result = meal.updateFood(meal.foodIndex(food), cloned.left);

                            for (FoodEntry e : cloned.right)
                                result = result.plusFood(e);

                            return result;
                        }
                    }));
                }
            }, recipeManager);
        }

        private PVector<ShepherdTour.Step> buildShepherdTour() {
            TreePVector<ShepherdTour.Step> result = TreePVector.empty();

            result = result.plus(
                    new ShepherdTour.Step("searchText", "#intake24-food-lookup-textbox", helpMessages.foodLookup_textboxTitle(), helpMessages
                            .foodLookup_textboxDescription())).plus(
                    new ShepherdTour.Step("searchButton", "#intake24-food-lookup-search-button", helpMessages.foodLookup_searchButtonTitle(),
                            helpMessages.foodLookup_searchButtonDescription(), "top right", "bottom right"));

            result = result.plusAll(recipeBrowser.getShepherdTourSteps()).plusAll(foodBrowser.getShepherdTourSteps());

            return result;
        }

        private ArrayList<String> getExistingFoods() {
            ArrayList<String> existing = new ArrayList<>();

            for (FoodEntry fe : meal.foods)
                if (fe.isEncoded())
                    existing.add(fe.asEncoded().data.code);
            return existing;
        }
    }

    @Override
    public SurveyStageInterface getInterface(final Callback1<MealOperation> onComplete,
                                             Callback1<Function1<Pair<FoodEntry, Meal>, Pair<FoodEntry, Meal>>> updateIntermediateState) {
        LookupInterface ui = new LookupInterface(meal, food, onComplete);

        String browseCategory = food.customData.get(RawFood.KEY_BROWSE_CATEGORY_INSTEAD_OF_LOOKUP);

        if (browseCategory != null) {
            ui.browse(browseCategory, SafeHtmlUtils.htmlEscape(food.description()));
        } else
            ui.lookup(food.description());

        return ui;
    }

    @Override
    public String toString() {
        return "Food lookup";
    }
}
