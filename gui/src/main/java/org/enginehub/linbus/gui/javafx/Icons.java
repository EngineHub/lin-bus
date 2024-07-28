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

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinTag;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Icons {
    private static final Map<LinTagId, Image> IMAGE_CACHE = new ConcurrentHashMap<>(LinTagId.values().length);

    public static Node iconForTag(LinTag<?> tag) {
        ImageView view = new ImageView(
            IMAGE_CACHE.computeIfAbsent(tag.type().id(), Icons::initImage)
        );
        view.setFitHeight(16);
        view.setFitWidth(16);
        return view;
    }

    private static Image initImage(LinTagId linTagId) {
        URL resource = Icons.class.getResource(
            "/nbt/" + linTagId.name().toLowerCase().replace('_', '-') + ".png"
        );
        if (resource == null) {
            throw new IllegalStateException("No image found for " + linTagId);
        }
        try {
            return new Image(resource.toURI().toASCIIString());
        } catch (URISyntaxException e) {
            throw new AssertionError("JDK bug if the URI is not valid", e);
        }
    }
}
