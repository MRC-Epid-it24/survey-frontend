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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.workcraft.gwt.shared.client.*;
import uk.ac.ncl.openlab.intake24.client.GoogleAnalytics;
import uk.ac.ncl.openlab.intake24.client.LoadingPanel;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodData;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodDataService;
import uk.ac.ncl.openlab.intake24.client.api.foods.PortionSizeMethod;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSize;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.HelpMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AssociatedFoodPrompt implements Prompt<Pair<FoodEntry, Meal>, MealOperation> {
    private final static PromptMessages messages = PromptMessages.Util.getInstance();
    private final static HelpMessages helpMessages = HelpMessages.Util.getInstance();

    private final Pair<FoodEntry, Meal> pair;
    private final int foodIndex;
    private final int promptIndex;
    private FlowPanel interf;
    private Panel buttonsPanel;

    private PVector<ShepherdTour.Step> tour;

    private FoodBrowser foodBrowser;
    private boolean isInBrowserMode = false;
    private final String locale;

    public AssociatedFoodPrompt(final String locale, final Pair<FoodEntry, Meal> pair, final int foodIndex, final int promptIndex) {
        this.locale = locale;
        this.pair = pair;
        this.foodIndex = foodIndex;
        this.promptIndex = promptIndex;
    }

    private Option<String> getParamValue(EncodedFood food, final String id) {
        return CollectionUtils.flattenOption(food.portionSize.map(new Function1<Either<PortionSize, CompletedPortionSize>, Option<String>>() {
            @Override
            public Option<String> apply(Either<PortionSize, CompletedPortionSize> argument) {
                return argument.accept(new Either.Visitor<PortionSize, CompletedPortionSize, Option<String>>() {
                    @Override
                    public Option<String> visitRight(CompletedPortionSize value) {
                        if (value.data.containsKey(id))
                            return Option.some(value.data.get(id));
                        else
                            return Option.none();
                    }

                    @Override
                    public Option<String> visitLeft(PortionSize value) {
                        if (value.data.containsKey(id))
                            return Option.some(value.data.get(id));
                        else
                            return Option.none();
                    }
                });
            }
        }));
    }

    private List<PortionSizeMethod> appendPotionSizeParameter(List<PortionSizeMethod> methods, String id, String value) {
        ArrayList<PortionSizeMethod> result = new ArrayList<PortionSizeMethod>();
        for (PortionSizeMethod m : methods) {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.putAll(m.parameters);
            parameters.put(id, value);
            result.add(new PortionSizeMethod(m.method, m.description, m.imageUrl, m.useForRecipes, parameters));
        }
        return result;
    }

    private Meal linkAssociatedFood(Meal meal, FoodEntry forFood, final FoodEntry assocFood, boolean linkAsMain) {
        if (linkAsMain) {
            final int forIndex = meal.foodIndex(forFood);
            final int assocIndex = meal.foodIndex(assocFood);

            Meal result = meal.updateFood(assocIndex, forFood).updateFood(forIndex, assocFood);
            final PVector<FoodEntry> foodsToRelink = Meal.linkedFoods(result.foods, forFood).plus(0, forFood);

            for (FoodEntry e : foodsToRelink) {
                int index = result.foodIndex(e);
                result = result.minusFood(index).plusFood(e.relink(FoodLink.newLinked(assocFood.link.id)));
            }

            return result;
        } else {
            final int index = meal.foodIndex(assocFood);
            return meal.minusFood(index).plusFood(assocFood.relink(FoodLink.newLinked(forFood.link.id)));
        }
    }

    @Override
    public SurveyStageInterface getInterface(final Callback1<MealOperation> onComplete,
                                             Callback1<Function1<Pair<FoodEntry, Meal>, Pair<FoodEntry, Meal>>> updateIntermediateState) {
        final EncodedFood food = (EncodedFood) pair.left;
        final AssociatedFood prompt = food.enabledPrompts.get(promptIndex);

        final FlowPanel content = new FlowPanel();
        PromptUtil.addBackLink(content);
        final Panel promptPanel = WidgetFactory.createPromptPanel(
                SafeHtmlUtils.fromSafeConstant("<p>" + SafeHtmlUtils.htmlEscape(prompt.promptText) + "</p>"),
                WidgetFactory.createHelpButton(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent arg0) {
                        String promptType = AssociatedFoodPrompt.class.getSimpleName();
                        GoogleAnalytics.trackHelpButtonClicked(promptType);
                        ShepherdTour.startTour(getShepherdTourSteps(), promptType);
                    }
                }));
        content.add(promptPanel);
        ShepherdTour.makeShepherdTarget(promptPanel);

        final Callback1<FoodData> addNewFood = new Callback1<FoodData>() {
            @Override
            public void call(final FoodData result) {
                onComplete.call(MealOperation.update(new Function1<Meal, Meal>() {
                    @Override
                    public Meal apply(final Meal meal) {
                        // Special case for cereal:
                        // if a "milk on cereal" food is linked to a cereal food
                        // copy bowl type from the parent food
                        Option<String> bowl_id = getParamValue(food, "bowl");

                        FoodData foodData = bowl_id.accept(new Option.Visitor<String, FoodData>() {
                            @Override
                            public FoodData visitSome(String bowl_id) {
                                return result.withPortionSizeMethods(appendPotionSizeParameter(result.portionSizeMethods, "bowl", bowl_id));
                            }

                            @Override
                            public FoodData visitNone() {
                                return result;
                            }
                        });

                        EncodedFood assocFood = new EncodedFood(foodData, FoodLink.newUnlinked(), "associated food prompt");

                        return linkAssociatedFood(meal.plusFood(assocFood), food, assocFood, prompt.linkAsMain);
                    }

                    ;
                }));
            }
        };

        final Callback addMissingFood = new Callback() {
            @Override
            public void call() {
                onComplete.call(MealOperation.update(new Function1<Meal, Meal>() {
                    @Override
                    public Meal apply(final Meal meal) {
                        FoodEntry missingFood = new MissingFood(FoodLink.newUnlinked(), prompt.genericName.substring(0, 1).toUpperCase()
                                + prompt.genericName.substring(1), false, Option.<MissingFoodDescription>none()).withCustomDataField(
                                MissingFood.KEY_ASSOC_FOOD_NAME, food.description()).withCustomDataField(MissingFood.KEY_ASSOC_FOOD_CATEGORY,
                                prompt.foodOrCategoryCode.getRightOrDie());

                        return linkAssociatedFood(meal.plusFood(missingFood), food, missingFood, prompt.linkAsMain);
                    }
                }));
            }
        };

        final Callback1<FoodEntry> addExistingFood = new Callback1<FoodEntry>() {
            @Override
            public void call(final FoodEntry existing) {
                onComplete.call(MealOperation.update(new Function1<Meal, Meal>() {
                    @Override
                    public Meal apply(final Meal meal) {
                        return linkAssociatedFood(meal, food, existing, prompt.linkAsMain);
                    }

                    ;
                }));
            }
        };

        foodBrowser = new FoodBrowser(locale, new Callback1<FoodData>() {
            @Override
            public void call(FoodData result) {
                addNewFood.call(result);
            }
        }, new Callback1<String>() {
            @Override
            public void call(String code) {
                throw new RuntimeException("Special foods are not allowed as associated foods");
            }
        }, new Callback() {
            @Override
            public void call() {
                addMissingFood.call();
            }
        }, Option.<SkipFoodHandler>none(), false, Option.<Pair<String, String>>none());

        Button no = WidgetFactory.createButton(messages.assocFoods_noButtonLabel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onComplete.call(MealOperation.updateEncodedFood(foodIndex, new Function1<EncodedFood, EncodedFood>() {
                    @Override
                    public EncodedFood apply(EncodedFood argument) {
                        return argument.minusPrompt(promptIndex);
                    }
                }));
            }
        });

        Button yes = WidgetFactory.createButton(messages.assocFoods_yesButtonLabel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (prompt.foodOrCategoryCode.isRight()) {
                    content.clear();
                    PromptUtil.addBackLink(content);
                    content.add(promptPanel);
                    content.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.assocFoods_specificFoodPrompt())));
                    content.add(interf);

                    content.add(foodBrowser);
                    isInBrowserMode = true;

                    foodBrowser.browse(prompt.foodOrCategoryCode.getRightOrDie(), messages.assocFoods_allFoodsDataSetName());
                } else {
                    content.clear();
                    content.add(new LoadingPanel(messages.foodBrowser_loadingMessage()));

                    FoodDataService.INSTANCE.getFoodData(locale, prompt.foodOrCategoryCode.getLeftOrDie(), new MethodCallback<FoodData>() {
                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            content.clear();
                            content.add(WidgetFactory.createDefaultErrorMessage());
                            content.add(WidgetFactory.createBackLink());
                        }

                        @Override
                        public void onSuccess(Method method, FoodData response) {
                            addNewFood.call(response);
                        }
                    });
                }
            }
        });

        yes.getElement().setId("intake24-assoc-food-yes-button");

        final int existingIndex = CollectionUtils.indexOf(pair.right.foods, new Function1<FoodEntry, Boolean>() {
            @Override
            public Boolean apply(FoodEntry argument) {
                return argument.accept(new FoodEntry.Visitor<Boolean>() {
                    @Override
                    public Boolean visitRaw(RawFood food) {
                        return false;
                    }

                    @Override
                    public Boolean visitEncoded(EncodedFood food) {
                        // don't suggest foods that are already linked to other
                        // foods
                        if (food.link.isLinked())
                            return false;
                            // don't suggest linking the food to itself
                        else if (food.link.id.equals(pair.left.link.id))
                            return false;
                            // don't suggest if the food has foods linked to it
                        else if (!Meal.linkedFoods(pair.right.foods, food).isEmpty())
                            return false;
                        else if (prompt.foodOrCategoryCode.isRight())
                            return food.isInCategory(prompt.foodOrCategoryCode.getRightOrDie());
                        else
                            return food.data.code.equals(prompt.foodOrCategoryCode.getLeftOrDie());
                    }

                    @Override
                    public Boolean visitTemplate(TemplateFood food) {
                        return false;
                    }

                    @Override
                    public Boolean visitMissing(MissingFood food) {
                        return false;
                    }

                    @Override
                    public Boolean visitCompound(CompoundFood food) {
                        return false;
                    }
                });
            }
        });

        no.getElement().setId("intake24-assoc-food-no-button");

        tour = TreePVector
                .<ShepherdTour.Step>empty()
                .plus(new ShepherdTour.Step("noButton", "#intake24-assoc-food-no-button", helpMessages.assocFood_noButtonTitle(), helpMessages
                        .assocFood_noButtonDescription()))
                .plus(new ShepherdTour.Step("yesButton", "#intake24-assoc-food-yes-button", helpMessages.assocFood_yesButtonTitle(), helpMessages
                        .assocFood_yesButtonDescription()));

        if (existingIndex != -1) {
            Button yesExisting = WidgetFactory.createButton(messages.assocFoods_alreadyEntered(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addExistingFood.call(pair.right.foods.get(existingIndex));
                }
            });

            yesExisting.getElement().setId("intake24-assoc-food-yes-existing-button");

            tour = tour.plus(new ShepherdTour.Step("yesButton", "#intake24-assoc-food-yes-existing-button", helpMessages
                    .assocFood_yesExistingButtonTitle(), helpMessages.assocFood_yesExistingButtonDescription(), "top right", "bottom right"));

            ShepherdTour.makeShepherdTarget(yesExisting);

            buttonsPanel = WidgetFactory.createButtonsPanel(no, yes, yesExisting);
        } else {
            buttonsPanel = WidgetFactory.createButtonsPanel(no, yes);
        }

        content.add(buttonsPanel);

        ShepherdTour.makeShepherdTarget(yes, no);

        interf = new FlowPanel();

        return new SurveyStageInterface.Aligned(content, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP,
                SurveyStageInterface.DEFAULT_OPTIONS, AssociatedFoodPrompt.class.getSimpleName());
    }

    public PVector<ShepherdTour.Step> getShepherdTourSteps() {
        if (isInBrowserMode)
            return foodBrowser.getShepherdTourSteps();
        else
            return tour;
    }

    @Override
    public String toString() {
        return "Food reminder prompt";
    }
}