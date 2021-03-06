package uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns;

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

public class AskIfCookedAtHome implements PromptRule<Meal, MealOperation> {

    public static final String COOKED_AT_HOME_KEY = "cookedAtHome";

    private static final PVector<MultipleChoiceQuestionOption> options = TreePVector.<MultipleChoiceQuestionOption>empty()
            .plus(new MultipleChoiceQuestionOption("Yes"))
            .plus(new MultipleChoiceQuestionOption("No"))
            .plus(new MultipleChoiceQuestionOption("Don't know"));

    @Override
    public Option<Prompt<Meal, MealOperation>> apply(Meal state, SelectionMode selectionType, PSet<String> surveyFlags) {
        if (!state.customData.containsKey(COOKED_AT_HOME_KEY) && state.portionSizeComplete()) {

            SafeHtml promptText = SafeHtmlUtils.fromSafeConstant("<p>Was your " + SafeHtmlUtils.htmlEscape(state.name.toLowerCase()) + " prepared and cooked at home?</p>");

            RadioButtonPrompt prompt = new RadioButtonPrompt(promptText, AskIfCookedAtHome.class.getSimpleName(),
                    options, PromptMessages.INSTANCE.mealComplete_continueButtonLabel(),
                    "cookedAtHomeOption");

            return Option.some(PromptUtil.asMealPrompt(prompt, answer -> MealOperation.setCustomDataField(COOKED_AT_HOME_KEY, answer.value)));
        } else {
            return Option.none();
        }
    }

    public static WithPriority<PromptRule<Meal, MealOperation>> withPriority(int priority) {
        return new WithPriority<>(new AskIfCookedAtHome(), priority);
    }
}
