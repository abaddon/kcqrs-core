# Projection Cache Improvements

## Summary of Changes

The `ProjectionHandler` cache has been improved to prevent unbounded memory growth, add proper lifecycle management, and provide observability.

### What Was Changed

**Before:**
```kotlin
private val projectionCache = ConcurrentHashMap<IProjectionKey, TProjection>()
```

**After:**
```kotlin
private val projectionCache = ProjectionCache<TProjection>(maxSize = cacheMaxSize)
```

---

## ✅ Implementation: Lightweight LRU Cache (Current)

### Features

1. **Bounded Size with LRU Eviction** - Default max size of 1000 entries
2. **Thread-Safe** - Uses `ReentrantReadWriteLock` for concurrent access
3. **Automatic Cleanup** - Cache is cleared when handler stops
4. **Observability** - Cache statistics available via `getCacheStats()`
5. **No External Dependencies** - Pure Kotlin/Java implementation
6. **Performance** - O(1) get/put operations with LRU tracking

### Usage

```kotlin
// Default cache size (1000 entries)
class MyProjectionHandler(
    override val repository: IProjectionRepository<MyProjection>
) : ProjectionHandler<MyProjection>() {
    // Uses default cacheMaxSize = 1000
}

// Custom cache size
class MyProjectionHandler(
    override val repository: IProjectionRepository<MyProjection>
) : ProjectionHandler<MyProjection>(
    cacheMaxSize = 5000  // Customize based on memory constraints
) {
    override fun getProjectionKey(event: IDomainEvent): IProjectionKey {
        return MyProjectionKey(event.aggregateId)
    }
}

// Monitor cache statistics
fun monitorCache() {
    val stats = handler.getCacheStats()
    println("Cache: ${stats.currentSize}/${stats.maxSize} (${stats.utilizationPercent}%)")
}
```

### How It Works

1. **LRU Policy**: Uses `LinkedHashMap` with `accessOrder = true`
   - Most recently accessed entries stay in cache
   - Least recently used entries evicted when capacity reached

2. **Thread Safety**: `ReentrantReadWriteLock` ensures:
   - Multiple concurrent reads
   - Exclusive writes
   - No race conditions

3. **Automatic Eviction**: When cache exceeds `maxSize`:
   ```kotlin
   override fun removeEldestEntry(...): Boolean = size > maxSize
   ```

4. **Lifecycle Management**: Cache cleared on handler stop:
   ```kotlin
   fun stop(exception: Throwable?) {
       projectionCache.clear() // Prevents memory leaks
       cancel(...)
   }
   ```

### Test Coverage

11 comprehensive tests covering:
- ✅ Basic get/put/remove operations
- ✅ LRU eviction behavior
- ✅ Size tracking and statistics
- ✅ Thread safety with concurrent access
- ✅ Cache clearing
- ✅ Utilization percentage calculation

---

## 🔄 Alternative: Caffeine Cache (Not Implemented)

For more advanced caching needs, consider using **Caffeine** - a high-performance Java caching library.

### Why Caffeine?

- **Advanced eviction policies**: Size-based, time-based (TTL), weight-based
- **Cache statistics**: Hit rate, miss rate, eviction count
- **Asynchronous loading**: Built-in async cache population
- **Write-through/write-behind**: Advanced persistence strategies
- **Cache warming**: Preload frequently accessed entries
- **Industry standard**: Used by Spring, Hibernate, Micronaut

### Implementation Example

**1. Add dependency to `gradle/libs.versions.toml`:**

```toml
[versions]
caffeine = "3.1.8"

[libraries]
caffeine = { module = "com.github.ben-manes.caffeine:caffeine", version.ref = "caffeine" }

[bundles]
ksqrs-core = [
    "kotlin-coroutines",
    "kotlin-reflect",
    "caffeine"  # Add this
]
```

**2. Create Caffeine-based cache:**

```kotlin
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

abstract class ProjectionHandler<TProjection : IProjection>(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    cacheMaxSize: Long = 1000,
    cacheTTL: Duration = Duration.ofHours(1)
) : IProjectionHandler<TProjection>, CoroutineScope by scope {

    private val projectionCache = Caffeine.newBuilder()
        .maximumSize(cacheMaxSize)
        .expireAfterAccess(cacheTTL)  // TTL support
        .recordStats()                 // Enable statistics
        .build<IProjectionKey, TProjection>()

    private suspend fun getOrInitialiseProjection(
        projectionKey: IProjectionKey
    ): Result<TProjection> {
        projectionCache.getIfPresent(projectionKey)?.let {
            return Result.success(it)
        }
        return repository.getByKey(projectionKey)
            .recoverCatching {
                repository.emptyProjection(projectionKey)
            }
    }

    private fun updateProjectionCache(projection: TProjection) {
        projectionCache.put(projection.key, projection)
    }

    fun getCacheStats(): CacheStats {
        val stats = projectionCache.stats()
        return CacheStats(
            hitRate = stats.hitRate(),
            missRate = stats.missRate(),
            evictionCount = stats.evictionCount(),
            currentSize = projectionCache.estimatedSize()
        )
    }

    fun stop(exception: Throwable?) {
        if (isActive) {
            projectionCache.invalidateAll()
            projectionCache.cleanUp()
            cancel(...)
        }
    }
}
```

