package organisms.g2;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.Color;
import java.util.Random;

public class SamChecker implements OrganismsPlayer {
    private OrganismsGame game;
    private Random random = new Random();
    private int age = 0;
    private int movesSinceFood = 0;
    private static final int MAX_MOVES_WITHOUT_FOOD = 3;
    private static final double REPRODUCTION_THRESHOLD = 0.5; // Slightly more conservative than G6
    private static final double MOVEANYWAY_THRESHOLD = 0.25;
    private static final double THROWCHILDATFOOD_THRESHOLD= 0.6;

    private static final double MOVEORSPLIT_PROB = 0.5;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        age = dna;
        this.game = game;
    }

    @Override
    public String name() {
        return "Checker Sam Player";
    }

    @Override
    public Color color() {
        return Color.PINK;//return new Color(255, 100, 100);
    }

    @Override
    public int externalState() {
        return age % 256;
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        age++;
        movesSinceFood++;
        if (movesSinceFood > 50 ){
            movesSinceFood = 1;
        }
        boolean hasMovementEnergy = energyLeft>= game.M()*MOVEANYWAY_THRESHOLD && (movesSinceFood < 2) ;

        Move foodMove = moveToAdjacentFood(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);

        // Count neighbors
        int neighborCount = countNeighbors(neighborN, neighborE, neighborS, neighborW);

        if (foodMove!= null && energyLeft > game.M()*THROWCHILDATFOOD_THRESHOLD){
            movesSinceFood = 0;
            return Move.reproduce(foodMove.getAction(), age);
        }

        // Always eat if there's food
        if (foodHere > 0) {
            movesSinceFood = 0;
            return Move.movement(Action.STAY_PUT);
        }

        // Move to adjacent food if available
        if (foodMove != null) {
            // if we have a lot of energy, instead of moving, reproduce toward food
            /*if (energyLeft > game.M()*THROWCHILDATFOOD_THRESHOLD){
                return Move.reproduce(foodMove.getAction(), age);
            }*/
            // movesSinceFood = 0;
            return foodMove;
        }



        // 1) Reproduce if alone and energy is at least half of max
        if (neighborCount == 0) {
            if (energyLeft >= game.M() * REPRODUCTION_THRESHOLD){
                movesSinceFood = 0;
                return reproduceRandomly();

            } else if (hasMovementEnergy) {
                return moveRandomly(neighborN, neighborE, neighborS, neighborW);

            }
            else{
                return Move.movement(Action.STAY_PUT);
            }
        }

        // 2) Move perpendicularly if next to one other unit
        if (neighborCount == 1) {
            Move perpendicularMove = movePerpendicularly(neighborN, neighborE, neighborS, neighborW);

            if (perpendicularMove != null) {
                if (random.nextDouble()< MOVEORSPLIT_PROB && canReproduce(energyLeft)){
                    movesSinceFood = 0;
                    return Move.reproduce(perpendicularMove.getAction(), age);
                }else if(hasMovementEnergy){
                    return perpendicularMove;
                } else{
                    return Move.movement(Action.STAY_PUT);
                }


            }
        }

        //if 2 neighbors move randomly away from them
        if (neighborCount == 2 && hasMovementEnergy){
            return moveRandomly(neighborN, neighborE, neighborS, neighborW);
        }

        // 3) Move to an empty spot if surrounded by 3 neighbors
        if (neighborCount == 3 ) {
            Move escapeMove = moveToEmptySpot(neighborN, neighborE, neighborS, neighborW);
            if (escapeMove != null && hasMovementEnergy) {
                return escapeMove;
            }
        }

        // Try to reproduce if energy is high enough
        /*if (canReproduce(energyLeft)) {
            Move reproductionMove = tryReproduce(energyLeft, neighborN, neighborE, neighborS, neighborW);
            if (reproductionMove != null) {
                return reproductionMove;
            }
        }*/

        // Stay put if we've been moving without finding food

        /*if (movesSinceFood > MAX_MOVES_WITHOUT_FOOD) {
            return Move.movement(Action.STAY_PUT);
        }

        // Move randomly if energy is sufficient
        if (energyLeft > game.v()) {
            return moveRandomly(neighborN, neighborE, neighborS, neighborW);
        }*/

        // If all else fails, stay put
        return Move.movement(Action.STAY_PUT);
    }

    private boolean canReproduce(int energyLeft) {
        return energyLeft > game.M() * REPRODUCTION_THRESHOLD;
    }

    private Move tryReproduce(int energyLeft, int neighborN, int neighborE, int neighborS, int neighborW) {
        Action[] directions = {Action.NORTH, Action.EAST, Action.SOUTH, Action.WEST};
        int[] neighbors = {neighborN, neighborE, neighborS, neighborW};

        for (int i = 0; i < 4; i++) {
            if (neighbors[i] == -1) {
                return Move.reproduce(directions[i], age);
            }
        }
        return null;
    }

    private Move moveToAdjacentFood(boolean foodN, boolean foodE, boolean foodS, boolean foodW,
                                    int neighborN, int neighborE, int neighborS, int neighborW) {
        if (foodN && neighborN == -1) return Move.movement(Action.NORTH);
        if (foodE && neighborE == -1) return Move.movement(Action.EAST);
        if (foodS && neighborS == -1) return Move.movement(Action.SOUTH);
        if (foodW && neighborW == -1) return Move.movement(Action.WEST);
        return null;
    }

    private Move moveRandomly(int neighborN, int neighborE, int neighborS, int neighborW) {
        Action[] possibleMoves = new Action[4];
        int count = 0;
        if (neighborN == -1) possibleMoves[count++] = Action.NORTH;
        if (neighborE == -1) possibleMoves[count++] = Action.EAST;
        if (neighborS == -1) possibleMoves[count++] = Action.SOUTH;
        if (neighborW == -1) possibleMoves[count++] = Action.WEST;

        if (count > 0) {
            return Move.movement(possibleMoves[random.nextInt(count)]);
        }
        return Move.movement(Action.STAY_PUT);
    }

    private int countNeighbors(int neighborN, int neighborE, int neighborS, int neighborW) {
        return (neighborN != -1 ? 1 : 0) + (neighborE != -1 ? 1 : 0) +
                (neighborS != -1 ? 1 : 0) + (neighborW != -1 ? 1 : 0);
    }

    private Move reproduceRandomly() {
        Action[] directions = {Action.NORTH, Action.EAST, Action.SOUTH, Action.WEST};
        return Move.reproduce(directions[random.nextInt(4)], age);
    }

    private Move movePerpendicularly(int neighborN, int neighborE, int neighborS, int neighborW) {

            if (neighborE != -1) return Move.movement(Action.NORTH);
            if (neighborW != -1) return Move.movement(Action.SOUTH);

            if (neighborN != -1) return Move.movement(Action.EAST);
            if (neighborS != -1) return Move.movement(Action.WEST);

        return null;
    }

    private Move moveToEmptySpot(int neighborN, int neighborE, int neighborS, int neighborW) {
        if (neighborN == -1) return Move.movement(Action.NORTH);
        if (neighborE == -1) return Move.movement(Action.EAST);
        if (neighborS == -1) return Move.movement(Action.SOUTH);
        if (neighborW == -1) return Move.movement(Action.WEST);
        return null;
    }
}