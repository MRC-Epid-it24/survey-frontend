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


import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Panel;
import org.workcraft.gwt.imagemap.shared.ImageMap;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.api.foods.AsServedImage;
import uk.ac.ncl.openlab.intake24.client.api.foods.AsServedSet;
import uk.ac.ncl.openlab.intake24.client.api.foods.DrinkScale;
import uk.ac.ncl.openlab.intake24.client.survey.SimplePrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.simple.*;

import java.util.List;

import static uk.ac.ncl.openlab.intake24.client.survey.PromptUtil.map;


public class PortionSizeScriptUtil {
    private static final PromptMessages messages = PromptMessages.INSTANCE;

    public static final SafeHtml defaultServingSizePrompt(final String foodDescription) {
        return SafeHtmlUtils.fromSafeConstant(messages.asServed_servingPromptText(SafeHtmlUtils.htmlEscape(foodDescription.toLowerCase())));
    }

    public static final SafeHtml defaultLeftoversPrompt(final String foodDescription) {
        return SafeHtmlUtils.fromSafeConstant(messages.asServed_leftoversPromptText(SafeHtmlUtils.htmlEscape(foodDescription.toLowerCase())));
    }

    public static Option<SimplePrompt<UpdateFunc>> done() {
        return Option.none();
    }

    public static Option<SimplePrompt<UpdateFunc>> withoutLeftoversDrinkDone() {
        UpdateFunc setField = new UpdateFunc().setField("leftovers", Boolean.toString(false));
        setField.setField("leftoversLevel", "0");
        setField.setField("leftovers", Boolean.toString(false));
        return Option.none();
    }

    public static SimplePrompt<UpdateFunc> quantityPrompt(final SafeHtml promptHtml, final String confirmText, final String field) {
        return (map(new FractionalQuantityPrompt(promptHtml, confirmText), new Function1<Double, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Double argument) {
                return new UpdateFunc().setField(field, Double.toString(argument));
            }
        }));
    }

    public static SimplePrompt<UpdateFunc> standardUnitChoicePrompt(final SafeHtml promptHtml, final String confirmText, List<StandardUnitDef> units,
                                                                    Function1<StandardUnitDef, String> label, final String field) {
        return (map(new StandardUnitPrompt(promptHtml, confirmText, units, label), new Function1<Integer, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Integer argument) {
                return new UpdateFunc().setField(field, Integer.toString(argument));
            }
        }));
    }

    public static SimplePrompt<UpdateFunc> foodWeightPrompt(final SafeHtml promptHtml, final SafeHtml unitLabel, final String confirmText,
                                                            final String field, final String leftoversField) {
        return (map(new WeightTypeInPrompt(promptHtml, unitLabel, confirmText), new Function1<Double, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Double argument) {
                return new UpdateFunc().setField(field, Double.toString(argument)).setField(leftoversField, "0.0");
            }
        }));
    }

    public static SimplePrompt<UpdateFunc> optionalFoodPrompt(final String locale, final SafeHtml promptHtml, final String yesText,
                                                              final String noText, final SafeHtml foodChoicePrompt, final String category, final String choiceField, final String codeField) {
        OptionalFoodPromptDef def = new OptionalFoodPromptDef(promptHtml, yesText, noText, foodChoicePrompt, category);

        return map(new OptionalFoodPrompt(locale, def), new Function1<Option<String>, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Option<String> argument) {
                return argument.accept(new Option.Visitor<String, UpdateFunc>() {
                    @Override
                    public UpdateFunc visitSome(String item) {
                        return new UpdateFunc().setField(choiceField, "true").setField(codeField, item);
                    }

                    @Override
                    public UpdateFunc visitNone() {
                        return new UpdateFunc().setField(choiceField, "false");
                    }
                });
            }
        });
    }

    public static SimplePrompt<UpdateFunc> yesNoPrompt(final SafeHtml promptHtml, final String yesText, final String noText, final SafeHtml backText,
                                                       final String field) {
        return (map(new YesNoPrompt(promptHtml, yesText, noText), new Function1<Boolean, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Boolean argument) {
                return new UpdateFunc().setField(field, Boolean.toString(argument));
            }
        }));
    }

    public static SimplePrompt<UpdateFunc> yesNoPromptZeroField(final SafeHtml promptHtml, final String yesText, final String noText,
                                                                final String field, final String zeroField) {
        return map(new YesNoPrompt(promptHtml, yesText, noText), new Function1<Boolean, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Boolean argument) {
                UpdateFunc setField = new UpdateFunc().setField(field, Boolean.toString(argument));
                if (!argument)
                    return setField.setField(zeroField, "0");
                else
                    return setField;
            }
        });
    }

    public static SimplePrompt<UpdateFunc> asServedPrompt(final AsServedSet set,
                                                          final String lessButtonLabel,
                                                          final String moreButtonLabel,
                                                          final String confirmButtonLabel,
                                                          final String indexField,
                                                          final String imageUrlField,
                                                          final String weightField,
                                                          final Option<WeightFactorSettings> weightFactorSettings,
                                                          boolean isLeftovers,
                                                          SafeHtml promptText) {

        boolean showMore = weightFactorSettings.map(s -> s.showMoreOption).getOrElse(false);
        boolean showLess = weightFactorSettings.map(s -> s.showLessOption).getOrElse(false);

        return map(new AsServedPrompt2(set.images.toArray(new AsServedImage[set.images.size()]), promptText, lessButtonLabel,
                        moreButtonLabel, confirmButtonLabel, showMore, showLess, isLeftovers),
                result -> {
                    final UpdateFunc f = new UpdateFunc()
                            .setField(indexField, Integer.toString(result.imageIndex))
                            .setField(weightField, Double.toString(result.imageWeight))
                            .setField(imageUrlField, result.imageUrl);

                    return weightFactorSettings.accept(Option.makeVisitor(settings -> f.setField(settings.fieldName, Double.toString(result.weightFactor)), () -> f));
                });
    }

    public static SimplePrompt<UpdateFunc> guidePromptEx(final SafeHtml promptText, final ImageMap imageMap, final String indexField,
                                                         final String imageUrlField, final Function1<Callback1<Integer>, Panel> additionalControlsCtor) {
        return map(new GuidePrompt(promptText, imageMap, additionalControlsCtor), new Function1<Integer, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Integer argument) {
                return new UpdateFunc().setField(indexField, argument.toString()).setField(imageUrlField, imageMap.baseImageUrl);
            }
        });
    }

    public static SimplePrompt<UpdateFunc> guidePrompt(final SafeHtml promptText, final ImageMap imageMap, final String indexField,
                                                       final String imageUrlField) {
        return map(new GuidePrompt(promptText, imageMap), new Function1<Integer, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Integer argument) {
                return new UpdateFunc().setField(indexField, argument.toString()).setField(imageUrlField, imageMap.baseImageUrl);
            }
        });
    }

    public static SimplePrompt<UpdateFunc> drinkScalePrompt(final SafeHtml promptText, final DrinkScale scaleDef, final String lessText,
                                                            final String moreText, final String confirmText, final double limit, final double initialLevel, final String levelField) {

        DrinkScalePromptDef promptDef = new DrinkScalePromptDef(scaleDef, promptText, lessText, moreText, confirmText, limit, initialLevel);

        return map(new DrinkScalePrompt(promptDef), new Function1<Double, UpdateFunc>() {
            @Override
            public UpdateFunc apply(Double argument) {
                // UpdateFunc setLeftOverFieldImplicitly = new UpdateFunc().setField("leftovers", "false");
                return new UpdateFunc().setField(levelField, Double.toString(argument));
            }
        });
    }
}
