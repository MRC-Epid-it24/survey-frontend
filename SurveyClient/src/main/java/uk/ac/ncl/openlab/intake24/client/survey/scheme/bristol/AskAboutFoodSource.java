package uk.ac.ncl.openlab.intake24.client.survey.scheme.bristol;

import org.pcollections.PVector;
import org.pcollections.TreePVector;
import uk.ac.ncl.openlab.intake24.client.survey.Meal;
import uk.ac.ncl.openlab.intake24.client.survey.PromptRule;
import uk.ac.ncl.openlab.intake24.client.survey.WithPriority;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MealOperation;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.MultipleChoiceQuestionOption;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.base.FoodSource;

public class AskAboutFoodSource extends FoodSource {

    public AskAboutFoodSource(String promptText, PVector<MultipleChoiceQuestionOption> options) {
        super(promptText, options);
    }

    public static WithPriority<PromptRule<Meal, MealOperation>> withPriority(int priority) {
        return new WithPriority<>(new AskAboutFoodSource(
                "<p>Where did you get <strong>most</strong> of the food/drink for your %s?</p>",
                TreePVector.<MultipleChoiceQuestionOption>empty()
                        .plus(new MultipleChoiceQuestionOption("Supermarket"))
                        .plus(new MultipleChoiceQuestionOption("Local / convenience store (<em>e.g. Premier, Spar, Nisa, etc.</em>)", "Local / convenience store"))
                        .plus(new MultipleChoiceQuestionOption("Traditional market / delicatessen / butchers / fishmongers / farm shop"))
                        .plus(new MultipleChoiceQuestionOption("Bakery / sandwich shop (<em>e.g. Greggs, Cooplands, Pret a Manger, etc.</em>)", "Bakery / sandwich shop"))
                        .plus(new MultipleChoiceQuestionOption("Transport hub (<em>e.g. petrol station, motorway service station, airport, etc.</em>)", "Transport hub"))
                        .plus(new MultipleChoiceQuestionOption("Restaurant"))
                        .plus(new MultipleChoiceQuestionOption("Café / coffee house / tea rooms"))
                        .plus(new MultipleChoiceQuestionOption("Pub / bar / club"))
                        .plus(new MultipleChoiceQuestionOption("Fast food / takeaway outlet / street food outlet"))
                        .plus(new MultipleChoiceQuestionOption("Non-food shops (<em>e.g. pharmacies, discount stores, off licences, etc.</em>)", "Non-food shops"))
                        .plus(new MultipleChoiceQuestionOption("Leisure facility (<em>e.g. tourist attraction, gym, cinema, concert venue, hotels, etc.</em>)", "Leisure facility"))
                        .plus(new MultipleChoiceQuestionOption("Vending machine"))
                        .plus(new MultipleChoiceQuestionOption("Canteen (<em>e.g. at work, college, university, etc.</em>)", "Canteen"))
                        .plus(new MultipleChoiceQuestionOption("Grown at home / allotment (includes tap water)", "Grown at home / allotment"))
                        .plus(new MultipleChoiceQuestionOption("Other (<em>e.g. online store, food bank, etc.</em>)", "Other"))
                        .plus(new MultipleChoiceQuestionOption("Don't know"))
        ), priority);
    }
}
