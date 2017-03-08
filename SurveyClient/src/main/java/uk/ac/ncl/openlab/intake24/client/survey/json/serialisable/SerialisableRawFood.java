/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey.json.serialisable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import uk.ac.ncl.openlab.intake24.client.survey.RawFood;

import java.util.Map;
import java.util.Set;

@JsonTypeName("raw")
public class SerialisableRawFood extends SerialisableFoodEntry {

    @JsonProperty
    public final String description;

    @JsonCreator
    public SerialisableRawFood(@JsonProperty("link") SerialisableFoodLink link,
                               @JsonProperty("description") String description,
                               @JsonProperty("flags") Set<String> flags,
                               @JsonProperty("customData") Map<String, String> customData) {
        super(link, HashTreePSet.from(flags), HashTreePMap.from(customData));
        this.description = description;
    }

    public SerialisableRawFood(RawFood food) {
        super(new SerialisableFoodLink(food.link), food.flags, food.customData);
        this.description = food.description;
    }

    public RawFood toRawFood() {
        return new RawFood(link.toFoodLink(), description, flags, customData);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitRaw(this);
    }
}
