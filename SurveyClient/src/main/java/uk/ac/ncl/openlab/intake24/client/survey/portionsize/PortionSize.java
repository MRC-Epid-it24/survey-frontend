/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey.portionsize;

import org.pcollections.PMap;
import org.workcraft.gwt.shared.client.Either;
import org.workcraft.gwt.shared.client.Option;
import uk.ac.ncl.openlab.intake24.client.survey.CompletedPortionSize;

public class PortionSize {
    public final String scriptName;
    public final double conversionFactor;
    public final PMap<String, String> data;
    public final PortionSizeScriptLoader script;
    public final Option<PortionSizeScript> loadedScript;

    public PortionSize(String scriptName, double conversionFactor, PMap<String, String> data, PortionSizeScriptLoader script, Option<PortionSizeScript> loadedScript) {
        this.scriptName = scriptName;
        this.conversionFactor = conversionFactor;
        this.data = data;
        this.script = script;
        this.loadedScript = loadedScript;
    }

    public PortionSize(String scriptName, double conversionFactor, PMap<String, String> data, PortionSizeScriptLoader script) {
        this(scriptName, conversionFactor, data, script, Option.<PortionSizeScript>none());
    }

    public PortionSize updateData(UpdateFunc updateFunc) {
        return new PortionSize(scriptName, conversionFactor, updateFunc.apply(data), script, loadedScript);
    }

    public PortionSize withLoadedScript(PortionSizeScript loadedScript) {
        return new PortionSize(scriptName, conversionFactor, data, script, Option.some(loadedScript));
    }

    public PortionSize withData(PMap<String, String> data) {
        return new PortionSize(scriptName, conversionFactor, data, script, loadedScript);
    }

    public static Either<PortionSize, CompletedPortionSize> incomplete(PortionSize size) {
        return new Either.Left<PortionSize, CompletedPortionSize>(size);
    }

    public static Either<PortionSize, CompletedPortionSize> complete(CompletedPortionSize size) {
        return new Either.Right<PortionSize, CompletedPortionSize>(size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PortionSize other = (PortionSize) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (scriptName == null) {
            if (other.scriptName != null)
                return false;
        } else if (!scriptName.equals(other.scriptName))
            return false;
        return true;
    }
}