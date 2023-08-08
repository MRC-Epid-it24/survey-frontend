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

public class MealDelivery implements PromptRule<Meal, MealOperation> {

    private static final String MEAL_DELIVERY_KEY = "mealDelivery";

    private static final String promptTemplate = "<p>Was your $meal purchased through a 3<sup>rd</sup> party ordering service (<em>e.g. Deliveroo, JustEat, etc.</em>)?</p>";

    private static final PVector<MultipleChoiceQuestionOption> options =
            TreePVector.<MultipleChoiceQuestionOption>empty()
                    .plus(new MultipleChoiceQuestionOption("Yes"))
                    .plus(new MultipleChoiceQuestionOption("No"))
                    .plus(new MultipleChoiceQuestionOption("Don't know"));

    @Override
    public Option<Prompt<Meal, MealOperation>> apply(Meal state, SelectionMode selectionType, PSet<String> surveyFlags) {
        if (!state.customData.containsKey(MEAL_DELIVERY_KEY) &&
                state.customData.containsKey(MealLocation.MEAL_LOCATION_KEY) &&
                !state.customData.get(MealLocation.MEAL_LOCATION_KEY).equals("Yes") &&
                !state.isEmpty() && state.portionSizeComplete()) {
            SafeHtml promptSafeText = SafeHtmlUtils.fromSafeConstant(promptTemplate.replace("$meal", SafeHtmlUtils.htmlEscape(state.name.toLowerCase())));

            RadioButtonPrompt prompt = new RadioButtonPrompt(promptSafeText, MealDelivery.class.getSimpleName(),
                    options, PromptMessages.INSTANCE.mealComplete_continueButtonLabel(),
                    "mealDelivery");

            return Option.some(PromptUtil.asMealPrompt(prompt, answer ->
                    MealOperation.setCustomDataField(MEAL_DELIVERY_KEY, answer.getValue())));
        } else {
            return Option.none();
        }
    }

    public static WithPriority<PromptRule<Meal, MealOperation>> withPriority(int priority) {
        return new WithPriority<>(new MealDelivery(), priority);
    }
}
