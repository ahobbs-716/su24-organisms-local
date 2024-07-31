package organisms.g3;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collections;

public class g3PlayerV4 implements OrganismsPlayer {

    private ThreadLocalRandom random;

    private OrganismsGame game;
    private int dna;

    private int currentX = 0;
    private int currentY = 0;
    private boolean movingRight = true;
    private boolean movingDown = true;

    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();

    }

    public String name() {
        return "g3PlayerV4";
    }

    public Color color() {
        return new Color(255, 255, 204, 255);
    }

    private Move moveInSquarePattern() {
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

    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {


        int actionIndex = 0;
        Action actionChoice = Action.fromInt(actionIndex);
        int childPosIndex = 1;
        Action childPosChoice = Action.fromInt(1);


        int decisionWeightStay = 0;

        boolean foodBool =true;

        int unitsPerFood = game.u();
        int maxEnergy = game.M();
        int moveEnergyLoss = game.v();
        int maxFoodUnits = game.K();
        int stayPutUnits = game.s();
        int repValue =100;

        ArrayList<Integer> moveDecisions = new ArrayList<>();
        ArrayList<Integer> repDecisions = new ArrayList<>();


        if (energyLeft < maxEnergy / 2) {
            repValue = -50;
        }
        //boolean foodBool = true;
        int neighbor = -1;


        for(int i = 1; i < 5; i++) {

            if (i == 1) {
                foodBool = foodW;
                neighbor = neighborW;
            } else if (i == 2) {
                foodBool = foodN;
                neighbor = neighborN;
            } else if (i == 3) {
                foodBool = foodE;
                neighbor = neighborE;
            } else if (i == 4) {
                foodBool = foodS;
                neighbor = neighborS;
            }


            if (foodBool && neighbor == -1) {
                int moveAdd = (energyLeft - moveEnergyLoss + maxFoodUnits);
                int repAdd = (energyLeft - moveEnergyLoss + maxFoodUnits + repValue);

                moveDecisions.add(moveAdd);
                repDecisions.add(repAdd);

            } else {
                int moveAdd = (energyLeft - moveEnergyLoss);
                int repAdd = (energyLeft - moveEnergyLoss + repValue);
                moveDecisions.add(moveAdd);
                repDecisions.add(repAdd);
                decisionWeightStay = energyLeft - stayPutUnits;

            }
        }

        adjustWeights(moveDecisions, neighborN, neighborE, neighborS, neighborW);

        int repMax = Collections.max(repDecisions);
        int repInt = repDecisions.indexOf(repMax)+1;
        int movMax = Collections.max(moveDecisions);
        int movInt = moveDecisions.indexOf(movMax)+1;

        int firstElementREP = repDecisions.get(0);
        boolean repBOOL = repDecisions.stream().allMatch(element -> element.equals(firstElementREP));
        int firstElementMOV = moveDecisions.get(0);
        boolean movBOOL = moveDecisions.stream().allMatch(element -> element.equals(firstElementMOV));


//        System.out.println("--------------------------------");
//        System.out.println("decision for organism: " + this.dna);
//        System.out.println("move weights:" + moveDecisions);
//        System.out.println("max movement weight:" + movMax);
//        System.out.println("rep weights:" +repDecisions);
//        System.out.println("max reproduce weight:" + repMax);
//        System.out.println("decision to stay weight: "+ decisionWeightStay);

        if (repMax >= movMax) {
            if(repMax>decisionWeightStay) {
                if(repBOOL){
                    childPosChoice = Action.fromInt(random.nextInt(1,5));
                }
                else {
                    childPosChoice = Action.fromInt(repInt);
                }
                int childKey = random.nextInt(1, 255);
                //System.out.println("reproduced @: " + childPosChoice);
                return Move.reproduce(childPosChoice, childKey);
            }
            else{
                //System.out.println(Action.fromInt(0));
                return Move.movement(Action.fromInt(0));

            }

        } else {
            if(movMax>decisionWeightStay){
                if(movBOOL){
                    int childKey = random.nextInt(1, 255);
                    //System.out.println("reproduced @: " + childPosChoice);
                    return Move.reproduce(childPosChoice, childKey);
                }
                else {
                    actionChoice = Action.fromInt(movInt);
                }
                //System.out.println("moved to: " + actionChoice);
                return Move.movement(actionChoice);

            }
            else{
                //System.out.println(Action.fromInt(0));
                return Move.movement(Action.fromInt(0));


            }


        }

    }

    private boolean checkOrganismInDirection(int neighborValue) {
        return neighborValue == 115;
    }

    private void adjustWeights(ArrayList<Integer> moveDecisions, int neighborN, int neighborE, int neighborS, int neighborW) {
        if (checkOrganismInDirection(neighborW)) {
            moveDecisions.set(2, moveDecisions.get(2) + 25); // East
        }
        if (checkOrganismInDirection(neighborN)) {
            moveDecisions.set(3, moveDecisions.get(3) + 25); // South
        }
        if (checkOrganismInDirection(neighborE)) {
            moveDecisions.set(0, moveDecisions.get(0) + 25); // West
        }
        if (checkOrganismInDirection(neighborS)) {
            moveDecisions.set(1, moveDecisions.get(1) + 25); // North
        }
    }



    public int externalState() {
        return 115;
    }
}






