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


import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.pcollections.PMap;
import org.workcraft.gwt.imagemap.shared.ImageMap;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.api.foods.AsServedSet;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodData;
import uk.ac.ncl.openlab.intake24.client.survey.PromptUtil;
import uk.ac.ncl.openlab.intake24.client.survey.SimplePrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.simple.WeightFactorSettings;

import java.util.Arrays;
import java.util.List;

import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withBackLink;
import static uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptUtil.*;

public class CerealPortionSizeScript implements PortionSizeScript {
    public static final String name = "cereal";
    public static final List<String> bowlCodes = Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F"});

    public final ImageMap bowlImageDef;
    public final PMap<String, AsServedSet> asServedDefs;

    private final PromptMessages messages = GWT.create(PromptMessages.class);

    public CerealPortionSizeScript(ImageMap bowlImageMap, PMap<String, AsServedSet> asServedDefs) {
        this.bowlImageDef = bowlImageMap;
        this.asServedDefs = asServedDefs;
    }

    @Override
    public Option<SimplePrompt<UpdateFunc>> nextPrompt(PMap<String, String> data, FoodData foodData) {
        if (!data.containsKey("bowl")) {
            return Option.some(PromptUtil.map(
                    withBackLink(
                            guidePrompt(
                                    SafeHtmlUtils.fromSafeConstant(messages.cereal_bowlPromptText()),
                                    bowlImageDef, "bowlIndex", "imageUrl")),
                    new Function1<UpdateFunc, UpdateFunc>() {
                        @Override
                        public UpdateFunc apply(final UpdateFunc f) {
                            return new UpdateFunc() {
                                @Override
                                public PMap<String, String> apply(PMap<String, String> argument) {
                                    PMap<String, String> a = f.apply(argument);
                                    return a.plus("bowl", bowlCodes.get(Integer.parseInt(a.get("bowlIndex")) - 1));
                                }
                            };
                        }
                    }));
        }
        if (!data.containsKey("servingWeight")) {
            String asServedSetId = "cereal_" + data.get("type") + data.get("bowl");

            SimplePrompt<UpdateFunc> portionSizePrompt =
                    withBackLink(
                            asServedPrompt(asServedDefs.get(asServedSetId), messages.asServed_servedLessButtonLabel(), messages.asServed_servedMoreButtonLabel(),
                                    messages.asServed_servedContinueButtonLabel(), "servingChoiceIndex", "servingImage", "servingWeight",
                                    Option.some(new WeightFactorSettings("servingWeightFactor", true, true)), false,
                                    defaultServingSizePrompt(foodData.description()))
                    );
            return Option.some(portionSizePrompt);
        } else if (!data.containsKey("leftoversWeight")) {
            if (!data.containsKey("leftovers"))
                return Option.some(withBackLink(
                        yesNoPromptZeroField(SafeHtmlUtils.fromSafeConstant(messages.asServed_leftoversQuestionPromptText(SafeHtmlUtils.htmlEscape(foodData.description().toLowerCase()))), messages.yesNoQuestion_defaultYesLabel(), messages.yesNoQuestion_defaultNoLabel(), "leftovers", "leftoversWeight")));
            else {
                String leftoversSetId = "cereal_" + data.get("type") + data.get("bowl") + "_leftovers";

                return Option.some(withBackLink(asServedPrompt(asServedDefs.get(leftoversSetId),
                        messages.asServed_leftLessButtonLabel(), messages.asServed_leftMoreButtonLabel(), messages.asServed_leftContinueButtonLabel(),
                        "leftoversChoiceIndex", "leftoversImage", "leftoversWeight",
                        Option.some(new WeightFactorSettings("leftoversWeightFactor", true, true)), true,
                        defaultLeftoversPrompt(foodData.description()))));
            }
        } else
            return done();
    }
}
