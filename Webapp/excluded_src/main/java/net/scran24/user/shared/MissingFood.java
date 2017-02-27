/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
 */

package net.scran24.user.shared;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.workcraft.gwt.shared.client.Option;

public class MissingFood extends FoodEntry {

	public static final String KEY_ASSOC_FOOD_NAME = "assocFoodName";
	public static final String KEY_PROMPT_TEXT = "promptText";
	public static final String KEY_ASSOC_FOOD_CATEGORY = "assocFoodCategory";
	public static final String KEY_DESCRIPTION = "missingFoodDescription";
	public static final String KEY_PORTION_SIZE = "missingFoodPortionSize";
	public static final String KEY_LEFTOVERS = "missingFoodLeftovers";

	public static final String HOME_RECIPE_FLAG = "home-recipe";
	public static final String NOT_HOME_RECIPE_FLAG = "not-home-recipe";

	public final String name;
	public final Option<MissingFoodDescription> description;
	public final boolean isDrink;

	public MissingFood(FoodLink link, String name, boolean isDrink) {
		super(link, HashTreePSet.<String> empty(), HashTreePMap.<String, String> empty());
		this.name = name;
		this.isDrink = isDrink;
		this.description = Option.none();
	}

	public MissingFood(FoodLink link, String name, boolean isDrink, Option<MissingFoodDescription> description) {
		super(link, HashTreePSet.<String> empty(), HashTreePMap.<String, String> empty());
		this.name = name;
		this.isDrink = isDrink;
		this.description = description;
	}

	public MissingFood(FoodLink link, String name, boolean isDrink, Option<MissingFoodDescription> description, PSet<String> flags,
			PMap<String, String> customData) {
		super(link, flags, customData);
		this.name = name;
		this.isDrink = isDrink;
		this.description = description;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitMissing(this);
	}

	@Override
	public FoodEntry relink(FoodLink link) {
		return new MissingFood(link, name, isDrink, description, flags, customData);
	}

	@Override
	public MissingFood withFlag(String flag) {
		return withFlags(flags.plus(flag));
	}

	@Override
	public MissingFood withFlags(PSet<String> new_flags) {
		return new MissingFood(link, name, isDrink, description, new_flags, customData);
	}

	@Override
	public MissingFood withCustomDataField(String key, String value) {
		return new MissingFood(link, name, isDrink, description, flags, customData.plus(key, value));
	}

	public MissingFood withDescription(Option<MissingFoodDescription> description) {
		return new MissingFood(link, name, isDrink, description, flags, customData);
	}

	@Override
	public String description() {
		return name;
	}

	@Override
	public boolean isDrink() {
		return isDrink;
	}

	public boolean isDescriptionComplete() {
		return !description.isEmpty();
	}
}