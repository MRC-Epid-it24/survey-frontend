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

import java.util.logging.Logger;

import org.pcollections.PVector;
import org.workcraft.gwt.shared.client.Callback;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.Callback2;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import org.workcraft.gwt.shared.client.Option.SideEffectVisitor;

import com.google.gwt.user.client.ui.FlowPanel;
import uk.ac.ncl.openlab.intake24.client.survey.portionsize.PortionSizeScriptManager;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.AddMealPrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.DeleteMealPrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.EditMealPrompt;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.EditTimePrompt;

public class IntakeSurvey implements SurveyStage<Survey> {
	public final static String FLAG_INTAKE_SURVEY_COMPLETE = "intake-survey-complete";

	private final PromptManager promptManager;
	private final SelectionManager selectionManager;
	private final StateManager baseStateManager;

	private IntakeStateManager stateManager;

	private final Logger log = Logger.getLogger("IntakeSurvey");

	public IntakeSurvey(StateManager stateManager, PromptManager promptManager, SelectionManager selectionManager,
			PortionSizeScriptManager scriptManager) {
		this.baseStateManager = stateManager;
		this.promptManager = promptManager;
		this.selectionManager = selectionManager;
	}

	private NavigationPanel navigationPanel;
	private PromptInterfaceManager interfaceManager;
	private Callback1<SurveyOperation> applyOperation;
	private Callback1<Survey> onComplete;
	private Callback1<Function1<Survey, Survey>> updateIntermediateState;

	private SimpleSurveyStageInterface cachedInterface = null;
	
	public void showPrompt(Prompt<Survey, SurveyOperation> prompt) {
		interfaceManager.applyInterface(prompt, applyOperation, updateIntermediateState);
	}

	private Selection convertToAuto(Selection selection) {
		return selection.accept(new Selection.Visitor<Selection>() {
			@Override
			public Selection visitMeal(Selection.SelectedMeal meal) {
				return new Selection.SelectedMeal(meal.mealIndex, SelectionMode.AUTO_SELECTION);
			}

			@Override
			public Selection visitFood(Selection.SelectedFood food) {
				return new Selection.SelectedFood(food.mealIndex, food.foodIndex, SelectionMode.AUTO_SELECTION);
			}

			@Override
			public Selection visitNothing(Selection.EmptySelection selection) {
				return new Selection.EmptySelection(SelectionMode.AUTO_SELECTION);
			}
		});
	}

	public void showNextPrompt() {
		Survey currentState = stateManager.getCurrentState();
		
		Option<Prompt<Survey, SurveyOperation>> nextPrompt = promptManager.nextPromptForSelection(currentState);

		final Survey stateWithForcedAutoSelection = currentState.withSelection(convertToAuto(currentState.selectedElement));
		stateManager.updateState(stateWithForcedAutoSelection, false);

		nextPrompt.accept(new SideEffectVisitor<Prompt<Survey, SurveyOperation>>() {
			@Override
			public void visitSome(Prompt<Survey, SurveyOperation> prompt) {
				showPrompt(prompt);
			}

			@Override
			public void visitNone() {
				// No prompt available for the current selection
				// Try to auto-select something else

				Option<Selection> nextSelection = selectionManager.nextSelection(stateWithForcedAutoSelection);

				nextSelection.accept(new SideEffectVisitor<Selection>() {
					@Override
					public void visitSome(Selection item) {
						// Selected something, pull the next prompt for
						// selection
						Survey updatedState = stateWithForcedAutoSelection.withSelection(item);
						// System.out.println ("Auto-select: " + item);
						stateManager.updateState(updatedState, false);
						showNextPrompt();
					}

					@Override
					public void visitNone() {
						// nothing else to be selected, get next global
						// prompt

						promptManager.nextGlobalPrompt(stateWithForcedAutoSelection).accept(new SideEffectVisitor<Prompt<Survey, SurveyOperation>>() {
							@Override
							public void visitSome(Prompt<Survey, SurveyOperation> prompt) {
								showPrompt(prompt);
							}

							@Override
							public void visitNone() {
								// no associatedFoods left, report completion
								onComplete.call(stateWithForcedAutoSelection.withFlag(Survey.FLAG_NO_MORE_PROMPTS).withFlag(Survey.FLAG_SKIP_HISTORY));
							}
						});

					}
				});
			}
		});
	}

