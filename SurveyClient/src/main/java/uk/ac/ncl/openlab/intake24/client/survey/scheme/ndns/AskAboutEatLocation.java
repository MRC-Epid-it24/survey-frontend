package uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns;

import org.pcollections.PVector;
import org.pcollections.TreePVector;
import uk.ac.ncl.openlab.intake24.client.survey.Meal;
import uk.ac.ncl.openlab.intake24.client.survey.PromptRule;
import uk.ac.ncl.openlab.intake24.client.survey.WithPriority;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MultipleChoiceQuestionOption;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.base.FoodEatLocation;

public class AskAboutEatLocation extends FoodEatLocation {

    public AskAboutEatLocation(String promptText, PVector<MultipleChoiceQuestionOption> options) {
        super(promptText, options);
    }

    public static WithPriority<PromptRule<Meal, MealOperation>> withPriority(int priority) {
        return new WithPriority<>(new AskAboutEatLocation(
                "<p>Where did you eat your %s? Tick one.</p>",
                TreePVector.<MultipleChoiceQuestionOption>empty()
                        .plus(new MultipleChoiceQuestionOption("In School (including outdoor areas within the school site)"))
                        .plus(new MultipleChoiceQuestionOption("At home"))
                        .plus(new MultipleChoiceQuestionOption("On the journey to/from school"))
                        .plus(new MultipleChoiceQuestionOption("Another location:", "Other", true))
        ), priority);
    }
}
