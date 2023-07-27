package uk.ac.ncl.openlab.intake24.client.survey.scheme.bristol;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.pcollections.PSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.survey.*;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MultipleChoiceQuestionOption;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.simple.RadioButtonPrompt;

public class MealLocation implements PromptRule<Meal, MealOperation> {

    public static final String MEAL_LOCATION_KEY = "mealLocation";

    private static final String promptTemplate = "<p>Did you consume your $meal in the same place as where you got it?</p>";

    private static final PVector<MultipleChoiceQuestionOption> options =
            TreePVector.<MultipleChoiceQuestionOption>empty()
                    .plus(new MultipleChoiceQuestionOption("Yes"))
                    .plus(new MultipleChoiceQuestionOption("No, I took it away"))
                    .plus(new MultipleChoiceQuestionOption("No, it was delivered to me"));

    @Override
    public Option<Prompt<Meal, MealOperation>> apply(Meal state, SelectionMode selectionType, PSet<String> surveyFlags) {
        if (!state.customData.containsKey(MEAL_LOCATION_KEY) && !state.isEmpty() && state.portionSizeComplete()) {
            SafeHtml promptSafeText = SafeHtmlUtils.fromSafeConstant(promptTemplate.replace("$meal", SafeHtmlUtils.htmlEscape(state.name.toLowerCase())));

            RadioButtonPrompt prompt = new RadioButtonPrompt(promptSafeText, MealLocation.class.getSimpleName(),
                    options, PromptMessages.INSTANCE.mealComplete_continueButtonLabel(),
                    "mealLocation");

            return Option.some(PromptUtil.asMealPrompt(prompt, answer ->
                    MealOperation.setCustomDataField(MEAL_LOCATION_KEY, answer.getValue())));
        } else {
            return Option.none();
        }
    }

    public static WithPriority<PromptRule<Meal, MealOperation>> withPriority(int priority) {
        return new WithPriority<>(new MealLocation(), priority);
    }
}
