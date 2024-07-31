package organisms.g5;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class NomadPlayer implements OrganismsPlayer {
    enum OCCUPANT {
        empty,              // i.e no foreign object/organism on this square
        other_organism,     // can be of the same or a different species
        food
    }

    private OrganismsGame game;

    private int REPRODUCTION_ENERGY_THRESHOLD; // energy threshold to force reproduce
    private int HIGH_ENERGY_THRESHOLD;
    private int MEDIUM_ENERGY_THRESHOLD;
    private int LOW_ENERGY_THRESHOLD;

    private final int STEPS_WITHOUT_FOOD_THRESHOLD = 4;
    private int stepsWithoutFood = 0;

    private final int MAX_GENERATION = 1;
    private int generationCount = 0;

    private int inheritState = 0;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.inheritState = dna;

        this.REPRODUCTION_ENERGY_THRESHOLD = (int) (game.M() * 0.46);
        this.HIGH_ENERGY_THRESHOLD = (int) (game.M() * 0.34);
        this.MEDIUM_ENERGY_THRESHOLD = (int) (game.M() * 0.26);
        this.LOW_ENERGY_THRESHOLD = (int) (game.M() * 0.13);
    }

    @Override
    public String name() { return "Nomad Player"; }

    @Override
    public Color color() { return new Color(200, 220, 240, 255); }


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
        double foodBias = 4.5;
        double energyBias = 4.5;
        double repoBias;

        if (energyLeft >= HIGH_ENERGY_THRESHOLD) {
            foodBias = 2.5;
            repoBias = 10;
        } else if (energyLeft >= MEDIUM_ENERGY_THRESHOLD) {
            repoBias = 7.5;
            energyBias = 0;
        } else if (energyLeft >= LOW_ENERGY_THRESHOLD) {
            repoBias = 2.5;
            foodBias = 7.5;
            energyBias = 1;
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

    @Override
    public Move move(int foodHere, int energyLeft,
                     boolean foodN, boolean foodE, boolean foodS, boolean foodW,
                     int neighborN, int neighborE, int neighborS, int neighborW) throws Exception {

        // 1st priority: force reproduction at high energy
        if (energyLeft >= REPRODUCTION_ENERGY_THRESHOLD) { return reproduceOnControlledDirection(); }

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
            double penalty = (energyLeft > REPRODUCTION_ENERGY_THRESHOLD) ? 50 : 0; // Apply penalty if energy is high
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

    @Override
    public int externalState() throws Exception { return 0; }
}