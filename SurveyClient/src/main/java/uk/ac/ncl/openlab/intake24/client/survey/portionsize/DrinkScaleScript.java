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
import uk.ac.ncl.openlab.intake24.client.api.foods.DrinkScale;
import uk.ac.ncl.openlab.intake24.client.api.foods.DrinkwareSet;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodData;
import uk.ac.ncl.openlab.intake24.client.survey.PromptUtil;
import uk.ac.ncl.openlab.intake24.client.survey.SimplePrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;

import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withBackLink;
import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withHeader;
import static uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptUtil.*;

public class DrinkScaleScript implements PortionSizeScript {
    public static String name = "drink-scale";

    private final DrinkwareSet drinkwareDef;
    private final ImageMap guideImage;

    private final PromptMessages messages = GWT.create(PromptMessages.class);

    public DrinkScaleScript(ImageMap guideImage, DrinkwareSet drinkwareDef) {
        this.guideImage = guideImage;
        this.drinkwareDef = drinkwareDef;
    }

    private DrinkScale getScaleDef(int index) {
        DrinkScale scale = null;

        for (DrinkScale d : drinkwareDef.scales)
            if (d.objectId == index)
                scale = d;

        if (scale == null)
            throw new IllegalStateException("No scale definition for container id " + index);

        return scale;
    }

    private Function1<UpdateFunc, UpdateFunc> calcFillVolumeIfSkip = new Function1<UpdateFunc, UpdateFunc>() {
        @Override
        public UpdateFunc apply(UpdateFunc argument) {
            return argument.compose(new UpdateFunc() {
                @Override
                public PMap<String, String> apply(PMap<String, String> argument) {
                    int index = Integer.parseInt(argument.get("containerIndex"));
                    double f = Double.parseDouble(argument.get("initial-fill-level"));

                    if (argument.get("skip-fill-level").equals("true"))
                        return argument.plus("fillLevel", Double.toString(f)).plus("servingWeight", Double.toString(getScaleDef(index).calcVolume(f)));
                    else
                        return argument;
                }
            });
        }
    };

    private Function1<UpdateFunc, UpdateFunc> calcFillVolume = new Function1<UpdateFunc, UpdateFunc>() {
        @Override
        public UpdateFunc apply(UpdateFunc argument) {
            return argument.compose(new UpdateFunc() {
                @Override
                public PMap<String, String> apply(PMap<String, String> argument) {
                    int index = Integer.parseInt(argument.get("containerIndex"));
                    double f = Double.parseDouble(argument.get("fillLevel"));

                    return argument.plus("servingWeight", Double.toString(getScaleDef(index).calcVolume(f)));
                }
            });
        }
    };

    private Function1<UpdateFunc, UpdateFunc> calcLeftoversVolume = new Function1<UpdateFunc, UpdateFunc>() {
        @Override
        public UpdateFunc apply(UpdateFunc argument) {
            return argument.compose(new UpdateFunc() {
                @Override
                public PMap<String, String> apply(PMap<String, String> argument) {

                    if (argument.containsKey("leftoversLevel")) {
                        int index = Integer.parseInt(argument.get("containerIndex"));
                        double f = Double.parseDouble(argument.get("leftoversLevel"));

                        return argument.plus("leftoversWeight", Double.toString(getScaleDef(index).calcVolume(f)));
                    } else
                        return argument;
                }
            });
        }
    };

    @Override
    public Option<SimplePrompt<UpdateFunc>> nextPrompt(final PMap<String, String> data, FoodData foodData) {
        String escapedFoodDesc = SafeHtmlUtils.htmlEscape(foodData.description().toLowerCase());

        if (!data.containsKey("containerIndex")) {
            return Option
                    .some(PromptUtil.map(
                            withBackLink(
                                    withHeader(
                                            guidePrompt(SafeHtmlUtils.fromSafeConstant(messages.drinkScale_containerPromptText(escapedFoodDesc)), guideImage, "containerIndex", "imageUrl"),
                                            foodData.description())), calcFillVolumeIfSkip));
        } else if (!data.containsKey("fillLevel")) {
            return Option.some(PromptUtil.map(
                    withBackLink(
                            withHeader(
                                    drinkScalePrompt(
                                            SafeHtmlUtils.fromSafeConstant(messages.drinkScale_servedPromptText()), getScaleDef(Integer.parseInt(data.get("containerIndex"))),
                                            messages.drinkScale_servedLessButtonLabel(), messages.drinkScale_servedMoreButtonLabel(), messages.drinkScale_servedContinueButtonLabel(),
                                            1.0, Double.parseDouble(data.get("initial-fill-level")), "fillLevel"), foodData.description())), calcFillVolume));
        } else
            return withoutLeftoversDrinkDone();
        //else if (!data.containsKey("leftoversLevel")) {
            // return done();
            // if (!data.containsKey("leftovers"))
            //     return Option.some(PromptUtil.map(
            //             withBackLink(
            //                     withHeader(
            //                             yesNoPromptZeroField(SafeHtmlUtils.fromSafeConstant(messages.drinkScale_leftoversQuestionPromptText(escapedFoodDesc)), messages.yesNoQuestion_defaultYesLabel(), messages.yesNoQuestion_defaultNoLabel(), "leftovers",
            //                                     "leftoversLevel"), foodData.description())), calcLeftoversVolume));
            // else
            //     return Option.some(PromptUtil.map(
            //             withBackLink(
            //                     withHeader(
            //                             drinkScalePrompt(SafeHtmlUtils.fromSafeConstant(messages.drinkScale_leftPromptText(escapedFoodDesc)),
            //                                     getScaleDef(Integer.parseInt(data.get("containerIndex"))), messages.drinkScale_leftLessButtonLabel(), messages.drinkScale_leftMoreButtonLabel(), messages.drinkScale_leftContinueButtonLabel(),
            //                                     Double.parseDouble(data.get("fillLevel")), 0, "leftoversLevel"), foodData.description())), calcLeftoversVolume));
        //} 

    }
}
