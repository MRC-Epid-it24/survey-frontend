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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.pcollections.PMap;
import org.pcollections.PVector;
import uk.ac.ncl.openlab.intake24.client.api.foods.DrinkwareSet;
import uk.ac.ncl.openlab.intake24.client.api.foods.FoodDataService;
import uk.ac.ncl.openlab.intake24.client.api.foods.SImageMap;


public class DrinkScaleScriptLoader implements PortionSizeScriptLoader {

    private final boolean leftovers;

    public DrinkScaleScriptLoader(boolean leftovers) {
        this.leftovers = leftovers;
    }

    @Override
    public void loadResources(PMap<String, String> data, final AsyncCallback<PortionSizeScript> onComplete) {

        FoodDataService.INSTANCE.getDrinkwareSet(data.get("drinkware-id"), new MethodCallback<DrinkwareSet>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                onComplete.onFailure(exception);
            }

            @Override
            public void onSuccess(Method method, DrinkwareSet drinkwareSet) {
                FoodDataService.INSTANCE.getImageMap(drinkwareSet.guideId, new MethodCallback<SImageMap>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        onComplete.onFailure(exception);

                    }

                    @Override
                    public void onSuccess(Method method, SImageMap imageMap) {
                        onComplete.onSuccess(new DrinkScaleScript(imageMap.toImageMap(), drinkwareSet, leftovers));
                    }

                });
            }
        });
    }
}