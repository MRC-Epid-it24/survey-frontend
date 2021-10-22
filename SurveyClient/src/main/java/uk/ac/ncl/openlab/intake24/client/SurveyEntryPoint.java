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

package uk.ac.ncl.openlab.intake24.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.ServiceRoots;
import uk.ac.ncl.openlab.intake24.client.api.auth.AuthCache;
import uk.ac.ncl.openlab.intake24.client.api.auth.DynamicRedirect;
import uk.ac.ncl.openlab.intake24.client.api.auth.UrlParameterConstants;
import uk.ac.ncl.openlab.intake24.client.api.survey.SurveyParameters;
import uk.ac.ncl.openlab.intake24.client.api.survey.SurveyService;
import uk.ac.ncl.openlab.intake24.client.api.survey.UserData;
import uk.ac.ncl.openlab.intake24.client.api.uxevents.UxEventsHelper;
import uk.ac.ncl.openlab.intake24.client.survey.SurveyInterfaceManager;
import uk.ac.ncl.openlab.intake24.client.survey.SurveyMessages;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.SurveyScheme;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.FollowUp;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.October2019;
import uk.ac.ncl.openlab.intake24.client.ui.ErrorPage;
import uk.ac.ncl.openlab.intake24.client.ui.Layout;
import uk.ac.ncl.openlab.intake24.client.ui.LogoutPage;
import uk.ac.ncl.openlab.intake24.client.ui.TutorialVideo;

import java.util.ArrayList;
import java.util.Map;

public class SurveyEntryPoint implements EntryPoint {
    private static int CUSTOM_DATA_REDIRECT_RECALL_NUMBER = 3; // hardcoded for now, should be moved to scheme when required

    private Anchor watchTutorial;
    private Anchor logOut;
    private Anchor recallNumber;

    private void startSurvey(SurveyParameters params, UserData userData) {

        Map<String, String> customFields = AuthCache.getCurrentUserCustomFields();

        try {
            String redirectURL = customFields.get("redirect url");

            if (redirectURL != null) {
                if (userData.recallNumber == CUSTOM_DATA_REDIRECT_RECALL_NUMBER && !redirectURL.isEmpty()) {
                    DynamicRedirect.set(Long.parseLong(AuthCache.getCurrentUserId()), redirectURL);
                } else {
                    DynamicRedirect.clear(Long.parseLong(AuthCache.getCurrentUserId()));
                }
            }
        } catch (RuntimeException e) {
            BrowserConsole.error(e.getMessage());
        }

        SurveyInterfaceManager surveyInterfaceManager = new SurveyInterfaceManager(Layout.getMainContentPanel());

        SurveyScheme scheme = SurveyScheme.createScheme(params, EmbeddedData.localeId, surveyInterfaceManager, userData);

        ArrayList<Anchor> navbarUserInfo = new ArrayList<>();
        navbarUserInfo.addAll(scheme.navBarUserInfo());
        navbarUserInfo.add(recallNumber);

        ArrayList<Anchor> navbarLinks = new ArrayList<>();
        navbarLinks.addAll(scheme.navBarLinks());
        navbarLinks.add(watchTutorial);
        navbarLinks.add(logOut);

        Layout.setNavBar(navbarUserInfo, navbarLinks);

        // scheme.showNextPage() is called from history change handler set up in StateManager constructor

        UxEventsHelper.postPageOpen();
    }

    private static void reportClientError() {
        UncaughtExceptionHandler.reportError(new RuntimeException("Test exception"));
    }

    private native void addReportClientErrorFunction() /*-{
        $wnd.__intake24_reportClientError = function () {
            @uk.ac.ncl.openlab.intake24.client.SurveyEntryPoint::reportClientError()();
        };
    }-*/;

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(UncaughtExceptionHandler.INSTANCE);

        addReportClientErrorFunction();

        // Force re-authentication on page load if using URL token to make sure the current user matches the auth token
        if (Window.Location.getParameter(UrlParameterConstants.authTokenKey) != null)
            AuthCache.clear();

