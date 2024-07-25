//package organisms.g5;
//
//import org.junit.Before;
//import org.junit.Test;
//import organisms.ui.OrganismsGame;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static junit.framework.TestCase.assertEquals;
//import static junit.framework.TestCase.assertTrue;
//import static org.junit.Assert.assertThrows;
//import static org.junit.Assert.fail;
//
//public class applyBiasTest {
//
//    OrganismsGame minValsGame;
//    OrganismsGame medValsGame;
//
//    OrganismsGame maxValsGame;
//    Group5Player player;
//
//    @Before
//    public void setUp() throws Exception {
//        player = new Group5Player();
//
//        minValsGame = new OrganismsGame() {
//            @Override
//            public int s() {
//                return 1;
//            }
//            @Override
//            public int u() {
//                return 10;
//            }
//            @Override
//            public int v() {
//                return 2;
//            }
//        };
//
//        maxValsGame = new OrganismsGame() {
//            @Override
//            public int s() {
//                return 1;
//            }
//
//            @Override
//            public int u() {
//                return 500;
//            }
//
//            @Override
//            public int v() {
//                return 20;
//            }
//        };
//
//        medValsGame = new OrganismsGame() {
//            @Override
//            public int s() {
//                return 1;
//            }
//
//            @Override
//            public int u() {
//                return 250;
//            }
//
//            @Override
//            public int v() {
//                return 11;
//            }
//        };
//    }
//
//    //input checking behaviour
//    @Test
//    public void throwsExceptionWhenArgsTooLow() throws Exception {
//
//        player.register(medValsGame, 0);
//
//        assertThrows(IllegalArgumentException.class, () -> player.applyBias(-1, 5, 5));
//        assertThrows(IllegalArgumentException.class, () -> player.applyBias(5, -1, 5));
//        assertThrows(IllegalArgumentException.class, () -> player.applyBias(5, 5, -1));
//
//    }
//    @Test
//    public void throwsExceptionWhenArgsTooHigh() throws Exception {
//
//        player.register(medValsGame, 0);
//
//        assertThrows(IllegalArgumentException.class, () -> player.applyBias(11, 5, 5));
//        assertThrows(IllegalArgumentException.class, () -> player.applyBias(5, 11, 5));
//        assertThrows(IllegalArgumentException.class, () -> player.applyBias(5, 5, 11));
//
//    }
//    @Test
//    public void copesWithMaxArgs() throws Exception {
//
//        player.register(medValsGame, 0);
//
//        assertEquals(4, player.applyBias(10, 5, 5).length);
//        assertEquals(4, player.applyBias(5, 10, 5).length);
//        assertEquals(4, player.applyBias(5, 5, 10).length);
//
//    }
//    @Test
//    public void copesWithMinArgs() throws Exception {
//
//        player.register(medValsGame, 0);
//
//        assertEquals(4, player.applyBias(0, 5, 5).length);
//        assertEquals(4, player.applyBias(5, 0, 5).length);
//        assertEquals(4, player.applyBias(5, 5, 0).length);
//
//    }
//
//    @Test
//    public void foodSeekingPriority() throws Exception {
//
//        //set up player
//        player.register(medValsGame, 0);
//
//        //set up value registers
//        List<Double> remainAndEatRecord = new ArrayList<>();
//        List<Double> moveAndEatRecord = new ArrayList<>();
//
//        //for each marginally higher priority given to food seeking...
//        for (int i = 1; i < 11; i++) {
//
//            //apply that as a bias
//            double[] biases = player.applyBias(i, 0, 0);
//
//            //calculate the costs that generates
//            double remain = player.netBenefit(false, Group5Player.OCCUPANT.empty, false, Optional.of(biases));
//            double remainAndEat = player.netBenefit(false, Group5Player.OCCUPANT.food, false, Optional.of(biases));
//            double move = player.netBenefit(true, Group5Player.OCCUPANT.empty, false, Optional.of(biases));
//            double moveAndEat = player.netBenefit(true, Group5Player.OCCUPANT.food, false, Optional.of(biases));
//            double reproduce = player.netBenefit(false, Group5Player.OCCUPANT.empty, true, Optional.of(biases));
//
//            //record the food-specific costs
//            remainAndEatRecord.add(remainAndEat);
//            moveAndEatRecord.add(moveAndEat);
//
//            //eating should always leave you better off than not eating
//            if (remainAndEat <= remain) fail("at values (0," + i + "0), remainAndEat(" + remainAndEat + ") <= remain(" + remain + ")");
//            if (moveAndEat <= move) fail("at values (0," + i + "0), moveAndEat(" + moveAndEat + ") <= move(" + move + ")");
//        }
//
//        //the value of eating should improve over time
//        for (int i = 0; i < remainAndEatRecord.size(); i++) System.out.println(remainAndEatRecord.get(i));
//        for (int i = 1; i < 9; i++) if (remainAndEatRecord.get(i) >= remainAndEatRecord.get(i + 1)) fail("the cost of remainingToEat is lower for food priority " + i + " than it is for food priority " + (i+1));
//
//        for (int i = 0; i < moveAndEatRecord.size(); i++) System.out.println(moveAndEatRecord.get(i));
//        for (int i = 1; i < 9; i++) if (moveAndEatRecord.get(i) >= moveAndEatRecord.get(i + 1)) fail("the cost of movingToEat is lower for food priority " + i + " than it is for food priority " + (i+1));
//
//    }
//
//    @Test
//    public void energyConservingPriority() throws Exception {
//
//        //set up player
//        player.register(medValsGame, 0);
//
//        //set up value registers
//        List<Double> remainRecord = new ArrayList<>();
//
//        //for each marginally higher priority given to food seeking...
//        for (int i = 1; i <= 10; i++) {
//
//            //apply that as a bias
//            double[] biases = player.applyBias(0, i, 0);
//
//            //calculate the costs that generates
//            double remain = player.netBenefit(false, Group5Player.OCCUPANT.empty, false, Optional.of(biases));
//            double remainAndEat = player.netBenefit(false, Group5Player.OCCUPANT.food, false, Optional.of(biases));
//            double move = player.netBenefit(true, Group5Player.OCCUPANT.empty, false, Optional.of(biases));
//            double moveAndEat = player.netBenefit(true, Group5Player.OCCUPANT.food, false, Optional.of(biases));
//            double reproduce = player.netBenefit(false, Group5Player.OCCUPANT.empty, true, Optional.of(biases));
//
//            //record the cost of remaining
//            remainRecord.add(remain);
//
//            //remaining should always leave you better off than reproducing
//            if (remain <= reproduce) fail("at values (0," + i + ",0), remain(" + remain + ") <= reproduce(" + reproduce + ")");
//            if (remain <= move) fail("at values (0," + i + ",0), remain(" + remain + ") <= move(" + move + ")");
//        }
//
//        //the cost of remaining should decreases as we focus on energy preservation
//        for (int i = 1; i < remainRecord.size()-1; i++) if (remainRecord.get(i) >= remainRecord.get(i + 1)) fail("the cost of remaining is not lower for energy priority " + (i+1) + " than it is for energy priority " + (i));
//
//    }
//
//    @Test
//    public void reproductionPriority() throws Exception {
//
//        //set up player
//        player.register(medValsGame, 0);
//
//        //set up value registers
//        List<Double> reproduceRecord = new ArrayList<>();
//        List<Double> remainRecord = new ArrayList<>();
//
//        //for each marginally higher priority given to food seeking...
//        for (int i = 1; i < 11; i++) {
//
//            //apply that as a bias
//            double[] biases = player.applyBias(0, 0, i);
//
//            //calculate the costs that generates
//            double remain = player.netBenefit(false, Group5Player.OCCUPANT.empty, false, Optional.of(biases));
//            double remainAndEat = player.netBenefit(false, Group5Player.OCCUPANT.food, false, Optional.of(biases));
//            double move = player.netBenefit(true, Group5Player.OCCUPANT.empty, false, Optional.of(biases));
//            double moveAndEat = player.netBenefit(true, Group5Player.OCCUPANT.food, false, Optional.of(biases));
//            double reproduce = player.netBenefit(false, Group5Player.OCCUPANT.empty, true, Optional.of(biases));
//
//            //record the cost of reproducing
//            reproduceRecord.add(reproduce);
//            remainRecord.add(remain);
//
//            //reproducing should always leave you better off than moving
//            if (reproduce <= move) fail("at values (0," + i + "0), reproduce(" + reproduce + ") <= move(" + move + ")");
//        }
//
//        //the cost of reproducing should decrease over time
//        for (int i = 0; i < reproduceRecord.size()-1; i++) if (reproduceRecord.get(i) >= reproduceRecord.get(i + 1)) fail("the cost of reproducing is higher for reproduction priority " + i + " than it is for reproduction priority " + (i+1));
//
//        //the delta between reproducing and remaining should drop over time
//        for (int i = 0; i < reproduceRecord.size()-1; i++) {
//            double deltai = remainRecord.get(i) - reproduceRecord.get(i);
//            double deltaiplus1 = remainRecord.get(i+1) - reproduceRecord.get(i+1);
//            if (deltai < deltaiplus1) fail("the delta between reproduce and remain is not dropping over time");
//        }
//    }
//
//    //priority behaviour //CHECK THESE
//    @Test
//    public void lowestPossibleCostings() throws Exception {
//
//        player.register(minValsGame, 0);
//
//        //try with most extreme biases
//        assertEquals(4, player.applyBias(0, 0, 0).length);
//        assertEquals(4, player.applyBias(10, 10, 10).length);
//
//    }
//    @Test
//    public void highestPossibleCostings() throws Exception {
//        player.register(maxValsGame, 0);
//
//        //try with most extreme biases
//        assertEquals(4, player.applyBias(10, 10, 10).length);
//        assertEquals(4, player.applyBias(0,0,0).length);
//    }
//
//}