	@Override
	public SimpleSurveyStageInterface getInterface(Callback1<Survey> onComplete, Callback2<Survey, Boolean> onIntermediateStateChange) {
		if (cachedInterface != null) {
			showNextPrompt();
			return cachedInterface;
		} else {

			navigationPanel = new NavigationPanel(baseStateManager.getCurrentState());

			stateManager = new IntakeStateManager(baseStateManager, navigationPanel);

			this.onComplete = onComplete;

			final FlowPanel promptPanel = new FlowPanel();
			promptPanel.getElement().setId("intake24-prompt-panel");
			
			interfaceManager = new PromptInterfaceManager(promptPanel);

			applyOperation = new Callback1<SurveyOperation>() {
				@Override
				public void call(final SurveyOperation op) {
					op.accept(new SurveyOperation.SideEffectVisitor() {
						@Override
						public void visitNoChange() {
							showNextPrompt();
						}

						@Override
						public void visitEditFoodsRequest(int mealIndex, boolean addDrink) {
							showPrompt(new PromptAdapter.ForMeal(mealIndex, new EditMealPrompt(stateManager.getCurrentState().meals.get(mealIndex), addDrink)));
						}

						@Override
						public void visitEditTimeRequest(int mealIndex) {
							Survey currentState = stateManager.getCurrentState();
							showPrompt(new PromptAdapter.ForMeal(mealIndex, new EditTimePrompt(currentState.meals.get(mealIndex).name, currentState.meals
									.get(mealIndex).time.getOrDie())));
						}

						@Override
						public void visitUpdate(SurveyOperation.Update update) {
							Survey newState = update.update.apply(stateManager.getCurrentState());
							stateManager.updateState(newState, update.makeHistoryEntry);
							showNextPrompt();
						}

						@Override
						public void visitDeleteMealRequest(int mealIndex, boolean showConfirmation) {
							if (showConfirmation) {								
								showPrompt(new DeleteMealPrompt(mealIndex, stateManager.getCurrentState().meals.get(mealIndex)));
							} else {
								Survey state = stateManager.getCurrentState();
								stateManager.updateState(state.minusMeal(mealIndex).withSelection(new Selection.EmptySelection(SelectionMode.AUTO_SELECTION)), true);
								showNextPrompt();
							}
						}

						@Override
						public void visitSplitFood(int mealIndex, int foodIndex, PVector<FoodEntry> splitInto) {
							Survey curState = stateManager.getCurrentState();

							PVector<FoodEntry> updatedFoods = curState.meals.get(mealIndex).foods.minus(foodIndex).plusAll(foodIndex, splitInto);

							Survey newState = curState.withMeals(curState.meals.with(mealIndex, curState.meals.get(mealIndex).withFoods(updatedFoods)));
							stateManager.updateState(newState, true);
							showNextPrompt();
						}

						@Override
						public void visitAddMeal(int selectedIndex) {
							showPrompt(new AddMealPrompt(selectedIndex));
						}
					});
				}
			};

			updateIntermediateState = new Callback1<Function1<Survey, Survey>>() {
				@Override
				public void call(Function1<Survey, Survey> update) {
					stateManager.updateState(update.apply(stateManager.getCurrentState()), true);
				}
			};

			Callback1<Selection> requestSelection = new Callback1<Selection>() {
				@Override
				public void call(Selection sel) {
					log.info("Selection request: " + sel.toString());

					Survey currentState = stateManager.getCurrentState();
					//if (!currentState.selectedElement.equals(sel)) {
						log.info("Selection changed");
						Survey withSelection = currentState.withSelection(sel);
						stateManager.updateState(withSelection, true);
						showNextPrompt();
					//} else {
						// FIXME: should actually determine if selection change would show the same prompt
						//log.info("Element already selected, request ignored");
					//}
				}
			};

			final Callback requestAddMeal = new Callback() {
				@Override
				public void call() {
					showPrompt(new AddMealPrompt(0));
				}
			};

			navigationPanel.setCallbacks(requestSelection, requestAddMeal);
			
			FlowPanel content = new FlowPanel();
			
			content.getElement().setId("intake24-survey-container");

			content.add(navigationPanel);
			content.add(promptPanel);
			
			FlowPanel clearDiv = new FlowPanel();
			clearDiv.addStyleName("intake24-clear-floats");
			
			content.add(clearDiv);
			

			showNextPrompt();

			cachedInterface = new SimpleSurveyStageInterface(content);

			return cachedInterface;
		}
	}
}