/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey.prompts;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.pcollections.PVector;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.BrowserConsole;
import uk.ac.ncl.openlab.intake24.client.UnorderedList;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;

import java.util.ArrayList;

public abstract class MultipleChoiceQuestion<T> extends Composite {

    protected static class OptionElements {
        public final int index;
        public final CheckBox checkBox;
        public final Option<TextBox> textBox;

        public OptionElements(int index, CheckBox checkBox, Option<TextBox> textBox) {
            this.index = index;
            this.checkBox = checkBox;
            this.textBox = textBox;
        }
    }

    final protected ArrayList<OptionElements> optionElements;
    final protected FlowPanel contents;

    final public FlowPanel promptPanel;
    final public UnorderedList<Widget> optionList;

    final protected FlowPanel warningDiv;

    public void clearWarning() {
        warningDiv.clear();
    }

    public void showWarning() {
        warningDiv.clear();
        warningDiv.add(new Label("Please answer this question before continuing"));
        contents.getElement().scrollIntoView();
    }

    public void selectFirst() {
        optionElements.get(0).checkBox.setValue(true);
    }

    protected abstract CheckBox createCheckBox(SafeHtml label, String value);

    public abstract Option<T> getAnswer();

    public MultipleChoiceQuestion(SafeHtml promptText, PVector<MultipleChoiceQuestionOption> options) {

        optionList = new UnorderedList<>();
        optionList.getElement().setId("intake24-mcq-options");

        warningDiv = new FlowPanel();
        warningDiv.getElement().addClassName("intake24-mcq-warning");

        optionElements = new ArrayList<>();

        int optionIndex = 0;

        for (MultipleChoiceQuestionOption option : options) {
            CheckBox checkBox = createCheckBox(SafeHtmlUtils.fromSafeConstant(option.label), option.value.getOrElse(option.label));

            checkBox.setFormValue(option.value.getOrElse(option.label));

            if (option.hasTextBox) {
                TextBox textBox = new TextBox();
                textBox.setMaxLength(500);
                textBox.addFocusHandler(event -> checkBox.setValue(true));
                textBox.addKeyUpHandler(event -> clearWarning());

                FlowPanel optionDiv = new FlowPanel();
                optionDiv.add(checkBox);
                optionDiv.add(textBox);
                optionList.addItem(optionDiv);
                optionElements.add(new OptionElements(optionIndex, checkBox, Option.some(textBox)));
            } else {
                optionList.addItem(checkBox);
                optionElements.add(new OptionElements(optionIndex, checkBox, Option.none()));
            }

            ++optionIndex;
        }

        contents = new FlowPanel();
        contents.addStyleName("intake24-multiple-choice-question");

        promptPanel = WidgetFactory.createPromptPanel(promptText);
        promptPanel.getElement().setId("intake24-mcq-prompt");
        contents.add(promptPanel);
        contents.add(warningDiv);
        contents.add(optionList);

        initWidget(contents);
    }
}