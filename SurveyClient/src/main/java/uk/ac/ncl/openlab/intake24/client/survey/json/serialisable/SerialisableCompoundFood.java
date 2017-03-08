/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
 */

package uk.ac.ncl.openlab.intake24.client.survey.json.serialisable;

import java.util.Map;
import java.util.Set;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import uk.ac.ncl.openlab.intake24.client.survey.CompoundFood;

@JsonTypeName("compound")
public class SerialisableCompoundFood extends SerialisableFoodEntry {
	
	@JsonProperty
	public final String description;
	@JsonProperty
	public final boolean isDrink;

	@JsonCreator
	public SerialisableCompoundFood(@JsonProperty("link") SerialisableFoodLink link, @JsonProperty("description") String description,
			@JsonProperty("isDrink") boolean isDrink, @JsonProperty("flags") Set<String> flags,
			@JsonProperty("customData") Map<String, String> customData) {
		super(link, HashTreePSet.from(flags), HashTreePMap.from(customData));
		this.description = description;
		this.isDrink = isDrink;
	}
	
	public SerialisableCompoundFood(CompoundFood food) {
		super(new SerialisableFoodLink(food.link), food.flags, food.customData);
		this.description = food.description;
		this.isDrink = food.isDrink;
	}
	
	public CompoundFood toCompoundFood() {
		return new CompoundFood(link.toFoodLink(), description, isDrink, flags, customData);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitCompound(this);
	}
}
