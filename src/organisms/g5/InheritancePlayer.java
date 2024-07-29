package organisms.g5;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static organisms.Constants.Action.*;


public class InheritancePlayer implements OrganismsPlayer {

    private OrganismsGame game;
    int generation;
    int preferred_direction;
    int vertical_dist;
    int horizontal_dist;
    int origin_clock;

    enum REGION {origin_clock, species_tag, generation, initial_direction, horizontal_dist_from_origin, vertical_dist_from_origin, sos}

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {

        this.game = game;

        if (dna == -1) {                //if QUEEN
            this.generation = -1;
            this.preferred_direction = 0;
            this.vertical_dist = 0;
            this.horizontal_dist = 0;
            this.origin_clock = 0;

        } else {                        //if OTHER
            this.generation = getValueStoredAtRegion(dna, REGION.generation);
            this.preferred_direction = getValueStoredAtRegion(dna, InheritancePlayer.REGION.initial_direction);
            this.vertical_dist = interpretDistance(getValueStoredAtRegion(dna, REGION.vertical_dist_from_origin));
            this.horizontal_dist = interpretDistance(getValueStoredAtRegion(dna, REGION.horizontal_dist_from_origin));
            this.origin_clock = interpretDistance(getValueStoredAtRegion(dna, REGION.origin_clock));

        }
    }
    @Override
    public String name() {
        return "Inheritance Player";
    }
    @Override
    public Color color() {

        if (generation == -1) return new Color(166, 124, 255, 0);
        else return new Color(60, 30, 255, 255);
    }

    public boolean exceedsBoundary(int threshold, int verticalDistance, int horizontalDistance) {
        return (verticalDistance > threshold) || (verticalDistance < -threshold) || (horizontalDistance > threshold) || (horizontalDistance < -threshold);

    }
    public boolean exceedsBoundary(int threshold) {
        return exceedsBoundary(threshold, vertical_dist, horizontal_dist);
    }

    public String getBinary(int num) {
        return String.format("%32s", Integer.toUnsignedString(num, 2)).replace(' ', '0');
    }
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
    public Action updateDistance(Action action) {

        horizontal_dist = preemptDistance(action, horizontal_dist, true);
        vertical_dist = preemptDistance(action, vertical_dist, false);

        return action;

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
        dna = storeValueAtRegion(dna, REGION.origin_clock, origin_clock);

        return dna;
    }

    public boolean viable(int proposed, int neighborN, int neighborE,
                            int neighborS, int neighborW, int threshold) {

        int projectedHorizontal= preemptDistance(Action.fromInt(proposed), horizontal_dist, true);
        int projectedVertical= preemptDistance(Action.fromInt(proposed), vertical_dist, false);

        if (exceedsBoundary(threshold, projectedVertical, projectedHorizontal)) return false;
        else if (proposed == NORTH.intValue() && neighborN != -1) return false;
        else if (proposed == EAST.intValue() && neighborE != -1) return false;
        else if (proposed == SOUTH.intValue() && neighborS != -1) return false;
        return (!(proposed == WEST.intValue() && neighborW != -1));

    }
    public boolean viable(int proposed, int neighborN, int neighborE, int neighborS, int neighborW) {

        return viable(proposed, neighborN, neighborE, neighborS, neighborW, 100);
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

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        int threshold = origin_clock/4;

        origin_clock++;

        if (generation == -1) {

            if (origin_clock < 5) return Move.reproduce(Action.fromInt(origin_clock), calculateDNA(origin_clock, origin_clock, origin_clock));
            else return Move.movement(Action.STAY_PUT);

        } else {

            if (!exceedsBoundary(threshold)) {

                return Move.movement(updateDistance(Action.fromInt(preferred_direction)));

            } else if (!exceedsBoundary(threshold+1)) {

                if (viable(turn(preferred_direction), neighborN, neighborE, neighborS, neighborW, threshold + 1)) {
                    return Move.reproduce(Action.fromInt(turn(preferred_direction, 1)), calculateDNA(2, preferred_direction, turn(preferred_direction, 1)));
                } else if (viable(turn(preferred_direction, 3), neighborN, neighborE, neighborS, neighborW, threshold + 1)) {
                    return Move.reproduce(Action.fromInt(turn(preferred_direction, 3)), calculateDNA(2, preferred_direction, turn(preferred_direction, 3)));
                }
            }
        }
        return Move.movement(Action.STAY_PUT);
    }
    @Override
    public int externalState() {
        return 0;
    }

}