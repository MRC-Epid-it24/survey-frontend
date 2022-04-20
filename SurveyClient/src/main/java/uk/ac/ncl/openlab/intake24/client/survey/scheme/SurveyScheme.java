/*
This file is part of Intake24.

Copyright 2015, 2016 Newcastle University.

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

package uk.ac.ncl.openlab.intake24.client.survey.scheme;

import com.google.gwt.user.client.ui.Anchor;
import uk.ac.ncl.openlab.intake24.client.api.survey.SurveyParameters;
import uk.ac.ncl.openlab.intake24.client.api.survey.UserData;
import uk.ac.ncl.openlab.intake24.client.survey.Survey;
import uk.ac.ncl.openlab.intake24.client.survey.SurveyInterfaceManager;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.birmingham.BirminghamNovember2019;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.April2019;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.FollowUp;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.NDNSDefault;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.October2019;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.ndns.October2019Behaviour;
import uk.ac.ncl.openlab.intake24.client.survey.scheme.sab.SAB;

import java.util.List;

public interface SurveyScheme {
    String getSchemeId();

    Boolean getSurveyExpired(Survey survey);

    String getDataVersion();

    void showNextPage();

    List<Anchor> navBarLinks();

    List<Anchor> navBarUserInfo();

    static SurveyScheme createScheme(SurveyParameters surveyParameters, final String locale, final SurveyInterfaceManager interfaceManager, UserData userData) {

        switch (surveyParameters.schemeId) {
            case DefaultScheme.ID:
                return new DefaultScheme(surveyParameters, locale, interfaceManager, userData);
            case ExperimentalPARulesScheme.ID:
                return new ExperimentalPARulesScheme(surveyParameters, locale, interfaceManager, userData);
            case ExperimentalPopularityScheme.ID:
                return new ExperimentalPopularityScheme(surveyParameters, locale, interfaceManager, userData);
            case ExperimentalFlexibleRecallScheme.ID:
                return new ExperimentalFlexibleRecallScheme(surveyParameters, locale, interfaceManager, userData);
            case SHeSJun15.ID:
                return new SHeSJun15(locale, surveyParameters, interfaceManager, userData);
            // NDNS Schemes
            case NDNSDefault.ID:
                return new NDNSDefault(locale, surveyParameters, interfaceManager, userData);
            case April2019.ID:
                return new April2019(locale, surveyParameters, interfaceManager, userData);
            case October2019.ID:
                return new October2019(locale, surveyParameters, interfaceManager, userData);
            case October2019Behaviour.ID:
                return new October2019Behaviour(locale, surveyParameters, interfaceManager, userData);
            case FollowUp.ID:
                return new FollowUp(locale, surveyParameters, interfaceManager, userData);
            // SAB Schemes
            case SAB.ID:
                return new SAB(locale, surveyParameters, interfaceManager, userData);
            case BirminghamNovember2019.ID:
                return new BirminghamNovember2019(locale, surveyParameters, interfaceManager, userData);
            default:
                throw new RuntimeException("Unknown survey scheme: " + surveyParameters.schemeId);
        }
    }
}
