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

package uk.ac.ncl.openlab.intake24.client.survey.portionsize;

import org.pcollections.PMap;
import org.workcraft.gwt.shared.client.Option;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import uk.ac.ncl.openlab.intake24.client.api.foods.AsServedDef;

public class AsServedScriptLoader implements PortionSizeScriptLoader {
    // private final FoodLookupServiceAsync lookupService = FoodLookupServiceAsync.Util.getInstance();

    private final static String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();

    @Override
    public void loadResources(final PMap<String, String> data, final AsyncCallback<PortionSizeScript> onComplete) {

        throw new RuntimeException("Not implemented");
        /*
		lookupService.getAsServedDef(data.get("serving-image-set"), currentLocale, new AsyncCallback<AsServedDef>() {
			@Override
			public void onFailure(Throwable caught) {
				onComplete.onFailure(caught);
			}

			@Override
			public void onSuccess(final AsServedDef servingImages) {
				if (data.containsKey("leftovers-image-set"))
					lookupService.getAsServedDef(data.get("leftovers-image-set"), currentLocale, new AsyncCallback<AsServedDef>() {
						@Override
						public void onFailure(Throwable caught) {
							onComplete.onFailure(caught);
						}

						@Override
						public void onSuccess(AsServedDef leftoversImages) {
							onComplete.onSuccess(new AsServedScript(servingImages, Option.some(leftoversImages)));
						}
					});
				else
					onComplete.onSuccess(new AsServedScript(servingImages, Option.<AsServedDef>none()));
			}
		});*/
    }
}
