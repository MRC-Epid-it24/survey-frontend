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
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodData;
import uk.ac.ncl.openlab.intake24.client.survey.PromptUtil;
import uk.ac.ncl.openlab.intake24.client.survey.SimplePrompt;

import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;

import java.util.List;

import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withBackLink;
import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.withHeader;
import static uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptUtil.*;

public class StandardPortionScript implements PortionSizeScript {
    public static final String name = "standard-portion";
    public final List<StandardUnitDef> units;

    private final static PromptMessages messages = PromptMessages.Util.getInstance();
    private final static StandardUnits unitNames = StandardUnits.Util.getInstance();

    public StandardPortionScript(List<StandardUnitDef> units) {
        this.units = units;
    }

    public Option<SimplePrompt<UpdateFunc>> mkQuantityPrompt(PMap<String, String> data, final int unitChoice, String foodDesc) {
        String message;
        StandardUnitDef unit = units.get(unitChoice);
        String escapedFoodDesc = SafeHtmlUtils.htmlEscape(foodDesc);

        if (unit.omitFoodDesc)
            message = messages.standardUnit_quantityPromptText_omitFood(unitNames.getString(unit.name + "_how_many"));
        else
            message = messages.standardUnit_quantityPromptText_includeFood(unitNames.getString(unit.name + "_how_many"), escapedFoodDesc.toLowerCase());


        return Option.some(
            withBackLink(
                withHeader(
                    PromptUtil.map(
                        quantityPrompt(SafeHtmlUtils.fromSafeConstant(message), messages.standardUnit_quantityContinueButtonLabel(), "quantity"),
                        new Function1<UpdateFunc, UpdateFunc>() {
                            @Override
                            public UpdateFunc apply(final UpdateFunc f) {
                                return new UpdateFunc() {
                                    @Override
                                    public PMap<String, String> apply(PMap<String, String> argument) {
                                        PMap<String, String> a = f.apply(argument);
                                        return a.plus("servingWeight", Double.toString(units.get(unitChoice).weight * Double.parseDouble(a.get("quantity")))).plus(
                                                "leftoversWeight", Double.toString(0));
                                    }
                                };
                            }
                        }
                    ), escapedFoodDesc
                )
            )
        );
    }

    @Override
    public Option<SimplePrompt<UpdateFunc>> nextPrompt(PMap<String, String> data, FoodData foodData) {
        String escapedFoodDesc = SafeHtmlUtils.htmlEscape(foodData.description());

        if (data.containsKey("servingWeight"))
            return done();
        else if (!data.containsKey("unit-choice")) {
            if (units.size() > 1)
                return Option.some(
                    withBackLink(
                        withHeader(
                            standardUnitChoicePrompt(SafeHtmlUtils.fromSafeConstant(messages.standardUnit_unitChoicePromptText(escapedFoodDesc)), messages.standardUnit_unitChoiceContinueButtonLabel(), units, new Function1<StandardUnitDef, String>() {
                            @Override
                            public String apply(StandardUnitDef argument) {
                                return messages.standardUnit_choiceLabel(SafeHtmlUtils.htmlEscape(unitNames.getString(argument.name + "_estimate_in")));
                            }
                            }, "unit-choice"), escapedFoodDesc
                        )
                    )
                );
            else
                return mkQuantityPrompt(data, 0, foodData.description());
        } else
            return mkQuantityPrompt(data, Integer.parseInt(data.get("unit-choice")), foodData.description());

    }
}