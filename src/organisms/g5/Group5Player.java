package organisms.g5;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static organisms.Constants.Action.*;

public class Group5Player implements OrganismsPlayer {
    enum OCCUPANT {
        empty,              // i.e no foreign object/organism on this square
        other_organism,     // can be of the same or a different species
        food
    }
    enum REGION {origin_clock, species_tag, generation, initial_direction, horizontal_dist_from_origin, vertical_dist_from_origin, sos}

    private OrganismsGame game;

    private final int REPRODUCTION_ENERGY = 490; // energy threshold to force reproduce
    private final int HIGH_ENERGY_THRESHOLD = 300;
    private final int MEDIUM_ENERGY_THRESHOLD = 150;
    private final int LOW_ENERGY_THRESHOLD = 75;
    private final int STEPS_WITHOUT_FOOD_THRESHOLD = 5;
    private int stepsWithoutFood = 0;
    private final int MAX_GENERATION = 1;
    private int generationCount = 0;
    private int inheritState = 0;


    int generation;
    int preferred_direction;
    int vertical_dist;
    int horizontal_dist;
    int origin_clock;



    //shared functions
    @Override
    public void register(OrganismsGame game, int dna) throws Exception {

        this.game = game;
        this.inheritState = dna;

        if (dna == -1) {                             //if FIRST
            this.generation = -1;
            this.preferred_direction = 0;
            this.vertical_dist = 0;
            this.horizontal_dist = 0;
            this.origin_clock = 0;

        } else if (getValueStoredAtRegion(dna, InheritancePlayer.REGION.generation) == 0) {                       //if WALL

            this.generation = getValueStoredAtRegion(dna, InheritancePlayer.REGION.generation);
            this.preferred_direction = getValueStoredAtRegion(dna, InheritancePlayer.REGION.initial_direction);
            this.vertical_dist = interpretDistance(getValueStoredAtRegion(dna, InheritancePlayer.REGION.vertical_dist_from_origin));
            this.horizontal_dist = interpretDistance(getValueStoredAtRegion(dna, InheritancePlayer.REGION.horizontal_dist_from_origin));
            this.origin_clock = interpretDistance(getValueStoredAtRegion(dna, InheritancePlayer.REGION.origin_clock));

        } else if (getValueStoredAtRegion(dna, InheritancePlayer.REGION.generation) == 1) {                                    //if NOMAD
            this.generation = getValueStoredAtRegion(dna, InheritancePlayer.REGION.generation);
            this.vertical_dist = interpretDistance(getValueStoredAtRegion(dna, InheritancePlayer.REGION.vertical_dist_from_origin));
            this.horizontal_dist = interpretDistance(getValueStoredAtRegion(dna, InheritancePlayer.REGION.horizontal_dist_from_origin));
        }

    }
    @Override
    public String name() { return "Group 5 Player"; }
    @Override
    public Color color() { return new Color(200, 200, 200, 255); }
    @Override
    public int externalState() throws Exception { return 0; }



