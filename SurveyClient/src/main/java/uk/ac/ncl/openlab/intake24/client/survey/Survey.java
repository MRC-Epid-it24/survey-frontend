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

import static org.workcraft.gwt.shared.client.CollectionUtils.filter;
import static org.workcraft.gwt.shared.client.CollectionUtils.flatten;
import static org.workcraft.gwt.shared.client.CollectionUtils.flattenOption;
import static org.workcraft.gwt.shared.client.CollectionUtils.forall;
import static org.workcraft.gwt.shared.client.CollectionUtils.map;
import static org.workcraft.gwt.shared.client.CollectionUtils.sort;
import static org.workcraft.gwt.shared.client.CollectionUtils.zipWithIndex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.workcraft.gwt.shared.client.CollectionUtils.WithIndex;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.api.uxevents.UxEventsHelper;

public class Survey {
    public static final String FLAG_ENERGY_VALUE_CONFIRMED = "energy-value-confirmed";
    public static final String FLAG_COMPLETION_CONFIRMED = "completion-confirmed";
    public static final String FLAG_ENCODING_COMPLETE = "encoding-complete";
    public static final String FLAG_FREE_ENTRY_COMPLETE = "free-entry-complete";
    public static final String FLAG_NO_MORE_PROMPTS = "no-more-associatedFoods";
    public static final String FLAG_SKIP_HISTORY = "skip-history";

    public final long startTime;
    public final long lastSaved;
    public final PVector<Meal> meals;
    public final Selection selectedElement;
    public final PSet<String> flags;
    public final PMap<String, String> customData;
    public final PVector<WithIndex<Meal>> mealsSortedByTime;

    public Survey(List<Meal> meals, Selection selectedElement, long startTime, long lastSaved, Set<String> flags, Map<String, String> customData) {
        this(TreePVector.<Meal>from(meals), selectedElement, startTime, lastSaved, HashTreePSet.<String>from(flags), HashTreePMap
                .<String, String>from(customData));
    }

    public Survey(PVector<Meal> meals, Selection selectedElement, long startTime, long lastSaved, PSet<String> flags, PMap<String, String> customData) {
        this.meals = meals;
        this.startTime = startTime;
        this.lastSaved = lastSaved;
        this.customData = customData;

        PVector<WithIndex<Meal>> mealsWithIndex = zipWithIndex(meals);

        mealsSortedByTime = sort(mealsWithIndex, new Comparator<WithIndex<Meal>>() {
            @Override
            public int compare(WithIndex<Meal> arg0, WithIndex<Meal> arg1) {
                if (arg0.value.time.isEmpty()) {
                    if (arg1.value.time.isEmpty())
                        return 0;
                    else
                        return 1;
                } else {
                    if (arg1.value.time.isEmpty())
                        return -1;
                    else {
                        Time t0 = arg0.value.time.getOrDie();
                        Time t1 = arg1.value.time.getOrDie();

                        if (t0.hours != t1.hours)
                            return t0.hours - t1.hours;
                        else
                            return t0.minutes - t1.minutes;
                    }
                }
            }
        });

        this.selectedElement = selectedElement;

        PSet<String> f = flags;

        if (!meals.isEmpty() && forall(meals, Meal.isFreeEntryCompleteFunc))
            f = f.plus(FLAG_FREE_ENTRY_COMPLETE);
        if (!meals.isEmpty() && forall(meals, Meal.isEncodingCompleteFunc))
            f = f.plus(FLAG_ENCODING_COMPLETE);

        this.flags = f;
    }

    public boolean isPortionSizeComplete() {
        return forall(meals, Meal.isPortionSizeComplete);
    }

    public CompletedSurvey finalise(List<String> log) {
        PVector<CompletedMeal> completedMeals = map(meals, new Function1<Meal, CompletedMeal>() {
            @Override
            public CompletedMeal apply(Meal meal) {
                PVector<CompletedFood> completedFoods = map(filter(meal.foods, new Function1<FoodEntry, Boolean>() {
                    @Override
                    public Boolean apply(FoodEntry argument) {
                        return !argument.isTemplate() && !argument.isCompound() && !argument.isMissing();
                    }
                }), new Function1<FoodEntry, CompletedFood>() {
                    @Override
                    public CompletedFood apply(FoodEntry foodEntry) {
                        return foodEntry.finalise();
                    }
                });

                PVector<CompletedMissingFood> missingFoods = flattenOption(map(meal.foods, new Function1<FoodEntry, Option<CompletedMissingFood>>() {
                    @Override
                    public Option<CompletedMissingFood> apply(FoodEntry foodEntry) {
                        return foodEntry.accept(new FoodEntry.Visitor<Option<CompletedMissingFood>>() {
                            @Override
                            public Option<CompletedMissingFood> visitRaw(RawFood food) {
                                return Option.none();
                            }

                            @Override
                            public Option<CompletedMissingFood> visitEncoded(EncodedFood food) {
                                return Option.none();
                            }

                            @Override
                            public Option<CompletedMissingFood> visitTemplate(TemplateFood food) {
                                return Option.none();
                            }

                            @Override
                            public Option<CompletedMissingFood> visitMissing(MissingFood food) {

                                MissingFoodDescription desc = food.description
                                        .getOrDie("Cannot finalise survey because it contains a missing food entry with no description");

                                return Option.some(new CompletedMissingFood(food.name, desc.brand.getOrElse(""), desc.description.getOrElse(""),
                                        desc.portionSize.getOrElse(""), desc.leftovers.getOrElse("")));
                            }

                            @Override
                            public Option<CompletedMissingFood> visitCompound(CompoundFood food) {
                                return Option.none();
                            }
                        });
                    }
                }));

                return new CompletedMeal(meal.name, new ArrayList<CompletedFood>(completedFoods), new ArrayList<CompletedMissingFood>(missingFoods),
                        meal.time.getOrDie("Cannot finalise survey because it contains an undefined time entry"), new HashMap<String, String>(meal.customData));
            }
        });

        return new CompletedSurvey(startTime, System.currentTimeMillis(), UxEventsHelper.sessionId, new ArrayList<CompletedMeal>(completedMeals), new HashMap<String, String>(customData));
    }

