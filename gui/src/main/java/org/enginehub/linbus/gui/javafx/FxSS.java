/*
 * Copyright (c) EngineHub <https://enginehub.org>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.enginehub.linbus.gui.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class FxSS {
    private static final String OBSERVABLE_CLASS_KEY = FxSS.class.getName() + ".observablesClasses";

    public static void addObservableClass(Node node, ObservableValue<@Nullable String> styleClass) {
        ChangeListener<@Nullable String> listener = (observable, oldValue, newValue) -> {
            if (oldValue != null) {
                node.getStyleClass().remove(oldValue);
            }
            if (newValue != null) {
                node.getStyleClass().add(newValue);
            }
        };
        styleClass.addListener(listener);
        // Also apply now
        String value = styleClass.getValue();
        if (value != null) {
            node.getStyleClass().add(value);
        }
        // Need to keep a reference to the style observable to prevent it from being garbage collected
        @SuppressWarnings("unchecked")
        var styleClassObservables = (Map<ObservableValue<String>, ChangeListener<String>>) node.getProperties().computeIfAbsent(
            OBSERVABLE_CLASS_KEY, k -> new IdentityHashMap<>()
        );
        styleClassObservables.put(styleClass, listener);
    }

    public static void removeObservableClass(Node node, ObservableValue<@Nullable String> styleClass) {
        @SuppressWarnings("unchecked")
        var styleClassObservables = (Map<ObservableValue<String>, ChangeListener<String>>) node.getProperties().get(OBSERVABLE_CLASS_KEY);
        if (styleClassObservables != null) {
            ChangeListener<String> listener = styleClassObservables.remove(styleClass);
            styleClass.removeListener(listener);
        }
    }
}
