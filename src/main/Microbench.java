package main;

import main.simulation.FlockSimulation;
import main.spatial.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class Microbench {
    private record IndexCase(String name, Supplier<SpatialIndex> factory) {}
    private record BenchResult(String name, double avgMs) {}

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int BOID_COUNT = 1500;
    private static final int WARMUP_ITERATIONS = 75;
    private static final int MEASURED_ITERATIONS = 300;
    private static final double NEIGHBOR_RADIUS = 60.0;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        List<IndexCase> cases = List.of(
            new IndexCase("Naive O(n²)", NaiveSpatialIndex::new),
            new IndexCase("KD-Tree", KDTreeSpatialIndex::new),
            new IndexCase("Spatial Hashing", () -> new SpatialHashIndex(WIDTH, HEIGHT, 60)),
            new IndexCase("QuadTree", () -> new QuadTreeSpatialIndex(WIDTH, HEIGHT))
        );

        System.out.printf("Boids: %d | Iterations: %d (warmup: %d) | Radius: %.1f%n",
            BOID_COUNT, MEASURED_ITERATIONS, WARMUP_ITERATIONS, NEIGHBOR_RADIUS);
        System.out.println("--------------------------------------------------------");

        List<BenchResult> results = new ArrayList<>();
        for (IndexCase indexCase : cases) {
            results.add(runCase(indexCase));
        }

        for (BenchResult result : results) {
            System.out.printf("%-18s %.3f ms/iteration%n", result.name(), result.avgMs());
        }
    }

    private static BenchResult runCase(IndexCase indexCase) {
        FlockSimulation simulation = new FlockSimulation(WIDTH, HEIGHT);
        simulation.setSpatialIndex(indexCase.factory().get());
        simulation.setNeighborRadius(NEIGHBOR_RADIUS);
        simulation.setWanderShare(0.0);
        simulation.setBoidCount(BOID_COUNT);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            simulation.update();
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            simulation.update();
        }
        long elapsed = System.nanoTime() - start;
        double avgMs = (elapsed / 1_000_000.0) / MEASURED_ITERATIONS;
        return new BenchResult(indexCase.name(), avgMs);
    }
}