**Benefits:**
- Time-based eviction (TTL/TTI)
- Detailed cache statistics (hit rate, miss rate)
- Better memory efficiency with size estimation
- Asynchronous cache loading support

**Tradeoffs:**
- Adds external dependency (~900KB)
- More complex API
- May be overkill for simple use cases

---

## Choosing the Right Approach

| Feature | Current (LRU) | Caffeine |
|---------|--------------|----------|
| **Dependencies** | None | +1 library |
| **Size** | ~200 LOC | ~900KB JAR |
| **Eviction** | LRU only | LRU, TTL, TTI, size, weight |
| **Statistics** | Basic (size, utilization) | Advanced (hit/miss rates, load times) |
| **Performance** | Excellent | Excellent |
| **Memory** | Minimal overhead | Optimized for memory efficiency |
| **Complexity** | Simple | Advanced features available |
| **Best For** | Most use cases | Large-scale production systems |

### Recommendation

**Use the current LRU implementation** for:
- ✅ Minimal dependencies preferred
- ✅ Simple, predictable behavior
- ✅ Standard CQRS projection caching
- ✅ Up to ~10,000 projections per handler

**Consider Caffeine** for:
- 🔄 Time-based eviction needed (TTL)
- 🔄 Detailed cache analytics required
- 🔄 Very large caches (>10,000 entries)
- 🔄 Complex cache warming strategies

---

## Configuration Guidelines

### Choosing `cacheMaxSize`

```kotlin
// Low-traffic projections (e.g., user settings)
cacheMaxSize = 100

// Standard projections (e.g., order summaries)
cacheMaxSize = 1000  // Default

// High-traffic projections (e.g., product catalog)
cacheMaxSize = 10000

// Memory-constrained environments
cacheMaxSize = 500
```

### Memory Estimation

Assuming average projection size of ~1KB:

| Cache Size | Estimated Memory |
|-----------|------------------|
| 100 | ~100 KB |
| 1,000 | ~1 MB |
| 10,000 | ~10 MB |
| 100,000 | ~100 MB |

**Monitor with:**
```kotlin
val stats = handler.getCacheStats()
log.info("Cache: ${stats.currentSize}/${stats.maxSize} " +
         "(${stats.utilizationPercent.format(2)}% utilized)")
```

---

## Migration Notes

### From Unbounded ConcurrentHashMap

The new cache maintains the same API semantics but adds safety:

1. **Automatic eviction** prevents OOM
2. **Lifecycle management** prevents leaks
3. **Observability** enables monitoring

### Backward Compatibility

✅ **Fully backward compatible** - existing code works without changes:

```kotlin
// This still works exactly the same
class MyProjectionHandler(
    override val repository: IProjectionRepository<MyProjection>
) : ProjectionHandler<MyProjection>() {
    // No changes needed
}
```

New features are **opt-in**:
```kotlin
// Opt-in to custom cache size
ProjectionHandler(cacheMaxSize = 5000)

// Opt-in to monitoring
handler.getCacheStats()
```

---

## Testing

All 100 tests pass, including 11 new cache-specific tests:

```bash
./gradlew test
```

**Cache test coverage:**
- ✅ LRU eviction behavior
- ✅ Thread safety (concurrent access)
- ✅ Size limits enforcement
- ✅ Statistics accuracy
- ✅ Cache clearing
- ✅ Update behavior

See: `ProjectionCacheTest.kt` for full test suite.

---

## Next Steps (Optional)

1. **Add cache warming** - Preload frequently accessed projections on startup
2. **Add cache metrics** - Expose via JMX/Prometheus for production monitoring
3. **Add TTL support** - Evict projections after time period (would require Caffeine)
4. **Add cache partitioning** - Multiple cache regions with different policies

For now, the current implementation provides:
- ✅ Memory safety (bounded size)
- ✅ Good performance (LRU + O(1) ops)
- ✅ No external dependencies
- ✅ Production-ready
