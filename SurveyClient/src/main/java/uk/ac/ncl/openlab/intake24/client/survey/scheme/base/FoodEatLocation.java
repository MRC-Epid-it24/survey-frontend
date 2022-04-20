package uk.ac.ncl.openlab.intake24.client.survey.scheme.base;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.pcollections.PSet;
import org.pcollections.PVector;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MultipleChoiceQuestionOption;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.simple.RadioButtonPrompt;

public abstract class FoodEatLocation implements PromptRule<Meal, MealOperation> {

    private static final String FOOD_EAT_LOCATION_KEY = "foodEatLocation";

    private String promptText;
    private PVector<MultipleChoiceQuestionOption> options;

    public FoodEatLocation(String promptText, PVector<MultipleChoiceQuestionOption> options) {
        this.promptText = promptText;
        this.options = options;
    }

    @Override
    public Option<Prompt<Meal, MealOperation>> apply(Meal state, SelectionMode selectionType, PSet<String> surveyFlags) {
        if (!state.customData.containsKey(FOOD_EAT_LOCATION_KEY) && !state.isEmpty() && state.portionSizeComplete()) {
            SafeHtml promptSafeText = SafeHtmlUtils.fromSafeConstant(
                    promptText.replace("%s", SafeHtmlUtils.htmlEscape(state.name.toLowerCase()))
            );

            RadioButtonPrompt prompt = new RadioButtonPrompt(promptSafeText, FoodEatLocation.class.getSimpleName(),
                    options, PromptMessages.INSTANCE.mealComplete_continueButtonLabel(),
                    "foodEatLocationOption");

            return Option.some(PromptUtil.asMealPrompt(prompt, answer ->
                    MealOperation.setCustomDataField(FOOD_EAT_LOCATION_KEY, answer.getValue())));
        } else {
            return Option.none();
        }
    }
}