    public Survey withSelection(Selection selectedElement) {
        return new Survey(meals, selectedElement, startTime, lastSaved, flags, customData);
    }

    public Survey plusMeal(Meal meal) {
        return new Survey(meals.plus(meal), selectedElement, startTime, lastSaved, flags, customData);
    }

    public Survey minusMeal(int mealIndex) {
        return new Survey(meals.minus(mealIndex), selectedElement, startTime, lastSaved, flags, customData);
    }

    public Survey updateMeal(int mealIndex, Meal value) {
        return new Survey(meals.with(mealIndex, value), selectedElement, startTime, lastSaved, flags, customData);
    }

    public Survey updateFood(int mealIndex, int foodIndex, FoodEntry value) {
        return updateMeal(mealIndex, meals.get(mealIndex).updateFood(foodIndex, value));
    }

    public Survey withMeals(PVector<Meal> newMeals) {
        return new Survey(newMeals, selectedElement, startTime, System.currentTimeMillis(), flags, customData);
    }

    public static Function1<Survey, Survey> addMealFunc(final Meal meal) {
        return new Function1<Survey, Survey>() {
            @Override
            public Survey apply(Survey argument) {
                return argument.plusMeal(meal);
            }
        };
    }

    public Survey invalidateSelection() {
        return this.withSelection(new Selection.EmptySelection(SelectionMode.AUTO_SELECTION));
    }

    public Survey withFlag(String flag) {
        return new Survey(meals, selectedElement, startTime, lastSaved, flags.plus(flag), customData);
    }

    public Survey clearFlag(String flag) {
        return new Survey(meals, selectedElement, startTime, lastSaved, flags.minus(flag), customData);
    }

    public Survey updateLastSaved() {
        return new Survey(meals, selectedElement, startTime, System.currentTimeMillis(), flags, customData);
    }

    public Survey markCompletionConfirmed() {
        return withFlag(FLAG_COMPLETION_CONFIRMED);
    }

    public Survey clearCompletionConfirmed() {
        return clearFlag(FLAG_COMPLETION_CONFIRMED);
    }

    public Survey markEnergyValueConfirmed() {
        return withFlag(FLAG_ENERGY_VALUE_CONFIRMED);
    }

    public Survey markFreeEntryComplete() {
        return withFlag(FLAG_FREE_ENTRY_COMPLETE);
    }

    public boolean freeEntryComplete() {
        return flags.contains(FLAG_FREE_ENTRY_COMPLETE);
    }

    public Survey clearEnergyValueConfirmed() {
        return clearFlag(FLAG_ENERGY_VALUE_CONFIRMED);
    }

    public boolean completionConfirmed() {
        return flags.contains(FLAG_COMPLETION_CONFIRMED);
    }

    public boolean energyValueConfirmed() {
        return flags.contains(FLAG_ENERGY_VALUE_CONFIRMED);
    }

    public Survey withData(PMap<String, String> newData) {
        return new Survey(meals, selectedElement, startTime, lastSaved, flags, newData);
    }

    public Survey withData(String key, String value) {
        return withData(customData.plus(key, value));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Survey other = (Survey) obj;
        if (customData == null) {
            if (other.customData != null)
                return false;
        } else if (!customData.equals(other.customData))
            return false;
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        if (meals == null) {
            if (other.meals != null)
                return false;
        } else if (!meals.equals(other.meals))
            return false;
        if (mealsSortedByTime == null) {
            if (other.mealsSortedByTime != null)
                return false;
        } else if (!mealsSortedByTime.equals(other.mealsSortedByTime))
            return false;
        if (selectedElement == null) {
            if (other.selectedElement != null)
                return false;
        } else if (!selectedElement.equals(other.selectedElement))
            return false;
        if (startTime != other.startTime)
            return false;
        return true;
    }
}
