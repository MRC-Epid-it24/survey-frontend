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

package uk.ac.ncl.openlab.intake24.client.survey.portionsize;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.pcollections.PMap;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.api.foods.AsServedSet;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodData;
import uk.ac.ncl.openlab.intake24.client.survey.SimplePrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.simple.WeightFactorSettings;

import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.setAdditionalField;
import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withBackLink;
import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withHeader;
import static uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptUtil.*;

public class AsServedScript implements PortionSizeScript {
    public static final String name = "as-served";

    public final AsServedSet servingImages;
    public final Option<AsServedSet> leftoversImages;

    public AsServedScript(AsServedSet servingImages, Option<AsServedSet> leftoversImages) {
        this.servingImages = servingImages;
        this.leftoversImages = leftoversImages;
    }

    public Option<SimplePrompt<UpdateFunc>> nextPrompt(PMap<String, String> data, FoodData foodData) {
        String escapedFoodDesc = SafeHtmlUtils.htmlEscape(foodData.description());
        boolean hasLeftoverImages = !leftoversImages.isEmpty();

		/*Logger log = Logger.getLogger("AsServedScript");

		log.info("Has leftovers: " + hasLeftoverImages);
		
		for (String k : data.keySet()) {
			log.info (k + " = " + data.get(k));
		}*/

        if (!data.containsKey("servingWeight")) {
            SimplePrompt<UpdateFunc> portionSizePrompt =
                    withBackLink(
                        withHeader(
                            asServedPrompt(servingImages, PromptMessages.INSTANCE.asServed_servedLessButtonLabel(), PromptMessages.INSTANCE.asServed_servedMoreButtonLabel(),
                                    PromptMessages.INSTANCE.asServed_servedContinueButtonLabel(), "servingChoiceIndex", "servingImage", "servingWeight",
                                    Option.some(new WeightFactorSettings("servingWeightFactor", true, true)), false,
                                    defaultServingSizePrompt(foodData.description())), escapedFoodDesc
                        )
                    );

            if (!hasLeftoverImages)
                return Option.some(setAdditionalField(portionSizePrompt, "leftoversWeight", "0"));
            else
                return Option.some(portionSizePrompt);
        } else if (!data.containsKey("leftoversWeight") && hasLeftoverImages) {
            if (!data.containsKey("leftovers"))
                return Option.some(
                    withBackLink(
                        withHeader(
                            yesNoPromptZeroField(SafeHtmlUtils.fromSafeConstant(PromptMessages.INSTANCE.asServed_leftoversQuestionPromptText(SafeHtmlUtils.htmlEscape(foodData.description().toLowerCase()))),
                                    PromptMessages.INSTANCE.yesNoQuestion_defaultYesLabel(), PromptMessages.INSTANCE.yesNoQuestion_defaultNoLabel(), "leftovers", "leftoversWeight"), escapedFoodDesc
                        )
                    )
                );
            else
                return Option.some(
                    withBackLink(
                        withHeader(
                            asServedPrompt(leftoversImages.getOrDie(),
                            PromptMessages.INSTANCE.asServed_leftLessButtonLabel(), PromptMessages.INSTANCE.asServed_leftMoreButtonLabel(), PromptMessages.INSTANCE.asServed_leftContinueButtonLabel(), "leftoversChoiceIndex",
                            "leftoversImage", "leftoversWeight", Option.some(new WeightFactorSettings("leftoversWeightFactor", true, true)), true,
                            defaultLeftoversPrompt(foodData.description())), escapedFoodDesc
                        )
                    )
                );
        } else
            return done();
    }
}
