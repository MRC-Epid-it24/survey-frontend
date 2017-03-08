/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey;

import org.workcraft.gwt.shared.client.Option;

/**
 * Selects next prompt to display to the user based on the current survey state.
 */
public interface PromptManager {
	public Option<Prompt<Survey, SurveyOperation>> nextPromptForSelection(final Survey state);
	public Option<Prompt<Survey, SurveyOperation>> nextGlobalPrompt(final Survey state);
}
