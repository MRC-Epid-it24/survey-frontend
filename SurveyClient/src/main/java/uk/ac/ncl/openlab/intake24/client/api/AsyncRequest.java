/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.api;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AsyncRequest<T> {
	void execute(AsyncCallback<T> callback);
}
