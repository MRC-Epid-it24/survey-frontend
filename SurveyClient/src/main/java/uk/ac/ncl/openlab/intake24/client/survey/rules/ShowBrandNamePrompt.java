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

package uk.ac.ncl.openlab.intake24.client.survey.rules;

import org.pcollections.PSet;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.BrandNamePrompt;

public class ShowBrandNamePrompt implements PromptRule<FoodEntry, FoodOperation> {
	@Override
	public Option<Prompt<FoodEntry, FoodOperation>> apply(FoodEntry data, SelectionMode selectionType, final PSet<String> surveyFlags) {
		return data.accept(new FoodEntry.Visitor<Option<Prompt<FoodEntry, FoodOperation>>>() {
			@Override
			public Option<Prompt<FoodEntry, FoodOperation>> visitRaw(RawFood food) {
				return Option.none();
			}

			@Override
			public Option<Prompt<FoodEntry, FoodOperation>> visitEncoded(EncodedFood food) {
				if (surveyFlags.contains(Survey.FLAG_FREE_ENTRY_COMPLETE) && food.brand.isEmpty() && !food.data.brands.isEmpty())
					return Option.<Prompt<FoodEntry, FoodOperation>>some(new BrandNamePrompt(food.data.localDescription, food.data.brands));
				else
					return Option.none();
			}

			@Override
			public Option<Prompt<FoodEntry, FoodOperation>> visitTemplate(TemplateFood food) {
				return Option.none();
			}

			@Override
			public Option<Prompt<FoodEntry, FoodOperation>> visitMissing(MissingFood food) {
				return Option.none();
			}

			@Override
			public Option<Prompt<FoodEntry, FoodOperation>> visitCompound(
					CompoundFood food) {
				return Option.none();
			}
		});
	}

	@Override
	public String toString() {
		return "Brand name prompt";
	}

	public static WithPriority<PromptRule<FoodEntry, FoodOperation>> withPriority(int priority) {
		return new WithPriority<PromptRule<FoodEntry, FoodOperation>>(new ShowBrandNamePrompt(), priority);
	}
}