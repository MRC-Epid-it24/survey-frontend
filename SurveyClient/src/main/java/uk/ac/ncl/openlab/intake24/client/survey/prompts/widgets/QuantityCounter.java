package uk.ac.ncl.openlab.intake24.client.survey.prompts.widgets;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import uk.ac.ncl.openlab.intake24.client.survey.prompts.messages.PromptMessages;
import uk.ac.ncl.openlab.intake24.client.ui.widgets.LabelledCounter;

public class QuantityCounter extends Composite {

    private static final PromptMessages messages = PromptMessages.Util.getInstance();

    public final LabelledCounter wholeCounter;
    public final LabelledCounter fractionalCounter;
    public final Label wholeLabel;

    public final double min;
    public final double max;

    private double value;

    public double getValue() {
        return value;
    }

    private String getWholeLabel() {
        double whole = (int) Math.floor(value);
        return NumberFormat.getDecimalFormat().format(whole);
    }

    private String getFractionLabel() {
        double frac = value - Math.floor(value);

        if (frac < 0.25)
            return messages.quantity_noFraction();
        else if (frac < 0.5)
            return messages.quantity_oneFourth();
        else if (frac < 0.75)
            return messages.quantity_oneHalf();
        else
            return messages.quantity_threeFourths();
    }

    private void updateValue(double newValue) {
        value = Math.min(max, Math.max(min, newValue));

        wholeCounter.setLabel(getWholeLabel());
        fractionalCounter.setLabel(getFractionLabel());
    }

    public QuantityCounter(double min, double max, double init) {
        this.min = min;
        this.max = max;
        this.value = Math.min(max, Math.max(min, init));

        final FlowPanel panel = new FlowPanel();

        wholeCounter = new LabelledCounter(getWholeLabel(), messages.quantity_wholeCaption(), () -> updateValue(value + 1.0), () -> updateValue(value - 1.0));

        wholeCounter.addStyleName("intake24-quantity-prompt-whole-counter");
        wholeCounter.getElement().setId("intake24-quantity-prompt-whole-counter");

        fractionalCounter = new LabelledCounter(getFractionLabel(), messages.quantity_fractionCaption(), () -> updateValue(value + 0.25), () -> updateValue(value - 0.25));
        fractionalCounter.addStyleName("intake24-quantity-prompt-frac-counter");
        fractionalCounter.getElement().setId("intake24-quantity-prompt-frac-counter");

        panel.add(wholeCounter);
        wholeLabel = new Label(messages.quantity_wholeItemsLabel());
        wholeLabel.addStyleName("intake24-quantity-prompt-whole-label");
        panel.add(wholeLabel);
        panel.add(fractionalCounter);

        initWidget(panel);
    }
}
