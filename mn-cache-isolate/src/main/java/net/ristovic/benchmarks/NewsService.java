package net.ristovic.benchmarks;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;

import jakarta.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.graalvm.nativeimage.ObjectSnapshots;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshot;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshotProvider;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshotRegion;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshotSlot;

@Singleton
@CacheConfig("headlines")
public class NewsService {

    private final Headlines cache;

    public NewsService() {
        var provider = ObjectSnapshots.provider();
        var slot = ObjectSnapshots.snapshotRegion().getSlot(0);

        try {

            // Snapshot path
            Path p = Paths.get("cache.doss");
            File f = new File(p.toString());

            ObjectSnapshot s = null;

            // Check if snapshot exists
            if (f.isFile()) { 
                // If yes, then...
                s = provider.load(p, slot);
            } else {
                // If not, then...

                // Warm-up the cache
                for (int i = 1; i <= 12; i++) {
                    Headlines.instance().get(i);
                }

                // Store warmed-up cache
                s = provider.createObjectSnapshot(Headlines.instance, slot);
                provider.store(slot, p);
            }

            cache = (Headlines) s.get();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable
    public List<String> headlines(Integer month) {
        return cache.get(month);
    }

    @CachePut(parameters = {"month"})
    public List<String> addHeadline(Integer month, String headline) {
        cache.put(month, headline);
        return cache.get(month);
    }

    @CacheInvalidate(parameters = {"month"})
    public void removeHeadline(Integer month, String headline) {
        var l = cache.get(month);
        l.remove(headline);
        cache.replace(month, l);
    }
}