    //DECISION FUNCTIONS
    public Move asWall(int neighborN, int neighborE, int neighborS, int neighborW, int threshold) {

        if (!exceedsBoundary(threshold)) {                                                      //form wall

            return Move.movement(Action.fromInt(preferred_direction));

        } else if (!exceedsBoundary(threshold+1)) {                                    //there is a gap in wall

            //condition double contact already - stay put
            if (doubleContact(neighborN, neighborE, neighborS, neighborW)) return Move.movement(STAY_PUT);

            //condition less than double contact: turn to fill gap
            if (viable(turn(preferred_direction), neighborN, neighborE, neighborS, neighborW, threshold + 1)) {
                return Move.reproduce(Action.fromInt(turn(preferred_direction, 1)), calculateDNA(0, preferred_direction, turn(preferred_direction, 1)));
            } else if (viable(turn(preferred_direction, 3), neighborN, neighborE, neighborS, neighborW, threshold + 1)) {
                return Move.reproduce(Action.fromInt(turn(preferred_direction, 3)), calculateDNA(0, preferred_direction, turn(preferred_direction, 3)));
            }

        }
        return Move.movement(Action.STAY_PUT);
    }
    public Move asNomad(int foodHere, int energyLeft,
                        boolean foodN, boolean foodE, boolean foodS, boolean foodW) {

        // 1st priority: force reproduction at high energy
        if (energyLeft >= REPRODUCTION_ENERGY) { return reproduceOnControlledDirection(); }

        // 2nd priority: If there is food on current square, stay put
        //               If there is food on adjacent square, move
        if (foodHere > 0) {
            stepsWithoutFood = 0;
            return Move.movement(Action.STAY_PUT);
        } else if (foodN || foodE || foodS || foodW) {
            return moveTowardsFood(foodN, foodE, foodS, foodW);
        }

        // 3rd priority: reproduce for 2 generations if no food is found after 5 steps
        if (stepsWithoutFood >= STEPS_WITHOUT_FOOD_THRESHOLD && generationCount < MAX_GENERATION) {
            generationCount++;
            reproduceOnControlledDirection();
        } else if (stepsWithoutFood >= STEPS_WITHOUT_FOOD_THRESHOLD && generationCount > MAX_GENERATION) {
            stepsWithoutFood = 0;
        }

        // 4th priority: If inherited state is not 0, move in the direction indicated by inherited state
        if (inheritState != 0) {
            Action moveAction = switch (inheritState) {
                case 1 -> Action.WEST;
                case 2 -> Action.EAST;
                case 3 -> Action.NORTH;
                case 4 -> Action.SOUTH;
                default -> Action.STAY_PUT;
            };

            // resets inherit state so it moves normally
            inheritState = 0;
            return Move.movement(moveAction);
        }

        stepsWithoutFood++;
        return generateBestMove(energyLeft);

    }
    public Move asAncestor() {

        if (origin_clock > 0 && origin_clock < 5) {
            return Move.reproduce(Action.fromInt(origin_clock), calculateDNA(0, origin_clock, origin_clock));
        } else if (origin_clock == 6) {
                return Move.reproduce(Action.fromInt(1), calculateDNA(1, 1, 1));
        } else return Move.movement(Action.STAY_PUT);
    }
    @Override
    public Move move(int foodHere, int energyLeft,
                     boolean foodN, boolean foodE, boolean foodS, boolean foodW,
                     int neighborN, int neighborE, int neighborS, int neighborW) throws Exception {

        int threshold = origin_clock/5;
        if (origin_clock < 18) origin_clock++;

        if (generation == -1) return updateDistance(asAncestor());
        else if (generation == 0) return updateDistance(asWall(neighborN, neighborE, neighborS, neighborW, threshold));
        else if (generation == 1) return updateDistance(asNomad(foodHere, energyLeft, foodN, foodE, foodS, foodW));

        throw new RuntimeException("Generation not recognised!");

    }


    //NOMAD CODE

