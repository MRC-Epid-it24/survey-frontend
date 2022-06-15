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
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.api.foods.GuideImage;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodData;
import uk.ac.ncl.openlab.intake24.client.survey.PromptUtil;
import uk.ac.ncl.openlab.intake24.client.survey.SimplePrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;

import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withBackLink;
import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withHeader;
import static uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptUtil.*;

public class GuideScript implements PortionSizeScript {
    public static final String name = "guide-image";

    public final GuideImage guideDef;

    private final PromptMessages messages = GWT.create(PromptMessages.class);

    public GuideScript(GuideImage guideDef) {
        this.guideDef = guideDef;
    }

    @Override
    public Option<SimplePrompt<UpdateFunc>> nextPrompt(PMap<String, String> data, FoodData foodData) {
        String escapedFoodDesc = SafeHtmlUtils.htmlEscape(foodData.description());

        if (!data.containsKey("objectWeight")) {
            return Option.some(PromptUtil.map(
                    withBackLink(
                        withHeader(
                            guidePrompt(SafeHtmlUtils.fromSafeConstant(messages.guide_choicePromptText(escapedFoodDesc.toLowerCase())), guideDef.imageMap.toImageMap(), "objectIndex", "imageUrl"), escapedFoodDesc
                        )
                    ),
                    new Function1<UpdateFunc, UpdateFunc>() {
                        @Override
                        public UpdateFunc apply(final UpdateFunc f) {
                            return new UpdateFunc() {
                                @Override
                                public PMap<String, String> apply(PMap<String, String> argument) {
                                    PMap<String, String> a = f.apply(argument);
                                    return a.plus("objectWeight", Double.toString(guideDef.weights.get(Integer.parseInt(a.get("objectIndex")))));
                                }
                            };
                        }
                    }));

        } else if (!data.containsKey("quantity")) {

            return Option.some(
                withBackLink(
                    withHeader(PromptUtil.map(quantityPrompt(SafeHtmlUtils.fromSafeConstant(messages.guide_quantityPromptText(escapedFoodDesc.toLowerCase())),
                                messages.guide_quantityContinueButtonLabel(), "quantity"), new Function1<UpdateFunc, UpdateFunc>() {
                            @Override
                            public UpdateFunc apply(final UpdateFunc f) {
                                return new UpdateFunc() {
                                    @Override
                                    public PMap<String, String> apply(PMap<String, String> argument) {
                                        PMap<String, String> a = f.apply(argument);
                                        return a.plus("servingWeight", Double.toString(Double.parseDouble(a.get("objectWeight")) * Double.parseDouble(a.get("quantity"))))
                                                .plus("leftoversWeight", Double.toString(0));
                                    }
                                };
                            }
                        }), escapedFoodDesc
                    )
                )
            );
        } else
            return done();
    }
}