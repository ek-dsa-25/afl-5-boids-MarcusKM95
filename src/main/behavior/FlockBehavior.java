package main.behavior;

import main.model.Boid;
import main.simulation.Forces;
import main.simulation.Vector2D;

import java.util.List;


public class FlockBehavior implements BehaviorStrategy {
    private static final double DESIRED_SPEED = 2.0;
    private static final double MAX_FORCE = 0.03;

    private final FlockWeights weights;

    public FlockBehavior() {
        this(FlockWeights.standard());
    }

    public FlockBehavior(FlockWeights weights) {
        this.weights = weights;
    }

    @Override
    public Forces calculateForces(Boid boid, List<Boid> neighbors) {
        if (neighbors.isEmpty()) {
            return new Forces();
        }

        Vector2D separation = calculateSeparation(boid, neighbors);
        Vector2D alignment = calculateAlignment(boid, neighbors);
        Vector2D cohesion = calculateCohesion(boid, neighbors);

        return new Forces(separation, alignment, cohesion);
    }

    private Vector2D calculateSeparation(Boid boid, List<Boid> neighbors) {
        double steerX = 0;
        double steerY = 0;
        int count = 0;

        for (Boid neighbor : neighbors) {
            double distance = boid.distanceTo(neighbor);
            if (distance > 0 && distance < 25) {
                double diffX = boid.getX() - neighbor.getX();
                double diffY = boid.getY() - neighbor.getY();

                diffX /= distance;
                diffY /= distance;

                steerX += diffX;
                steerY += diffY;
                count++;
            }
        }

        if (count > 0) {
            steerX /= count;
            steerY /= count;

            double magnitude = Math.sqrt(steerX * steerX + steerY * steerY);
            if (magnitude > 0) {
                steerX = (steerX / magnitude) * DESIRED_SPEED;
                steerY = (steerY / magnitude) * DESIRED_SPEED;

                steerX -= boid.getVx();
                steerY -= boid.getVy();

                double force = Math.sqrt(steerX * steerX + steerY * steerY);
                if (force > MAX_FORCE) {
                    steerX = (steerX / force) * MAX_FORCE;
                    steerY = (steerY / force) * MAX_FORCE;
                }
            }
        }

        return new Vector2D(steerX * weights.separation(), steerY * weights.separation());
    }

    private Vector2D calculateAlignment(Boid boid, List<Boid> neighbors) {
        double avgVx = 0;
        double avgVy = 0;
        int count = 0;

        for (Boid neighbor : neighbors) {
            double distance = boid.distanceTo(neighbor);
            if (distance > 0 && distance < 50) {
                avgVx += neighbor.getVx();
                avgVy += neighbor.getVy();
                count++;
            }
        }

        if (count > 0) {
            avgVx /= count;
            avgVy /= count;

            double magnitude = Math.sqrt(avgVx * avgVx + avgVy * avgVy);
            if (magnitude > 0) {
                avgVx = (avgVx / magnitude) * DESIRED_SPEED;
                avgVy = (avgVy / magnitude) * DESIRED_SPEED;

                double steerX = avgVx - boid.getVx();
                double steerY = avgVy - boid.getVy();

                double force = Math.sqrt(steerX * steerX + steerY * steerY);
                if (force > MAX_FORCE) {
                    steerX = (steerX / force) * MAX_FORCE;
                    steerY = (steerY / force) * MAX_FORCE;
                }

                return new Vector2D(steerX * weights.alignment(), steerY * weights.alignment());
            }
        }

        return Vector2D.ZERO;
    }

    private Vector2D calculateCohesion(Boid boid, List<Boid> neighbors) {
        double centerX = 0;
        double centerY = 0;
        int count = 0;

        for (Boid neighbor : neighbors) {
            double distance = boid.distanceTo(neighbor);
            if (distance > 0 && distance < 50) {
                centerX += neighbor.getX();
                centerY += neighbor.getY();
                count++;
            }
        }

        if (count > 0) {
            centerX /= count;
            centerY /= count;

            double steerX = centerX - boid.getX();
            double steerY = centerY - boid.getY();

            double magnitude = Math.sqrt(steerX * steerX + steerY * steerY);
            if (magnitude > 0) {
                steerX = (steerX / magnitude) * DESIRED_SPEED;
                steerY = (steerY / magnitude) * DESIRED_SPEED;

                steerX -= boid.getVx();
                steerY -= boid.getVy();

                double force = Math.sqrt(steerX * steerX + steerY * steerY);
                if (force > MAX_FORCE) {
                    steerX = (steerX / force) * MAX_FORCE;
                    steerY = (steerY / force) * MAX_FORCE;
                }

                return new Vector2D(steerX * weights.cohesion(), steerY * weights.cohesion());
            }
        }

        return Vector2D.ZERO;
    }
}
