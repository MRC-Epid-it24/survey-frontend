/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client;

public class BrowserWindow {
	public static native void postMessage(String msg) /*-{
		$wnd.parent.postMessage(msg, "*");
	}-*/;

	public static native void alert(String msg) /*-{
		$wnd.alert(msg);
	}-*/;
<<<<<<< HEAD
<<<<<<< HEAD
}
=======
}
>>>>>>> support/rhel
=======
}
>>>>>>> ABS_05.2022
