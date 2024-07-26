package organisms.g3;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class g3player1awoiv implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private int currentX = 0;
    private int currentY = 0;
    private boolean movingRight = true;
    private boolean movingDown = true;
    private ThreadLocalRandom random;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() {
        return "g3Player1awoiv";
    }

    @Override
    public Color color() {
        return new Color(200, 0, 255, 200);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        // Consume food if present
        if (foodHere > 0) {
            int childPosIndex = this.random.nextInt(1, 5);
            Action childPosChoice = Action.fromInt(childPosIndex);
            int childKey = this.random.nextInt();
            return Move.reproduce(childPosChoice, childKey);
        }


        // Move in a 5x5 square pattern
        if (movingRight) {
            if (currentX < 4) {
                currentX++;
                return Move.movement(Action.EAST);
            } else {
                movingRight = false;
                if (movingDown && currentY < 4) {
                    currentY++;
                    return Move.movement(Action.SOUTH);
                } else {
                    movingDown = false;
                    currentY--;
                    return Move.movement(Action.NORTH);
                }
            }
        } else {

            if (currentX > 0) {
                currentX--;
                return Move.movement(Action.WEST);
            } else {
                movingRight = true;
                if (movingDown && currentY < 4) {
                    currentY++;
                    return Move.movement(Action.SOUTH);
                } else {
                    movingDown = false;
                    currentY--;
                    return Move.movement(Action.NORTH);
                }
            }
        }
    }

    @Override
    public int externalState() {
        return 0;
    }
}