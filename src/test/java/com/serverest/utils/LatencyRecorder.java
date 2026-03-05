package com.serverest.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LatencyRecorder {
    private static final List<Long> TIMES = Collections.synchronizedList(new ArrayList<>());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static volatile boolean hookAdded = false;

    private LatencyRecorder() {
    }

    public static void record(long millis) {
        TIMES.add(millis);
        if (!hookAdded) {
            synchronized (LatencyRecorder.class) {
                if (!hookAdded) {
                    Runtime.getRuntime().addShutdownHook(new Thread(LatencyRecorder::writeSummary));
                    hookAdded = true;
                }
            }
        }
    }

    private static void writeSummary() {
        try {
            Path out = Path.of("target", "latency-summary.json");
            Files.createDirectories(out.getParent());
            if (TIMES.isEmpty()) {
                Files.writeString(out, "{}", StandardCharsets.UTF_8);
                return;
            }
            List<Long> sorted = new ArrayList<>(TIMES);
            Collections.sort(sorted);
            long p95 = percentile(sorted, 0.95);
            long p99 = percentile(sorted, 0.99);
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("count", sorted.size());
            map.put("p95_ms", p95);
            map.put("p99_ms", p99);
            map.put("max_ms", sorted.get(sorted.size() - 1));
            map.put("min_ms", sorted.get(0));
            Files.writeString(out, MAPPER.writeValueAsString(map), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static long percentile(List<Long> data, double p) {
        int index = (int) Math.ceil(p * data.size()) - 1;
        index = Math.max(0, Math.min(index, data.size() - 1));
        return data.get(index);
        }
}
