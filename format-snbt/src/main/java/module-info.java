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
 * The tree module of lin-bus. Presents a standard tree structure of NBT. Internally, everything is parsed using the
 * streaming module.
 */
module org.enginehub.linbus.format.snbt {
    exports org.enginehub.linbus.format.snbt;
    requires static org.jetbrains.annotations;
    requires org.enginehub.linbus.common;
    requires org.enginehub.linbus.stream;
}
