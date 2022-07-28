package uk.ac.ncl.openlab.intake24.client.survey.prompts.simple;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;

public class WeightFactorUI extends Composite {

    private final FlowPanel weightFactorLabel;
    private final WeightFactorLabels labelsImpl;

    public final int minNumerator;
    public final int maxNumerator;
    public final int denominator;
    public final double baseWeight;
    public final boolean useLeftoversLabel;
    public final boolean useSmallestPortionLabel;

    private int numerator;

    private static int gcm(int a, int b) {
        return b == 0 ? a : gcm(b, a % b);
    }

    public int getWeightFactor() {
        return numerator;
    }

    public void setWeightFactor(int newNumerator) {
        numerator = newNumerator;

        boolean hasWhole = numerator >= denominator;
        boolean hasFraction = numerator % denominator > 0;
        boolean displayFraction = hasFraction || labelsImpl.displayZeroFraction();

        weightFactorLabel.clear();

        if (useLeftoversLabel)
            weightFactorLabel.add(new HTMLPanel("span", PromptMessages.INSTANCE.asServed_weightFactor_I_left() + " "));
        else
            weightFactorLabel.add(new HTMLPanel("span", PromptMessages.INSTANCE.asServed_weightFactor_I_had() + " "));

        if (hasWhole) {
            weightFactorLabel.add(labelsImpl.getWholeLabelWidget());
            labelsImpl.updateWholeLabel(numerator / denominator);

            if (displayFraction)
                weightFactorLabel.add(new HTMLPanel("span", " " + PromptMessages.INSTANCE.asServed_weightFactor_and() + " "));
        }

        if (displayFraction) {
            weightFactorLabel.add(labelsImpl.getFractionLabelWidget());
            int gcm = gcm(numerator % denominator, denominator);
            labelsImpl.updateFractionLabel((numerator % denominator) / gcm, denominator / gcm);
        }

        if (useSmallestPortionLabel)
            weightFactorLabel.add(new HTMLPanel("span",
                    "<br/>" + PromptMessages.INSTANCE.asServed_weightFactor_smallestPortion() + "</br>" +
                            PromptMessages.INSTANCE.asServed_weightFactor_weight(NumberFormat.getDecimalFormat().format(Math.round(baseWeight * numerator / denominator)))));
        else
            weightFactorLabel.add(new HTMLPanel("span", "<br/>" + PromptMessages.INSTANCE.asServed_weightFactor_largestPortion() + "</br>" +
                    PromptMessages.INSTANCE.asServed_weightFactor_weight(NumberFormat.getDecimalFormat().format(Math.round(baseWeight * numerator / denominator)))));
    }

    public WeightFactorUI(int minNumerator, int maxNumerator, int denominator, double baseWeight, boolean useSmallestPortionLabel,
                          boolean useLeftoversLabel, WeightFactorLabels labelsImpl) {

        this.denominator = denominator;
        this.minNumerator = minNumerator;
        this.maxNumerator = maxNumerator;
        this.baseWeight = baseWeight;
        this.useLeftoversLabel = useLeftoversLabel;
        this.useSmallestPortionLabel = useSmallestPortionLabel;
        this.labelsImpl = labelsImpl;

        numerator = minNumerator;

        FlowPanel container = new FlowPanel();
        container.addStyleName("intake24-as-served-weight-factor-container");

        Button more = new Button();

        HTMLPanel moreLabel = new HTMLPanel("i", "");
        moreLabel.addStyleName("fas fa-plus intake24-as-served-weight-factor-button");
        more.getElement().appendChild(moreLabel.getElement());
        more.addClickHandler(e -> setWeightFactor(Math.min(maxNumerator, numerator + 1)));

        Button less = new Button();
        HTMLPanel lessLabel = new HTMLPanel("i", "");
        lessLabel.addStyleName("fas fa-minus intake24-as-served-weight-factor-button");
        less.getElement().appendChild(lessLabel.getElement());
        less.addClickHandler(e -> setWeightFactor(Math.max(minNumerator, numerator - 1)));

        weightFactorLabel = new FlowPanel();
        weightFactorLabel.addStyleName("ui");

        setWeightFactor(numerator);

        container.add(more);
        container.add(weightFactorLabel);
        container.add(less);

        initWidget(container);
    }
}

