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

    private int energy;

    private ArrayList<Action> lastMoves;

    private ThreadLocalRandom random;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game= game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.lastMoves = new ArrayList<>();
    }

    @Override
    public String name() { return "Group 7 Player"; }

    @Override
    public Color color() {
        return new Color(255, 10, 10, 255);
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


    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        Action action = null;
        boolean[] food = new boolean[]{foodN, foodE, foodS, foodW};
        int[] neighbors = new int[]{neighborN, neighborE, neighborS, neighborW};
        ArrayList<Integer> emptySquares = new ArrayList<>();
        Action[] dirs = new Action[]{Action.NORTH, Action.EAST, Action.SOUTH, Action.WEST};

        this.energy = energyLeft;


        //if present square has food or coolDown is 0, stay put
        //consider moving to allow the chance for more food to generate
        if (0 < foodHere && foodHere < 3) {//best upper limit is 3, original limit was 4
            return Move.movement(Action.STAY_PUT);
        }
        else {
            //move to adjacent square with food if present
            for (int i=0; i<4; ++i) {
                if (food[i] && neighbors[i] == -1) {
                    action = dirs[i];
                    return Move.movement(action);
                }
                if (neighbors[i] == -1) {
                    emptySquares.add(i);
                }
            }
            //if no food present
            if (action == null) {
                //if no open spaces, stay put
                if (emptySquares.size() == 0) {
                    return Move.movement(Action.STAY_PUT);
                }

                if(energyLeft<250){//best so far 200
                    return Move.movement(Action.STAY_PUT);
                }


                //otherwise choose random empty square to move to, avoiding previous square if possible
                Action oppDir = getOppositeDirection();
                emptySquares.remove(oppDir);
                if (emptySquares.size() == 1 && dirs[emptySquares.get(0)] == oppDir) {
                    action = oppDir;
                } else {
                    int randIndex = this.random.nextInt(0, emptySquares.size());
                    action = dirs[emptySquares.get(randIndex)];
                }

                if (energyLeft >= 250) {// best so far 250
                    return Move.reproduce(action, dna);
                }
                if(neighborN>=240 && neighborN<=241){
                    return Move.movement(Action.SOUTH);
                }
                if(neighborS>=240 && neighborS<=241){
                    return Move.movement(Action.NORTH);
                }
                if(neighborW>=240 && neighborW<=241){
                    return Move.movement(Action.EAST);
                }
                if(neighborE>=240 && neighborE<=241){
                    return Move.movement(Action.WEST);
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
        return 240+(this.energy/50);
    }
}

