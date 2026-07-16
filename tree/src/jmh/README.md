# CompoundValueMap benchmarks

JMH benchmarks for the production `CompoundValueMap`, the open-addressed map backing
`LinCompoundTag`. The benchmarks live in the `org.enginehub.linbus.tree` package
so they can construct the package-private map directly.

`CompoundValueMapBenchmark` covers the map's hot paths across several sizes:

- `getHit` tests a successful get with a reused key (its `String.hashCode` is cached after the first lookup)
- `getMiss` tests an unsuccessful get with a reused key
- `getHitFreshKey` / `getMissFreshKey` build a new `String` per lookup so the hash is uncached, forcing the map to
  rehash the key every time
- `construct` tests the time to build the copy of another map (the source's keys have cached hashes)
- `constructFreshKey` builds from a source whose keys have uncached hashes, so both maps hash every key during the
  copy rather than the `LinkedHashMap` path reusing cached hashes
- `iterate` tests the time to iterate over the map's entries, which is a common operation when serializing

## Running

```sh
# CPU: all JMH benchmarks (fork/warmup/iteration counts come from the jmh { } block in build.gradle.kts)
./gradlew :tree:jmh

# CPU + allocation: add `profilers.add("gc")` to the jmh { } block so the benchmarks report
# `gc.alloc.rate.norm` (bytes allocated per op), or run the jar directly:
#   ./gradlew :tree:jmhJar && java -jar tree/build/libs/tree-*-jmh.jar -prof gc
```
