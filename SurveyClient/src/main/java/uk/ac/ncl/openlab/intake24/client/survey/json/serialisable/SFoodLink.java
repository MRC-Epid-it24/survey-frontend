/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey.json.serialisable;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.survey.FoodLink;
import uk.ac.ncl.openlab.intake24.client.survey.UUID;

public class SFoodLink {
    @JsonProperty
    public final String id;
    @JsonProperty
    public final Option<String> linkedTo;

    @JsonCreator
    public SFoodLink(@JsonProperty("id") String id, @JsonProperty("linkedTo") Option<String> linkedTo) {
        this.id = id;
        this.linkedTo = linkedTo;
    }

    public SFoodLink(FoodLink link) {
        this(link.id.value, link.linkedTo.map(new Function1<UUID, String>() {
            @Override
            public String apply(UUID argument) {
                return argument.value;
            }
        }));
    }

    public FoodLink toFoodLink() {
        return new FoodLink(new UUID(id), linkedTo.map(new Function1<String, UUID>() {
            @Override
            public UUID apply(String argument) {
                return new UUID(argument);
            }
        }));
    }
}