        // Force the generation of new user if the page is accessed with the genUser param
        if (Window.Location.getParameter(UrlParameterConstants.generateUserKey) != null)
            AuthCache.clear();

        // Force the generation of new user if the page is accessed with the createUser param
        if (Window.Location.getParameter(UrlParameterConstants.createUserKey) != null)
            AuthCache.clear();

        RootPanel.get("loading").getElement().removeFromParent();

        Defaults.setServiceRoot("/");
        ServiceRoots.add("intake24-api", EmbeddedData.apiBaseUrl);

        watchTutorial = new Anchor(SurveyMessages.INSTANCE.navBar_tutorialVideo(), TutorialVideo.url, "_blank");

        logOut = new Anchor(SurveyMessages.INSTANCE.navBar_logOut());

        logOut.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                UxEventsHelper.postPageClose();
                AuthCache.clear();
                LogoutPage.logout();
            }
        });

        SurveyService.INSTANCE.getSurveyParameters(EmbeddedData.surveyId, new MethodCallback<SurveyParameters>() {
            @Override
            public void onFailure(Method method, Throwable exception) {

                Layout.createMainPageLayout();
                Layout.setNavBarLinks(logOut);

                switch (method.getResponse().getStatusCode()) {
                    case 403:
                        ErrorPage.showForbiddenErrorPage();
                        break;
                    default:
                        ErrorPage.showInternalServerErrorPage();
                        break;
                }
            }

            @Override
            public void onSuccess(Method method, SurveyParameters response) {

                SurveyService.INSTANCE.getUserData(EmbeddedData.surveyId, TimeZoneUtils.getTimeZone(), new MethodCallback<UserData>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {

                        Layout.createMainPageLayout();
                        Layout.setNavBarLinks(logOut);

                        switch (method.getResponse().getStatusCode()) {
                            case 403:
                                ErrorPage.showForbiddenErrorPage();
                                break;
                            default:
                                ErrorPage.showInternalServerErrorPage();
                                break;
                        }
                    }

                    @Override
                    public void onSuccess(Method method, UserData userResponse) {
                        // NDNS Y12/Followup redirect to feedback
                        boolean redirect = userResponse.redirectToFeedback &&
                                (response.schemeId.equals(October2019.ID) || response.schemeId.equals(FollowUp.ID));

                        if (redirect) {
                            UrlBuilder builder = Window.Location.createUrlBuilder();
                            for (String paramName : Window.Location.getParameterMap().keySet()) {
                                builder.removeParameter(paramName);
                            }

                            builder.setPath(Window.Location.getPath() + "/feedback").setHash("/");
                            Window.Location.replace(builder.buildString());
                        } else if (userResponse.maximumDailySubmissionsReached) {
                            Layout.createMainPageLayout();
                            Layout.setNavBarLinks(watchTutorial, logOut);

                            ErrorPage.showMaximumDailySubmissionsReached();
                        } else if (userResponse.maximumTotalSubmissionsReached) {
                            Layout.createMainPageLayout();
                            Layout.setNavBarLinks(watchTutorial, logOut);

                            ErrorPage.showMaximumTotalSubmissionsReached();
                        } else {
                            NumberFormat nf = NumberFormat.getDecimalFormat();

                            recallNumber = new Anchor(SurveyMessages.INSTANCE.navBar_currentRecallNumber(nf.format(userResponse.recallNumber)));
                            recallNumber.getElement().setId("intake24-recall-number");


                            UxEventsHelper.applySettings(response.uxEventsSettings);
                            Layout.createMainPageLayout();
                            Layout.setNavBarLinks(watchTutorial, logOut);

                            switch (response.state) {
                                case "running":
                                    startSurvey(response, userResponse);
                                    break;
                                case "pending":
                                    ErrorPage.showSurveyPendingPage();
                                    break;
                                case "finished":
                                    ErrorPage.showSurveyFinishedPage();
                                    break;
                                case "suspended":
                                    ErrorPage.showSurveySuspendedPage(response.suspensionReason);
                                    break;
                                default:
                                    ErrorPage.showInternalServerErrorPage();
                            }
                        }
                    }
                });
            }
        });
    }
}
