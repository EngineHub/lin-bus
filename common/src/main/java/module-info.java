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

/**
 * The common module of lin-bus, shared between the streaming and tree modules.
 */
module org.enginehub.linbus.common {
    exports org.enginehub.linbus.common;
    exports org.enginehub.linbus.common.internal to org.enginehub.linbus.stream, org.enginehub.linbus.tree,
        org.enginehub.linbus.format.snbt;
    requires static org.jetbrains.annotations;
}
