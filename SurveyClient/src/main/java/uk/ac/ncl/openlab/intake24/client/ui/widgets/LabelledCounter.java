/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.ui.widgets;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.workcraft.gwt.shared.client.Callback;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;


public class LabelledCounter extends Composite {
    public int index;

    public final Label label = new Label();
    public final Label captionLabel = new Label();

    public LabelledCounter(final String initialLabel, final String captionLabelText, final Callback onIncreaseClicked, final Callback onDecreaseClicked) {
        VerticalPanel panel = new VerticalPanel();

        panel.setStyleName("counterPanel");

        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

        Button inc = WidgetFactory.createButton("▲", event -> {
            event.preventDefault();
            onIncreaseClicked.call();
        });

        inc.setStyleName("counterIncButton");

        Button dec = WidgetFactory.createButton("▼", event -> {
            event.preventDefault();
            onDecreaseClicked.call();
        });

        dec.setStyleName("counterDecButton");

        label.setStyleName("counterTextBox");
        label.getElement().getStyle().setFontSize(100, Unit.PCT);
        label.getElement().getStyle().setHeight(1.2, Unit.EM);
        captionLabel.setStyleName("counterCaptionTextBox");
        captionLabel.getElement().getStyle().setFontSize(80, Unit.PCT);

        label.setText(initialLabel);
        captionLabel.setText(captionLabelText);

        panel.add(inc);
        panel.add(label);
        panel.add(dec);
        panel.add(captionLabel);
        initWidget(panel);
    }

    public void setLabel(String label) {
        this.label.setText(label);
    }
}
