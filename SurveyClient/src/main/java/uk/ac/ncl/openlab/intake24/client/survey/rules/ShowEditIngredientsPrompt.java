/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey.rules;

import org.pcollections.PSet;
import org.workcraft.gwt.shared.client.Option;
import org.workcraft.gwt.shared.client.Pair;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.EditRecipeIngredientsPrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;

public class ShowEditIngredientsPrompt implements PromptRule<Pair<FoodEntry, Meal>, MealOperation> {
	@Override
	public Option<Prompt<Pair<FoodEntry, Meal>, MealOperation>> apply(Pair<FoodEntry, Meal> data, SelectionMode selectionType, PSet<String> surveyFlags) {
		
		boolean markedAsComplete = data.left.flags.contains(CompoundFood.FLAG_INGREDIENTS_COMPLETE);
		
		// Make this prompt show up if all ingredients were deleted after completion
		boolean noLinkedFoods = Meal.linkedFoods(data.right.foods, data.left).isEmpty();
		
		if (data.left.isCompound() && (noLinkedFoods || !markedAsComplete)) {
			return new Option.Some<Prompt<Pair<FoodEntry, Meal>, MealOperation>>(new EditRecipeIngredientsPrompt(data.right, data.right.foodIndex(data.left)));
		} else {
			return new Option.None<Prompt<Pair<FoodEntry, Meal>, MealOperation>>();
		}
	}

	@Override
	public String toString() {
		return "Show edit recipe ingredients prompt";
	}

	public static WithPriority<PromptRule<Pair<FoodEntry, Meal>, MealOperation>> withPriority(int priority) {
		return new WithPriority<PromptRule<Pair<FoodEntry, Meal>, MealOperation>>(new ShowEditIngredientsPrompt(), priority);
	}
}