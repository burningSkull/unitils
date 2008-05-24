/*
 * Copyright 2006-2007,  Unitils.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.reflectionassert.comparator.impl;

import org.unitils.reflectionassert.ReflectionComparator;
import static org.unitils.reflectionassert.ReflectionComparatorFactory.createRefectionComparator;
import org.unitils.reflectionassert.comparator.Comparator;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.difference.MapDifference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Comparator for maps. This will compare all values with corresponding keys.
 *
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class MapComparator implements Comparator {


    /**
     * Returns true when both values are not null and instance of Map
     *
     * @param left  The left object
     * @param right The right object
     * @return True for maps
     */
    public boolean canCompare(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if ((left instanceof Map && right instanceof Map)) {
            return true;
        }
        return false;
    }


    /**
     * Compares the given maps by looping over the keys and comparing their values.
     * The key values are compared using a strict reflection comparison.
     *
     * @param left                 The left map, not null
     * @param right                The right map, not null
     * @param onlyFirstDifference  True if only the first difference should be returned
     * @param reflectionComparator The root comparator for inner comparisons, not null
     * @return A MapDifference or null if both maps are equal
     */
    public Difference compare(Object left, Object right, boolean onlyFirstDifference, ReflectionComparator reflectionComparator) {
        Map<?, ?> leftMap = (Map<?, ?>) left;
        Map<?, ?> rightMap = (Map<?, ?>) right;

        // Create copy from which we can remove elements.
        Map<Object, Object> rightCopy = new HashMap<Object, Object>(rightMap);

        ReflectionComparator keyReflectionComparator = createRefectionComparator();
        MapDifference difference = new MapDifference("Different elements", left, right);

        for (Map.Entry<?, ?> leftEntry : leftMap.entrySet()) {
            Object leftKey = leftEntry.getKey();
            Object leftValue = leftEntry.getValue();

            boolean found = false;
            Iterator<Map.Entry<Object, Object>> rightIterator = rightCopy.entrySet().iterator();
            while (rightIterator.hasNext()) {
                Map.Entry<Object, Object> rightEntry = rightIterator.next();
                Object rightKey = rightEntry.getKey();
                Object rightValue = rightEntry.getValue();

                // compare keys using strict reflection compare
                boolean isKeyEqual = keyReflectionComparator.isEqual(leftKey, rightKey);
                if (isKeyEqual) {
                    found = true;
                    rightIterator.remove();

                    // compare values
                    Difference elementDifference = reflectionComparator.getDifference(leftValue, rightValue, onlyFirstDifference);
                    if (elementDifference != null) {
                        difference.addValueDifference(leftKey, elementDifference);
                        if (onlyFirstDifference) {
                            return difference;
                        }
                    }
                    break;
                }
            }

            if (!found) {
                difference.addValueDifference(leftKey, new Difference("Left element not found in right map", leftValue, null));
            }
        }

        for (Map.Entry<?, ?> rightEntry : rightCopy.entrySet()) {
            Object rightKey = rightEntry.getKey();
            Object rightValue = rightEntry.getValue();
            difference.addValueDifference(rightKey, new Difference("Right element not found in left map", null, rightValue));
        }

        if (difference.getValueDifferences().isEmpty()) {
            return null;
        }
        return difference;
    }
}
