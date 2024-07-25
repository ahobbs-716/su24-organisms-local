package organisms.g2;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SamPlayer implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private Action lastMove;
    private int roundsSinceLastSplit;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.lastMove = null;
        this.roundsSinceLastSplit = 0;
    }

    @Override
    public String name() {
        return "Sam Player";
    }

    @Override
    public Color color() {
        return new Color(196, 84, 255, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        roundsSinceLastSplit++;

        // If energy is above 400 and it's been at least 10 rounds since last split, try reproduce
        if (energyLeft > 400 && roundsSinceLastSplit > 10) {
            Action reproductionDirection = getRandomDirection();
            roundsSinceLastSplit = 0; // Reset counter
            return Move.reproduce(reproductionDirection, random.nextInt());
        }

        // Look for food in adjacent cells
        List<Action> foodDirections = new ArrayList<>();
        if (foodN) foodDirections.add(Action.NORTH);
        if (foodE) foodDirections.add(Action.EAST);
        if (foodS) foodDirections.add(Action.SOUTH);
        if (foodW) foodDirections.add(Action.WEST);

        // If there's food nearby, move towards it
        if (!foodDirections.isEmpty()) {
            Action chosenDirection = foodDirections.get(random.nextInt(foodDirections.size()));
            lastMove = chosenDirection;
            return Move.movement(chosenDirection);
        }

        // If ther's no food nearby, move randomly but not back to same spot
        Action randomMove;
        do {
            randomMove = getRandomDirection();
        } while (randomMove == getOppositeDirection(lastMove));

        lastMove = randomMove;
        return Move.movement(randomMove);
    }

    @Override
    public int externalState() {
        return 0;
    }

    private Action getRandomDirection() {
        int directionIndex = random.nextInt(4);
        return Action.fromInt(directionIndex + 1);
    }

    private Action getOppositeDirection(Action action) {
        if (action == null) return null;
        switch (action) {
            case NORTH: return Action.SOUTH;
            case SOUTH: return Action.NORTH;
            case EAST: return Action.WEST;
            case WEST: return Action.EAST;
            default: return null;
        }
    }
}
