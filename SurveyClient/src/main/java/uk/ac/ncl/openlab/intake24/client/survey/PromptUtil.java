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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Focusable;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Pair;
import uk.ac.ncl.openlab.intake24.client.LoadingPanel;
import uk.ac.ncl.openlab.intake24.client.api.AsyncRequest;
import uk.ac.ncl.openlab.intake24.client.api.AsyncRequestAuthHandler;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScript;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.UpdateFunc;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;

public class PromptUtil {

    private static final SurveyMessages messages = SurveyMessages.Util.getInstance();

    public static <T> Prompt<FoodEntry, FoodOperation> asFoodPrompt(final SimplePrompt<T> prompt, final Function1<T, FoodOperation> updateFunc) {
        return new Prompt<FoodEntry, FoodOperation>() {
            @Override
            public SurveyStageInterface getInterface(final Callback1<FoodOperation> onComplete,
                                                     Callback1<Function1<FoodEntry, FoodEntry>> updateIntermediateState) {
                return new SurveyStageInterface.Aligned(prompt.getInterface(new Callback1<T>() {
                    @Override
                    public void call(T result) {
                        onComplete.call(updateFunc.apply(result));
                    }
                }), HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP, SurveyStageInterface.DEFAULT_OPTIONS, prompt.getClassName());
            }
        };
    }

    public static <T> Prompt<Pair<FoodEntry, Meal>, MealOperation> asExtendedFoodPrompt(final SimplePrompt<T> prompt,
                                                                                        final Function1<T, MealOperation> updateFunc) {
        return new Prompt<Pair<FoodEntry, Meal>, MealOperation>() {

            @Override
            public SurveyStageInterface getInterface(final Callback1<MealOperation> onComplete,
                                                     Callback1<Function1<Pair<FoodEntry, Meal>, Pair<FoodEntry, Meal>>> updateIntermediateState) {
                return new SurveyStageInterface.Aligned(prompt.getInterface(new Callback1<T>() {
                    @Override
                    public void call(T result) {
                        onComplete.call(updateFunc.apply(result));
                    }
                }), HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP, SurveyStageInterface.DEFAULT_OPTIONS, prompt.getClassName());
            }
        };
    }

    public static <T> Prompt<Meal, MealOperation> asMealPrompt(final SimplePrompt<T> prompt, final Function1<T, MealOperation> updateFunc) {
        return new Prompt<Meal, MealOperation>() {
            @Override
            public SurveyStageInterface getInterface(final Callback1<MealOperation> onComplete,
                                                     Callback1<Function1<Meal, Meal>> updateIntermediateState) {
                return new SurveyStageInterface.Aligned(prompt.getInterface(new Callback1<T>() {
                    @Override
                    public void call(T result) {
                        onComplete.call(updateFunc.apply(result));
                    }
                }), HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP, SurveyStageInterface.DEFAULT_OPTIONS, prompt.getClassName());
            }
        };
    }

    public static <T> Prompt<Survey, SurveyOperation> asSurveyPrompt(final SimplePrompt<T> prompt, final Function1<T, SurveyOperation> updateFunc) {
        return new Prompt<Survey, SurveyOperation>() {
            @Override
            public SurveyStageInterface getInterface(final Callback1<SurveyOperation> onComplete,
                                                     Callback1<Function1<Survey, Survey>> updateIntermediateState) {
                return new SurveyStageInterface.Aligned(prompt.getInterface(new Callback1<T>() {
                    @Override
                    public void call(T result) {
                        onComplete.call(updateFunc.apply(result));
                    }
                }), HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP, SurveyStageInterface.DEFAULT_OPTIONS, prompt.getClassName());
            }
        };
    }

    public static Prompt<FoodEntry, FoodOperation> loading(final String message, final AsyncRequest<PortionSizeScript> load,
                                                           final Function1<PortionSizeScript, FoodOperation> f) {
        return new Prompt<FoodEntry, FoodOperation>() {
            @Override
            public SurveyStageInterface getInterface(final Callback1<FoodOperation> onComplete,
                                                     Callback1<Function1<FoodEntry, FoodEntry>> updateIntermediateState) {
                final FlowPanel contents = new FlowPanel();
                contents.add(new LoadingPanel(message));

                AsyncRequestAuthHandler.execute(load, new AsyncCallback<PortionSizeScript>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        contents.clear();
                        contents.add(WidgetFactory.createDefaultErrorMessage());
                        contents.add(WidgetFactory.createBackLink());
                    }

                    @Override
                    public void onSuccess(PortionSizeScript result) {
                        onComplete.call(f.apply(result));
                    }
                });

                return new SurveyStageInterface.Aligned(contents, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP,
                        SurveyStageInterface.DEFAULT_OPTIONS, LoadingPanel.class.getSimpleName());
            }
        };
    }

    public static <T, R> SimplePrompt<R> map(final SimplePrompt<T> prompt, final Function1<T, R> f) {
        return new SimplePrompt<R>() {
            @Override
            public FlowPanel getInterface(final Callback1<R> onComplete) {
                return prompt.getInterface(new Callback1<T>() {
                    @Override
                    public void call(T arg1) {
                        onComplete.call(f.apply(arg1));
                    }
                });
            }

            @Override
            public String getClassName() {
                return prompt.getClassName();
            }
        };
    }

    public static final SimplePrompt<UpdateFunc> setAdditionalField(SimplePrompt<UpdateFunc> prompt, final String key, final String value) {
        return map(prompt, new Function1<UpdateFunc, UpdateFunc>() {
            @Override
            public UpdateFunc apply(UpdateFunc argument) {
                return argument.setField(key, value);
            }
        });
    }

    public static void addBackLink(FlowPanel container) {
        if (!History.getToken().equals("0")) {
            FlowPanel anchorDiv = new FlowPanel();
            anchorDiv.addStyleName("intake24-back-link-container");
            anchorDiv.getElement().setTabIndex(0);

            Anchor back = new Anchor(SafeHtmlUtils.fromSafeConstant(messages.goBackLabel()));
            back.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    History.back();
                }
            });

            back.addStyleName("intake24-back-link");

            anchorDiv.add(back);
            container.insert(anchorDiv, 0);
        }
    }

    public static final SimplePrompt<UpdateFunc> withBackLink(final SimplePrompt<UpdateFunc> prompt) {
        return new SimplePrompt<UpdateFunc>() {
            @Override
            public FlowPanel getInterface(final Callback1<UpdateFunc> onComplete) {
                FlowPanel contents = prompt.getInterface(onComplete);

                addBackLink(contents);

                return contents;
            }

            @Override
            public String getClassName() {
                return prompt.getClassName();
            }
        };
    }

    public static final SimplePrompt<UpdateFunc> withHeader(final SimplePrompt<UpdateFunc> prompt, final String headerText) {
        return new SimplePrompt<UpdateFunc>() {
            @Override
            public FlowPanel getInterface(final Callback1<UpdateFunc> onComplete) {
                FlowPanel contents = prompt.getInterface(onComplete);

                contents.insert(new HTMLPanel("h1", headerText), 0);

                return contents;
            }

            @Override
            public String getClassName() {
                return prompt.getClassName();
            }
        };
    }
}