    /*
    * Randomly chooses the direction to move where food is present
    * */
    private Move moveTowardsFood(boolean foodN, boolean foodE, boolean foodS, boolean foodW) {
        ArrayList<Move> stepDirection = new ArrayList<>();

        if (foodN) { stepDirection.add(Move.movement(Action.NORTH)); }
        if (foodE) { stepDirection.add(Move.movement(Action.EAST)); }
        if (foodS) { stepDirection.add(Move.movement(Action.SOUTH)); }
        if (foodW) { stepDirection.add(Move.movement(Action.WEST)); }

        return stepDirection.get((int) (Math.random() * stepDirection.size()));
    }
    /*
    * When reproducing, set the DNA as the direction where the child will move for the next few steps
    * */
    private Move reproduceOnControlledDirection() {
        int actionIndex = (int) (Math.random() * 4) + 1;
        Action actionChoice = Action.fromInt(actionIndex);

        return Move.reproduce(actionChoice, actionIndex);
    }
    /*
    * Controls the parameters for the biases based on current energy level
    * */
    private double[] biasController(int energyLeft) {
        // Calculate biases param
        double foodBias = 5.0;
        double energyBias = 5.0;
        double repoBias;

        if (energyLeft >= HIGH_ENERGY_THRESHOLD) {
            foodBias = 2.5;
            repoBias = 10;
        } else if (energyLeft >= MEDIUM_ENERGY_THRESHOLD) {
            repoBias = 7.5;
            energyBias = 10;
        } else if (energyLeft >= LOW_ENERGY_THRESHOLD) {
            repoBias = 2.5;
            foodBias = 7.5;
            energyBias = 10;
        } else {
            repoBias = 1;
            foodBias = 10;
            energyBias = 10;
        }

        return applyBias(foodBias, energyBias, repoBias);
    }
    /*
    * Finds the best move from using biases & current energy level
    * */
    private Move generateBestMove(int energyLeft) {
        // get the best move with biases
        double[] biases = biasController(energyLeft);

        // Optimize move based on netBenefit
        double maxBenefit = Double.NEGATIVE_INFINITY;
        Action bestMove = Action.STAY_PUT;

        // generate occupant based on current organism condition
        for (Action action : Action.values()) {
            boolean move = (action != Action.STAY_PUT);

            // generate maximum benefit move
            double benefit = netBenefit(move, OCCUPANT.empty, false, Optional.of(biases), energyLeft);
            if (benefit > maxBenefit) {
                maxBenefit = benefit;
                bestMove = action;
            }
        }

        System.out.println("Action " + bestMove.toString() + " , chosen based on net benefit.");
        return Move.movement(bestMove);
    }
    protected double netBenefit(boolean move, OCCUPANT occupant, boolean reproduce, Optional<double[]> override, int energyLeft) {

        // get game costings (default values)
        double v1 = game.v();              // move
        double v2 = game.v();              // reproduce
        double s = game.s();
        double u = game.u();

        // give player opportunity to override these
        if (override.isPresent() && override.get().length == 4) {
            v1 = override.get()[0];
            v2 = override.get()[1];
            s = override.get()[2];
            u = override.get()[3];
        }

        // calculate override of specific moves
        if (reproduce) {
            if (move) throw new IllegalArgumentException("Bad argument! Conflicting values for reproduce and move.");
            if (occupant.equals(OCCUPANT.other_organism)) throw new IllegalArgumentException("Bad argument! Conflicting values for reproduce and occupant.");
            return -v2;
        }

        if (!move) {
            if (occupant.equals(OCCUPANT.other_organism)) throw new IllegalArgumentException("Bad argument! Conflicting values for move and occupant.");
            double penalty = (energyLeft > 300) ? 50 : 0; // Apply penalty if energy is high
            if (occupant.equals(OCCUPANT.food)) return u - s - penalty; // gain from food (eat) - cost of staying (x) - penalty
            else return -s - penalty;
        } else {
            if (occupant.equals(OCCUPANT.food)) return u - v1;              // gain from food (eat) - cost of movement to get that food (exert)
            if (occupant.equals(OCCUPANT.empty)) return -v1;
            if (occupant.equals(OCCUPANT.other_organism)) {
                System.err.println("This move has poor efficiency! Please consider remain.");
                return -game.v();
            }
        }

        // we should never get here
        throw new IllegalArgumentException("No valid strategy for this combination of arguments. Please try again.");
    }
    protected double[] applyBias(double foodW, double energyW, double repoW) {

        // check inputs
        if (foodW < 0 || energyW < 0 || repoW < 0 || foodW > 10 || energyW > 10 || repoW > 10)
            throw new IllegalArgumentException("Bad input! Priority values must be between 0 and 10, inclusive.");

        // container for return value
        double[] netBenefits = new double[4];            // order of variables is V1, V2, S, U

        // work out priorities - expressed as a proportion of the max (30)
        double fp = (foodW / 30);
        double ep = (energyW / 30);
        double rp = (repoW / 30);

        // work out deltas for each game-set variable
        double v = 20 - 2;
        double u = 500 - 10;

        // change net benefits to reflect new biases
        netBenefits[0] = game.v();                     // leave cost of movement the same (never changed by weightings)
        netBenefits[1] = (game.v() - (v * rp));        // reduce cost of reproduction by scaling factor rp
        netBenefits[2] = (game.s() - (1 * ep));        // reduce cost of staying in place by scaling factor ep
        netBenefits[3] = (game.u() + (u * fp));        // increase the benefit of food by scaling factor fp

        // return new set of net benefits [order: v1, v2, s, u] with biases applied
        return netBenefits;
    }



    //INHERITANCE CODE
    public boolean doubleContact(int neighborN, int neighborE, int neighborS, int neighborW) {

        int contactPoints = 0;

        if (neighborN == 4353) contactPoints++;
        if (neighborE == 4553) contactPoints++;
        if (neighborW == 4553) contactPoints++;
        if (neighborS == 4553) contactPoints++;

        return contactPoints > 1;
    }
    public boolean exceedsBoundary(int threshold, int verticalDistance, int horizontalDistance) {

        if (verticalDistance > threshold) return true;
        if (verticalDistance < -threshold) return true;
        if (horizontalDistance > threshold) return true;
        if (horizontalDistance < -threshold) return true;

        return false;

    }

    /** wrapper method using current vertical and horizontal distances from first ancestor organism.
     * @param threshold the number of acceptable units an organism can move from its start point
     * @return a boolean indicating whether the organism is strictly beyond the boundary
     */
    public boolean exceedsBoundary(int threshold) {

        return exceedsBoundary(threshold, vertical_dist, horizontal_dist);
    }

