package organisms.g6;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Group6Player implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;

    private int M; // Maximum energy level
    private int v; // Energy cost of movement
    private int u; // Energy gain from eating food
    private int movesSinceFood; // Track moves since last eating

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.M = game.M();
        this.v = game.v();
        this.u = game.u();
        this.movesSinceFood = 0;
    }

    @Override
    public String name() {
        return "Group 6 Player";
    }

    @Override
    public Color color() {
        return new Color(96, 14, 15, 100);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        if (shouldReproduce(energyLeft, neighborN, neighborE, neighborS, neighborW)) {
            return reproduce(neighborN, neighborE, neighborS, neighborW);
        } else if (shouldStayPut(foodHere, energyLeft)) {
            return Move.movement(Action.STAY_PUT);
        } else {
            return moveToFoodOrRandom(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW, energyLeft);
        }
    }

    private boolean shouldReproduce(int energyLeft, int neighborN, int neighborE, int neighborS, int neighborW) {
        // Check if reproduction is possible
        return energyLeft > M / 2 && hasEmptyAdjacentCell(neighborN, neighborE, neighborS, neighborW);
    }

    private boolean shouldStayPut(int foodHere, int energyLeft) {
        // Stay put if there's food here and energy is not full
        return foodHere > 0 && energyLeft < M - u;
    }

    private boolean hasEmptyAdjacentCell(int neighborN, int neighborE, int neighborS, int neighborW) {
        // Check for any empty adjacent cell
        return neighborN == -1 || neighborE == -1 || neighborS == -1 || neighborW == -1;
    }

    private Move reproduce(int neighborN, int neighborE, int neighborS, int neighborW) {
        // Reproduce in the first available empty cell
        if (neighborN == -1) return Move.reproduce(Action.NORTH, random.nextInt());
        if (neighborE == -1) return Move.reproduce(Action.EAST, random.nextInt());
        if (neighborS == -1) return Move.reproduce(Action.SOUTH, random.nextInt());
        if (neighborW == -1) return Move.reproduce(Action.WEST, random.nextInt());
        return Move.movement(Action.STAY_PUT);
    }

    private Move moveToFoodOrRandom(boolean foodN, boolean foodE, boolean foodS, boolean foodW,
                                    int neighborN, int neighborE, int neighborS, int neighborW,
                                    int energyLeft) {
        // Move towards food if available and the cell is empty
        if (foodN && neighborN == -1) {
            movesSinceFood = 0;
            return Move.movement(Action.NORTH);
        }
        if (foodE && neighborE == -1) {
            movesSinceFood = 0;
            return Move.movement(Action.EAST);
        }
        if (foodS && neighborS == -1) {
            movesSinceFood = 0;
            return Move.movement(Action.SOUTH);
        }
        if (foodW && neighborW == -1) {
            movesSinceFood = 0;
            return Move.movement(Action.WEST);
        }

        // If no food is found nearby for a few moves, prioritize staying put to save energy
        if (movesSinceFood > 2) {
            return Move.movement(Action.STAY_PUT);
        }

        // Random movement if energy allows
        if (energyLeft > v) {
            int[] possibleMoves = new int[4];
            int count = 0;
            if (neighborN == -1) possibleMoves[count++] = Action.NORTH.ordinal();
            if (neighborE == -1) possibleMoves[count++] = Action.EAST.ordinal();
            if (neighborS == -1) possibleMoves[count++] = Action.SOUTH.ordinal();
            if (neighborW == -1) possibleMoves[count++] = Action.WEST.ordinal();

            if (count > 0) {
                int actionIndex = random.nextInt(count);
                Action actionChoice = Action.fromInt(possibleMoves[actionIndex]);
                movesSinceFood++;
                return Move.movement(actionChoice);
            }
        }
        return Move.movement(Action.STAY_PUT);
    }

    @Override
    public int externalState() {
        return 0;
    }
}
