/*
This file is part of Intake24.

Copyright 2015, 2016, 2017 Newcastle University.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This file is based on Intake24 v1.0.

© Crown copyright, 2012, 2013, 2014

Licensed under the Open Government Licence 3.0: 

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.ui;


import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.workcraft.gwt.shared.client.Callback1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.CommonMessages;
import uk.ac.ncl.openlab.intake24.client.EmbeddedData;
import uk.ac.ncl.openlab.intake24.client.api.auth.Credentials;

public class LoginForm extends Composite {

    final private static CommonMessages messages = CommonMessages.Util.getInstance();

    final private TextBox userNameTextBox;
    final private PasswordTextBox passwordTextBox;
    final private Callback1<Credentials> attemptLogin;
    final private FlowPanel statusPanel;
    final private Button loginButton;

    private void doLoginAttempt() {
        loginButton.setEnabled(false);

        statusPanel.clear();
        statusPanel.add(new LoadingWidget());

        attemptLogin.call(new Credentials(EmbeddedData.surveyId, userNameTextBox.getText(), passwordTextBox.getText()));
    }

    public void onLoginAttemptFailed() {
        statusPanel.clear();
        statusPanel.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.loginForm_passwordNotRecognised(SafeHtmlUtils.htmlEscape(EmbeddedData.surveySupportEmail)))));
        loginButton.setEnabled(true);
    }

    public void onLoginServiceError() {
        statusPanel.clear();
        statusPanel.add(new HTMLPanel(SafeHtmlUtils.fromSafeConstant(messages.loginForm_serviceException(SafeHtmlUtils.htmlEscape(EmbeddedData.surveySupportEmail)))));
        loginButton.setEnabled(true);
    }

    public LoginForm(final Callback1<Credentials> attemptLogin, boolean sessionExpired) {
        this.attemptLogin = attemptLogin;

        FlowPanel container = new FlowPanel();
        container.getElement().addClassName("intake24-login-container");

        FlowPanel header = new FlowPanel();
        header.getElement().addClassName("intake24-login-header");

        FlowPanel formContainer = new FlowPanel();
        formContainer.getElement().addClassName("intake24-login-form-container");

        HTMLPanel welcome = new HTMLPanel("h1", messages.loginForm_welcome());
        header.add(welcome);

        Grid credentials = new Grid(2, 2);

        credentials.setCellPadding(5);

        Label userLabel = new Label(messages.loginForm_userNameLabel());
        Label passLabel = new Label(messages.loginForm_passwordLabel());

        this.userNameTextBox = new TextBox();
        this.passwordTextBox = new PasswordTextBox();

        credentials.setWidget(0, 0, userLabel);
        credentials.setWidget(1, 0, passLabel);
        credentials.setWidget(0, 1, userNameTextBox);
        credentials.setWidget(1, 1, passwordTextBox);

        VerticalPanel form = new VerticalPanel();
        form.getElement().addClassName("login-form");
        form.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        FlowPanel linkPanel = new FlowPanel();

        linkPanel.add(WidgetFactory.createTutorialVideoLink());

        HTMLPanel messagePanel = new HTMLPanel("h1", sessionExpired ? messages.loginForm_sessionExpired() : messages.loginForm_logInToContinue());

        HTMLPanel divider = new HTMLPanel(messages.loginForm_logInSeparator());
        divider.getElement().addClassName("intake24-login-form-divider");

        form.add(messagePanel);
        form.add(divider);
        form.add(linkPanel);
        form.add(credentials);

        statusPanel = new FlowPanel();
        form.add(statusPanel);

        loginButton = WidgetFactory.createButton(messages.loginForm_logInButtonLabel(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                doLoginAttempt();
            }
        });

        loginButton.getElement().addClassName("intake24-login-button");

        passwordTextBox.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
                    doLoginAttempt();
            }
        });

        userNameTextBox.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
                    doLoginAttempt();
            }
        });
        userNameTextBox.getElement().setId("username-input");
        passwordTextBox.getElement().setId("password-input");
        userNameTextBox.setName("username");
        passwordTextBox.setName("password");

        form.add(WidgetFactory.createButtonsPanel(loginButton));
        form.getElement().addClassName("intake24-login-form");

        container.add(header);
        formContainer.add(form);
        container.add(formContainer);

        initWidget(container);
    }
}