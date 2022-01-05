/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.workcraft.gwt.shared.client.Callback;
import uk.ac.ncl.openlab.intake24.client.api.help.CallbackRequest;
import uk.ac.ncl.openlab.intake24.client.api.help.HelpService;
import uk.ac.ncl.openlab.intake24.client.ui.LoadingWidget;
import uk.ac.ncl.openlab.intake24.client.ui.OverlayDiv;
import uk.ac.ncl.openlab.intake24.client.ui.WidgetFactory;

public class CallbackRequestForm extends Composite {
    private final static CommonMessages messages = CommonMessages.Util.getInstance();
    // private final static HelpServiceAsync helpService = HelpServiceAsync.Util.getInstance();

    final private TextBox nameTextBox;
    final private TextBox phoneNumberTextBox;

    final private FlowPanel errorMessage;
    final private Button requestCallbackButton;
    final private Button hideFormButton;

    private void doRequest() {
        requestCallbackButton.setEnabled(false);

        errorMessage.clear();

        if (nameTextBox.getText().isEmpty() || phoneNumberTextBox.getText().isEmpty()) {
            errorMessage.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.callbackRequestForm_fieldsEmpty())));
            errorMessage.getElement().addClassName("intake24-login-error-message");
            requestCallbackButton.setEnabled(true);
            return;
        }

        if (EmbeddedData.surveyId.equals("demo")) {
            errorMessage.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.callbackRequestForm_disabledForDemo(EmbeddedData.surveySupportEmail))));
            errorMessage.getElement().addClassName("intake24-login-error-message");
            requestCallbackButton.setEnabled(true);
            return;
        }

        errorMessage.add(new LoadingWidget());

        HelpService.INSTANCE.requestCallback(EmbeddedData.surveyId, new CallbackRequest(nameTextBox.getText(), phoneNumberTextBox.getText()), new MethodCallback<Void>() {
            @Override
            public void onFailure(Method method, Throwable exception) {

                if (method.getResponse().getStatusCode() == 429) {
                    errorMessage.clear();
                    errorMessage.getElement().addClassName("intake24-login-error-message");
                    errorMessage.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.callbackRequestForm_tooSoon())));

                    GoogleAnalytics.trackHelpCallbackRejected();
                } else {
                    errorMessage.clear();
                    errorMessage.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.serverErrorTitle())));
                    errorMessage.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.serverErrorText(EmbeddedData.surveySupportEmail))));
                    errorMessage.getElement().addClassName("intake24-login-error-message");
                    requestCallbackButton.setEnabled(true);
                }
            }

            @Override
            public void onSuccess(Method method, Void response) {
                errorMessage.clear();
                errorMessage.getElement().addClassName("intake24-login-success-message");
                errorMessage.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.callbackRequestForm_success())));

                GoogleAnalytics.trackHelpCallbackAccepted();
            }
        });
    }

    public CallbackRequestForm(final Callback onComplete) {
        Grid g = new Grid(2, 2);

        g.setCellPadding(5);
        Label nameLabel = new Label(messages.callbackRequestForm_nameLabel());
        Label phoneNumberLabel = new Label(messages.callbackRequestForm_phoneNumberLabel());

        this.nameTextBox = new TextBox();
        this.phoneNumberTextBox = new TextBox();

        // Hack for ABS need more elegant solution wih conditional styles and build configurations
        //g.setWidget(0, 0, nameLabel); //ABS
        g.setWidget(1, 0, phoneNumberLabel);
        //g.setWidget(0, 1, nameTextBox); //ABS
        g.setWidget(1, 1, phoneNumberTextBox);
        g.removeRow(1); //ABS

        VerticalPanel p = new VerticalPanel();
        p.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        FlowPanel videoLinkDiv = new FlowPanel();
        videoLinkDiv.add(WidgetFactory.createTutorialVideoLink());

        p.add(new HTMLPanel(messages.callbackRequestForm_watchWalkthrough()));
        p.add(videoLinkDiv);

        HTMLPanel pp = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.callbackRequestForm_promptText()));
        pp.getElement().addClassName("intake24-login-prompt-text");
        p.add(pp);
        p.add(g);

        errorMessage = new FlowPanel();
        p.add(errorMessage);

        requestCallbackButton = WidgetFactory.createButton(messages.callbackRequestForm_requestCallbackButtonLabel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doRequest();
            }
        });

        hideFormButton = WidgetFactory.createButton(messages.callbackRequestForm_hideButtonLabel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onComplete.call();
            }
        });

        requestCallbackButton.getElement().setId("intake24-login-button");
        // Hack for ABS need more elegant solution wih conditional styles and build configuration
        requestCallbackButton.getElement().getStyle().setDisplay(Style.Display.NONE);

        nameTextBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
                    doRequest();
            }
        });

        phoneNumberTextBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
                    doRequest();
            }
        });

        p.add(WidgetFactory.createButtonsPanel(requestCallbackButton, hideFormButton));
        p.addStyleName("intake24-login-form");

        initWidget(p);
    }

    public static void showPopup() {
        final OverlayDiv div = new OverlayDiv();

        CallbackRequestForm dialog = new CallbackRequestForm(new Callback() {

            @Override
            public void call() {
                div.setVisible(false);
            }
        });

        dialog.addStyleName("intake24-login-popup");

        div.setContents(dialog);
        div.setVisible(true);
    }

}