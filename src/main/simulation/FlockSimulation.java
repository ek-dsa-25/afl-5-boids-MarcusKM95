package main.simulation;

import main.behavior.BehaviorStrategy;
import main.behavior.FlockBehavior;
import main.model.Boid;
import main.model.BoidType;
import main.spatial.*;
import main.behavior.WanderBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FlockSimulation {
    private final List<Boid> boids;
    private SpatialIndex spatialIndex;
    private final int width;
    private final int height;
    private double neighborRadius = 50.0;
    private double lastIterationTimeMs = 0;
    private final Map<BoidType, Supplier<BehaviorStrategy>> behaviorFactories;
    private double wanderShare = 0.25;

    public FlockSimulation(int width, int height) {
        this.width = width;
        this.height = height;
        this.boids = new ArrayList<>();
        this.spatialIndex = new NaiveSpatialIndex();
        this.behaviorFactories = new HashMap<>();
        registerBehavior(BoidType.STANDARD, FlockBehavior::new);
        registerBehavior(BoidType.WANDERER, WanderBehavior::new);
    }

    public void setSpatialIndex(SpatialIndex spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    public void addBoid() {
        int futureSize = boids.size() + 1;
        int desiredWanderers = (int) Math.round(futureSize * wanderShare);
        if (getCountByType(BoidType.WANDERER) < desiredWanderers) {
            addBoid(BoidType.WANDERER);
        } else {
            addBoid(BoidType.STANDARD);
        }
    }

    public void addBoid(BoidType type) {
        int id = boids.size();
        double x = Math.random() * width;
        double y = Math.random() * height;
        boids.add(new Boid(id, x, y, type, createBehaviorForType(type)));
    }

    public void setBoidCount(int count) {
        rebuildBoids(count);
    }

    public void update() {
        long startTime = System.nanoTime();
        
        spatialIndex.clear();
        for (Boid boid : boids) {
            spatialIndex.insert(boid);
        }

        for (Boid boid : boids) {
            List<Boid> neighbors = spatialIndex.findNeighbors(boid, neighborRadius);
            boid.update(neighbors, width, height);
        }
        
        long endTime = System.nanoTime();
        lastIterationTimeMs = (endTime - startTime) / 1_000_000.0;
    }

    public List<Boid> getBoids() {
        return boids;
    }

    public String getSpatialIndexName() {
        return spatialIndex.getName();
    }

    public double getLastIterationTimeMs() {
        return lastIterationTimeMs;
    }

    public int getBoidCount() {
        return boids.size();
    }

    public void setNeighborRadius(double radius) {
        this.neighborRadius = radius;
    }

    public double getNeighborRadius() {
        return neighborRadius;
    }


    public int getCountByType(BoidType type) {
        return (int) boids.stream().filter(b -> b.getType() == type).count();
    }

    public double getWanderShare() {
        return wanderShare;
    }

    public void setWanderShare(double share) {
        wanderShare = Math.max(0.0, Math.min(share, 1.0));
        rebuildBoids(boids.size());
    }

    public void registerBehavior(BoidType type, Supplier<BehaviorStrategy> factory) {
        behaviorFactories.put(type, factory);
    }

    private BehaviorStrategy createBehaviorForType(BoidType type) {
        Supplier<BehaviorStrategy> factory = behaviorFactories.get(type);
        if (factory == null) {
            factory = FlockBehavior::new;
        }
        return factory.get();
    }

    private void rebuildBoids(int count) {
        List<Boid> newBoids = new ArrayList<>(count);
        int wanderTarget = (int) Math.round(count * wanderShare);

        for (int i = 0; i < count; i++) {
            BoidType type = (i < wanderTarget) ? BoidType.WANDERER : BoidType.STANDARD;
            double x = (i < boids.size()) ? boids.get(i).getX() : Math.random() * width;
            double y = (i < boids.size()) ? boids.get(i).getY() : Math.random() * height;
            newBoids.add(new Boid(i, x, y, type, createBehaviorForType(type)));
        }

        boids.clear();
        boids.addAll(newBoids);
    }
}
