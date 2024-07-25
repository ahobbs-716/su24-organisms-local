package organisms.g7;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
public class Group7Player implements OrganismsPlayer {

    private OrganismsGame game;

    private int dna;

    private int energy = 0;

    private ArrayList<Action> lastMoves = new ArrayList<>();

    private int RESETCOOLDOWN = 10;

    private boolean coolingDown = false;

    private int coolDown = 0;

    private ThreadLocalRandom random;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game= game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() { return "Group 7 Player"; }

    @Override
    public Color color() {
        return new Color(87, 6, 236, 204);
    }

    private Action getOppositeDirection() {
        HashMap<Action, Action> opp = new HashMap<>();
        opp.put(Action.NORTH, Action.SOUTH);
        opp.put(Action.EAST, Action.WEST);

        if (lastMoves.size() > 0) {
            Action currDir = lastMoves.get(lastMoves.size() - 1);
            for (Action key : opp.keySet()) {
                if (currDir == opp.get(key)) {
                    return key;
                }
                else if (currDir == key) {
                    return opp.get(key);
                }
            }
        }

        return null;
    }

    private boolean neighborsPresent(int[] neighbors) {
        for (int i : neighbors) {
            if (i != -1) {
                return true;
            }
        }
        return false;
    }

    private void incrementCooldown() {
        if (coolDown < RESETCOOLDOWN) {
            ++coolDown;
        }
    }

    private void decrementCooldown() {
        if (coolDown > 0) {
            --coolDown;
        }
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        Action action = null;
        boolean[] food = new boolean[]{foodN, foodE, foodS, foodW};
        int[] neighbors = new int[]{neighborN, neighborE, neighborS, neighborW};
        ArrayList<Integer> emptySquares = new ArrayList<>();
        Action[] dirs = new Action[]{Action.NORTH, Action.EAST, Action.SOUTH, Action.WEST};

        this.energy = Math.min(255, energy);

        if (coolDown == 0) {
            coolingDown = true;
        }
        else if (coolDown >= 5) {
            coolingDown = false;
        }

        //if present square has food or coolDown is 0, stay put
        //consider moving to allow the chance for more food to generate
        if (0 < foodHere && foodHere < 4 || coolingDown) {
            incrementCooldown();
            return Move.movement(Action.STAY_PUT);
        }
        else {
            //move to adjacent square with food if present
            for (int i=0; i<4; ++i) {
                if (food[i] && neighbors[i] == -1) {
                    action = dirs[i];
                    break;
                }
                if (neighbors[i] == -1) {
                    emptySquares.add(i);
                }
            }
            //if no food present
            if (action == null) {
                //if no open spaces, stay put
                if (emptySquares.size() == 0) {
                    incrementCooldown();
                    return Move.movement(Action.STAY_PUT);
                }

                //check external state of neighbors move away from low energy neighbors
                if(neighborN>=0 && neighborN<50){
                    action = Action.SOUTH;
                }
                if(neighborS>=0 && neighborS<50){
                    action = Action.NORTH;
                }
                if(neighborW>=0 && neighborW<50){
                    action = Action.EAST;
                }
                if(neighborE>=0 && neighborE<50){
                    action = Action.WEST;
                }
                if (action == null) {
                    //otherwise choose random empty square to move to, avoiding previous square if possible
                    Action oppDir = getOppositeDirection();
                    emptySquares.remove(oppDir);
                    if (emptySquares.size() == 1 && dirs[emptySquares.get(0)] == oppDir) {
                        decrementCooldown();
                        action = oppDir;
                    } else {
                        int randIndex = this.random.nextInt(0, emptySquares.size());
                        action = dirs[emptySquares.get(randIndex)];
                    }
                }
                if (action != Action.STAY_PUT) {
                    decrementCooldown();
                }

                if (energyLeft >= 250) {
                    return Move.reproduce(action, dna);
                }
                else {
                    return Move.movement(action);
                }


            }
        }

        return Move.movement(action);
    }

    @Override
    public int externalState() {
        return this.energy;
    }
}

