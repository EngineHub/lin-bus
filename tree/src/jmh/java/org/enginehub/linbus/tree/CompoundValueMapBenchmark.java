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

package org.enginehub.linbus.tree;

import org.jspecify.annotations.Nullable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CompoundValueMapBenchmark {
    public enum MapType {
        // Add your modified map here for testing.
        COMPOUND_VALUE_HASH_MAP,
        COMPOUND_VALUE_LINEAR_MAP,
        UNMODIFIABLE_LINKED_HASH_MAP,
        ;

        public Map<String, LinTag<?>> create(Map<String, LinTag<?>> source) {
            return switch (this) {
                case COMPOUND_VALUE_HASH_MAP -> new CompoundValueHashMap(source);
                case COMPOUND_VALUE_LINEAR_MAP -> new CompoundValueLinearMap(source);
                case UNMODIFIABLE_LINKED_HASH_MAP -> Collections.unmodifiableMap(new LinkedHashMap<>(source));
            };
        }
    }

    public enum KeyCategory {
        // Long resource-location-style keys with a shared prefix, e.g. block palette entries.
        BLOCK {
            @Override
            String hitKey(int i) {
                return "minecraft:block/entry_" + i + "_value";
            }

            @Override
            String missKey(int i) {
                return "absent:block/entry_" + i + "_value";
            }
        },
        // Short keys like common entity NBT tags.
        ENTITY {
            @Override
            String hitKey(int i) {
                int round = i / ENTITY_KEYS.size();
                String base = keysForRound(round).get(i % ENTITY_KEYS.size());
                return round == 0 ? base : base + round;
            }

            @Override
            String missKey(int i) {
                return "xyz" + i;
            }
        },
        ;

        private static final List<String> ENTITY_KEYS = List.of(
            "id", "Pos", "Motion", "Rotation", "FallDistance", "Fire", "Air", "OnGround", "Invulnerable",
            "PortalCooldown", "UUID", "CustomName", "CustomNameVisible", "Silent", "NoGravity", "Glowing",
            "TicksFrozen", "HasVisualFire", "Tags", "Passengers", "Health", "AbsorptionAmount", "HurtTime",
            "HurtByTimestamp", "DeathTime", "FallFlying", "Brain", "Attributes", "ActiveEffects", "HandItems",
            "ArmorItems", "HandDropChances", "ArmorDropChances", "DeathLootTable", "CanPickUpLoot",
            "PersistenceRequired", "LeftHanded", "NoAI", "Leash", "Age", "ForcedAge", "InLove", "Owner",
            "Sitting", "CollarColor", "Count", "Slot", "tag"
        );

        private static int cachedRound = -1;
        private static List<String> cachedRoundKeys = ENTITY_KEYS;

        private static List<String> keysForRound(int round) {
            if (round == 0) {
                return ENTITY_KEYS;
            }
            if (round != cachedRound) {
                ArrayList<String> shuffled = new ArrayList<>(ENTITY_KEYS);
                Collections.shuffle(shuffled, new Random(round));
                cachedRoundKeys = shuffled;
                cachedRound = round;
            }
            return cachedRoundKeys;
        }

        // A key present in a map of the given size.
        abstract String hitKey(int i);

        // A key absent from a map of the given size, of the same shape as the hit keys.
        abstract String missKey(int i);
    }

    // Some interesting sizes to test.
    // "4" is a nice small size.
    // "256" may be affected by future optimizations to memory size.
    // "4096" gives a little more confidence that the "256" results scale.
    // "16384" is a large size that may be affected by future optimizations to memory size.
    // "65536" is a very large size that may be affected by future optimizations to memory size.
    @Param({"4", "256", "4096", "16384", "65536"})
    int size;

    @Param
    @Nullable
    MapType mapType;

    @Param
    @Nullable
    KeyCategory keyCategory;

    @Nullable
    Map<String, LinTag<?>> source;
    // A source whose keys have an uncached hash on every iteration, for the fresh-key construct benchmark.
    @Nullable
    Map<String, LinTag<?>> freshSource;
    @Nullable
    Map<String, LinTag<?>> map;
    @Nullable
    List<String> hitKeys;
    @Nullable
    List<String> missKeys;
    // The same keys as char[]s, so the fresh-key benchmarks can build a String with an uncached hash per lookup
    // without also having a toCharArray() allocation in the loop.
    @Nullable
    List<char[]> hitKeyChars;
    @Nullable
    List<char[]> missKeyChars;

    @Setup(Level.Trial)
    public void setup() {
        assert this.keyCategory != null;
        this.source = new LinkedHashMap<>();
        List<String> hitKeys = new ArrayList<>(this.size);
        List<String> missKeys = new ArrayList<>(this.size);
        List<char[]> hitKeyChars = new ArrayList<>(this.size);
        List<char[]> missKeyChars = new ArrayList<>(this.size);
        List<LinTag<?>> values = new ArrayList<>(this.size);
        for (int i = 0; i < this.size; i++) {
            String key = this.keyCategory.hitKey(i);
            String missKey = this.keyCategory.missKey(i);
            LinTag<?> value = LinIntTag.of(i);
            hitKeys.add(key);
            missKeys.add(missKey);
            hitKeyChars.add(key.toCharArray());
            missKeyChars.add(missKey.toCharArray());
            values.add(value);
            this.source.put(key, value);
        }
        this.hitKeys = hitKeys;
        this.missKeys = missKeys;
        this.hitKeyChars = hitKeyChars;
        this.missKeyChars = missKeyChars;
        this.freshSource = new FreshKeySource(hitKeyChars, values);
        assert mapType != null;
        this.map = this.mapType.create(this.source);
    }

    @Benchmark
    public void getHit(Blackhole blackhole) {
        assert this.hitKeys != null;
        assert this.map != null;
        for (String key : this.hitKeys) {
            blackhole.consume(this.map.get(key));
        }
    }

    @Benchmark
    public void getMiss(Blackhole blackhole) {
        assert this.missKeys != null;
        assert this.map != null;
        for (String key : this.missKeys) {
            blackhole.consume(this.map.get(key));
        }
    }

    // Fresh key versions prevent the LinkedHashMap from getting an advantage due to cached string hashes.
    // This is applicable for schematic loading use cases.
    @Benchmark
    public void getHitFreshKey(Blackhole blackhole) {
        assert this.hitKeyChars != null;
        assert this.map != null;
        for (char[] key : this.hitKeyChars) {
            blackhole.consume(this.map.get(new String(key)));
        }
    }

    @Benchmark
    public void getMissFreshKey(Blackhole blackhole) {
        assert this.missKeyChars != null;
        assert this.map != null;
        for (char[] key : this.missKeyChars) {
            blackhole.consume(this.map.get(new String(key)));
        }
    }

    @Benchmark
    public void iterate(Blackhole blackhole) {
        assert this.map != null;
        for (Map.Entry<String, LinTag<?>> entry : this.map.entrySet()) {
            blackhole.consume(entry.getKey());
            blackhole.consume(entry.getValue());
        }
    }

    @Benchmark
    public Map<String, LinTag<?>> construct() {
        assert this.mapType != null;
        assert this.source != null;
        return this.mapType.create(this.source);
    }

    @Benchmark
    public Map<String, LinTag<?>> constructFreshKey() {
        assert this.mapType != null;
        assert this.freshSource != null;
        return this.mapType.create(this.freshSource);
    }

    // Allows constructing a map from a source without computed hashes for the keys.
    // This prevents the LinkedHashMap from getting an advantage due to cached string hashes.
    private static final class FreshKeySource extends AbstractMap<String, LinTag<?>> {
        private final List<char[]> keys;
        private final List<LinTag<?>> values;

        FreshKeySource(List<char[]> keys, List<LinTag<?>> values) {
            this.keys = keys;
            this.values = values;
        }

        @Override
        public int size() {
            return this.keys.size();
        }

        @Override
        public Set<Entry<String, LinTag<?>>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Entry<String, LinTag<?>>> iterator() {
                    return new Iterator<>() {
                        private int cursor;

                        @Override
                        public boolean hasNext() {
                            return this.cursor < FreshKeySource.this.keys.size();
                        }

                        @Override
                        public Entry<String, LinTag<?>> next() {
                            if (this.cursor >= FreshKeySource.this.keys.size()) {
                                throw new NoSuchElementException();
                            }
                            int index = this.cursor++;
                            return new SimpleImmutableEntry<>(
                                new String(FreshKeySource.this.keys.get(index)),
                                FreshKeySource.this.values.get(index)
                            );
                        }
                    };
                }

                @Override
                public int size() {
                    return FreshKeySource.this.keys.size();
                }
            };
        }
    }
}