    /**
     * helper method used to interpret the (horizontal and vertical) distance parameters written into the dna code.
     * This is required because the dna can only store positive numbers.
     * Therefore, any negative number is treated as a positive number over 20.
     * @param distance the binary number to interpret
     * @return the binary number, interpreted as a (positive or negative) integer
     */
    public int interpretDistance(int distance) {

        if (distance > 20) return  -(distance - 20);
        return distance;

    }
    public int preemptDistance(Action action, int distance, boolean horizontal) {

        if (horizontal) {
            switch (action) {case WEST -> {
                return distance - 1;}
                case EAST -> {return distance + 1;}
                case NORTH, SOUTH -> {return distance;}
            }
        } else {
            switch (action) {case WEST, EAST -> {
                return distance;}
                case NORTH -> {return distance + 1;}
                case SOUTH -> {return distance - 1;}
            }
        }

        return distance;
    }
    public Move updateDistance(Move move) {

        if (move.getAction().equals(REPRODUCE) || move.getAction().equals(STAY_PUT)) return move;

        else {
            horizontal_dist = preemptDistance(move.getAction(), horizontal_dist, true);
            vertical_dist = preemptDistance(move.getAction(), vertical_dist, false);
        }

        return move;

    }
    public int[] regionSlice(organisms.g5.InheritancePlayer.REGION region) {

        switch (region) {
            case generation -> {return new int[]{0, 6};}
            case horizontal_dist_from_origin -> {return new int[]{6, 12};}
            case vertical_dist_from_origin -> {return new int[]{12, 18};}
            case initial_direction -> {return new int[]{18, 24};}
            case origin_clock -> {return new int[]{24, 30};}
        }

        throw new RuntimeException("Region not recognised!");
    }
    public int calculateDNA(int generation, int preferredDirection, int birthDirection) {

        int dna = 0;

        //adjust for birth direction
        int tempHorizontal = preemptDistance(Action.fromInt(birthDirection), horizontal_dist, true);
        int tempVertical = preemptDistance(Action.fromInt(birthDirection), vertical_dist, false);

        //adjust for negative horizontals
        if (tempHorizontal < 0) tempHorizontal = 20 + Math.abs(tempHorizontal);
        if (tempVertical < 0) tempVertical = 20 + Math.abs(tempVertical);

        dna = storeValueAtRegion(dna, organisms.g5.InheritancePlayer.REGION.generation, generation);
        dna = storeValueAtRegion(dna, organisms.g5.InheritancePlayer.REGION.horizontal_dist_from_origin, tempHorizontal);
        dna = storeValueAtRegion(dna, organisms.g5.InheritancePlayer.REGION.vertical_dist_from_origin, tempVertical);
        dna = storeValueAtRegion(dna, organisms.g5.InheritancePlayer.REGION.initial_direction, preferredDirection);
        dna = storeValueAtRegion(dna, InheritancePlayer.REGION.origin_clock, origin_clock);

        return dna;
    }
    public boolean viable(int proposed, int neighborN, int neighborE,
                          int neighborS, int neighborW, int threshold) {

        //set up vars
        int projectedHorizontal= preemptDistance(Action.fromInt(proposed), horizontal_dist, true);
        int projectedVertical= preemptDistance(Action.fromInt(proposed), vertical_dist, false);

        //check whether it exceeds the boundary
        if (exceedsBoundary(threshold, projectedVertical, projectedHorizontal)) return false;

            //check whether it would lead to a collision with a neighbour
        if (proposed == NORTH.intValue() && neighborN != -1) return false;
        else if (proposed == EAST.intValue() && neighborE != -1) return false;
        else if (proposed == SOUTH.intValue() && neighborS != -1) return false;
        if (proposed == WEST.intValue() && neighborW != -1) return false;

        return true;

    }
    public int getValueStoredAtRegion(int host, organisms.g5.InheritancePlayer.REGION region) {

        int[] slice = regionSlice(region);                                  //get beginning and end values
        String binary = getBinary(host).substring(slice[0], slice[1]);      //use these values to slice
        return Integer.parseUnsignedInt(binary, 2);                    //return the slice as a number

    }
    public int storeValueAtRegion(int dna, organisms.g5.InheritancePlayer.REGION region, int value) {

        String vs = getBinary(value).substring(32 - 6, 32);     //get the 6 digits to store
        StringBuilder ds = new StringBuilder(getBinary(dna));                             //get the DNA string

        ds.replace(regionSlice(region)[0], regionSlice(region)[1], vs);   //replace the relevant region
        return Integer.parseUnsignedInt((String.valueOf(ds)),2);                    //return the updated dna
    }
    public Integer turn(int action) {

        List<Integer> clockwise = List.of(WEST.intValue(), NORTH.intValue(), EAST.intValue(), SOUTH.intValue());

        int index = clockwise.indexOf(action);

        if (index < 0 || index > 4) throw new RuntimeException("Action not recognised");
        if (index == 3) return clockwise.get(0);

        else return clockwise.get(index+1);
    }
    public Integer turn(int action, int revolutions) {

        for (int i = 0; i < revolutions; i++) action = turn(action);
        return action;
    }
    public String getBinary(int num) {
        return String.format("%32s", Integer.toUnsignedString(num, 2)).replace(' ', '0');
    }
}
