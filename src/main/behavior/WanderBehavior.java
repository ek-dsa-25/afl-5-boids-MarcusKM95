package main.behavior;

import main.model.Boid;
import main.simulation.Forces;
import main.simulation.Vector2D;

import java.util.List;

public class WanderBehavior implements BehaviorStrategy {
    private final FlockBehavior flockBehavior;
    private final double jitterStrength;
    private final double maxTurnDelta;

    public WanderBehavior() {
        this(new FlockBehavior(new FlockWeights(1.2, 0.5, 0.6)), 0.02, Math.toRadians(25));
    }

    public WanderBehavior(FlockBehavior flockBehavior, double jitterStrength, double maxTurnDelta) {
        this.flockBehavior = flockBehavior;
        this.jitterStrength = jitterStrength;
        this.maxTurnDelta = maxTurnDelta;
    }

    @Override
    public Forces calculateForces(Boid boid, List<Boid> neighbors) {
        Forces base = flockBehavior.calculateForces(boid, neighbors);

        double heading = Math.atan2(boid.getVy(), boid.getVx());
        double randomTurn = (Math.random() - 0.5) * 2 * maxTurnDelta;
        double wanderAngle = heading + randomTurn;

        Vector2D wanderVector = new Vector2D(
            Math.cos(wanderAngle) * jitterStrength,
            Math.sin(wanderAngle) * jitterStrength
        );

        Vector2D adjustedAlignment = new Vector2D(
            base.alignment().x() + wanderVector.x(),
            base.alignment().y() + wanderVector.y()
        );

        return new Forces(base.separation(), adjustedAlignment, base.cohesion());
    }
}
