/*
This file is part of Intake24.

© Crown copyright, 2012, 2013, 2014.

This software is licensed under the Open Government Licence 3.0:

http://www.nationalarchives.gov.uk/doc/open-government-licence/
*/

package uk.ac.ncl.openlab.intake24.client.survey;

import org.pcollections.PSet;
import org.pcollections.PVector;
import org.workcraft.gwt.shared.client.Function1;
import org.workcraft.gwt.shared.client.Option;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.workcraft.gwt.shared.client.CollectionUtils.*;

import uk.ac.ncl.openlab.intake24.client.BrowserWindow;

public class DefaultPromptGenerator<T, Op> implements PromptGenerator<T, Op> {
    private final Logger log = Logger.getLogger("DefaultPromptGenerator");
    private final PVector<WithPriority<PromptRule<T, Op>>> sortedRules;

    public DefaultPromptGenerator(PVector<WithPriority<PromptRule<T, Op>>> rules) {
        this.sortedRules = sort(rules, WithPriority.<PromptRule<T, Op>>comparator());
    }

    @Override
    public Option<WithPriority<Prompt<T, Op>>> nextPrompt(final T state, final Selection selection, final PSet<String> surveyFlags) {
        PVector<Option<WithPriority<Prompt<T, Op>>>> allRules = map(sortedRules,
                new Function1<WithPriority<PromptRule<T, Op>>, Option<WithPriority<Prompt<T, Op>>>>() {
                    @Override
                    public Option<WithPriority<Prompt<T, Op>>> apply(final WithPriority<PromptRule<T, Op>> rule) {
                        Option<WithPriority<Prompt<T, Op>>> promptOption = rule.value.apply(state, selection.selectionMode, surveyFlags).map(
                                new Function1<Prompt<T, Op>, WithPriority<Prompt<T, Op>>>() {
                                    @Override
                                    public WithPriority<Prompt<T, Op>> apply(Prompt<T, Op> p) {
                                        return new WithPriority<Prompt<T, Op>>(p, rule.priority);
                                    }
                                });

						/* if (promptOption.isEmpty())
                            log.log(Level.INFO, "Rule " + rule.toString() + " N");
						else
							log.log(Level.INFO, "Rule" + rule.toString() + " Y"); */

                        return promptOption;
                    }
                });

        log.fine("Choosing next prompt for selection " + selection.toString());

        PVector<WithPriority<Prompt<T, Op>>> applicable = flattenOption(allRules);

        if (applicable.isEmpty()) {
            log.fine("No prompt available for current selection");
            return new Option.None<WithPriority<Prompt<T, Op>>>();
        } else {
            log.fine("Chosen prompt: " + applicable.get(0));
            BrowserWindow.postMessage("Switched to the next prompt: " + applicable.get(0));
            return new Option.Some<WithPriority<Prompt<T, Op>>>(applicable.get(0));
        }
    }
}