//package organisms.g5;
//import org.junit.Before;
//import org.junit.Test;
//import organisms.ui.OrganismsGame;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.util.Optional;
//
//import static junit.framework.TestCase.assertEquals;
//import static org.junit.Assert.assertThrows;
//
//public class netBenefitTest {
//
//    OrganismsGame game;
//    Group5Player player;
//    int exist;
//    int exert;
//    int eat;
//
//    @Before
//    public void setUp() throws Exception {
//
//        eat = 10;
//        exert = 2;
//        exist = 1;
//
//        //MADE ORG GAME NOT FINAL - NEED TO CHANGE BACK
//        game = new OrganismsGame() {
//            @Override
//            public int s() {
//                return 1;
//            }
//
//            @Override
//            public int u() {
//                return 10;
//            }
//
//            @Override
//            public int v() {
//                return 2;
//            }
//        };
//        player = new Group5Player();
//        player.register(game, 0);
//    }
//    @Test
//    public void reproduceGoodOccupant() {
//        assertEquals(-exert, player.netBenefit(false, Group5Player.OCCUPANT.empty, true, Optional.empty()), 0.0);
//
//    }
//    @Test
//    public void reproduceIrrelevantOccupant() {
//        assertEquals(-exert, player.netBenefit(false, Group5Player.OCCUPANT.food, true, Optional.empty()), 0.0);
//
//    }
//    @Test
//    public void reproduceBadOccupant() {
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, true, Optional.empty()));
//
//    }
//    @Test
//    public void reproduceIllegalAttemptToMove() {
//
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(true, Group5Player.OCCUPANT.empty, true, Optional.empty()));
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(true, Group5Player.OCCUPANT.other_organism, true, Optional.empty()));
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(true, Group5Player.OCCUPANT.food, true, Optional.empty()));
//
//    }
//    @Test
//    public void moveToEmptySquare() {
//        assertEquals(-exert, player.netBenefit(true, Group5Player.OCCUPANT.empty, false, Optional.empty()), 0.0);
//
//
//    }
//    @Test
//    public void moveOntoOtherOrganism() {
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        System.setErr(new PrintStream(stream));
//        assertEquals(-exert, player.netBenefit(true, Group5Player.OCCUPANT.other_organism, false, Optional.empty()), 0.0);
//        assertEquals("This move has poor efficiency! Please consider remain.", stream.toString().trim());
//
//    }
//    @Test
//    public void moveIntoFoodSquare() {
//        assertEquals((eat - exert), player.netBenefit(true, Group5Player.OCCUPANT.food, false, Optional.empty()), 0.0);
//
//
//    }
//    @Test
//    public void stayNoFood() {
//
//        assertEquals(-exist, player.netBenefit(false, Group5Player.OCCUPANT.empty, false, Optional.empty()), 0.0);
//
//    }
//    @Test
//    public void stayWithFood() {
//
//        assertEquals(eat - exist, player.netBenefit(false, Group5Player.OCCUPANT.food, false, Optional.empty()), 0.0);
//
//    }
//    @Test
//    public void stayErrorCurrentSquare() {
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.empty()));
//    }
//    @Test
//    public void validOverrideValue() {
//
//    }
//    @Test
//    public void outOfBoundsOverrideTooLow() {
//
//        double[] override = {-1, 5, 5, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override)));
//
//        double[] override1 = {5, -1, 5, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override1)));
//
//        double[] override2 = {5, 5, -1, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override2)));
//
//        double[] override3 = {5, 5, 5, -1};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override3)));
//
//    }
//    @Test
//    public void outOfBoundsOverrideTooHigh() {
//
//        double[] override = {11, 5, 5, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override)));
//
//        double[] override1 = {5, 11, 5, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override1)));
//
//        double[] override2 = {5, 5, 11, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override2)));
//
//        double[] override3 = {5, 5, 5, 11};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override3)));
//
//    }
//    @Test
//    public void wrongLengthOfOverrideArray() {
//        double[] override = {5, 5, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override)));
//
//        double[] override2 = {5, 5, 5, 5, 5};
//        assertThrows(IllegalArgumentException.class, () -> player.netBenefit(false, Group5Player.OCCUPANT.other_organism, false, Optional.of(override2)));
//
//    }
//
//
//}
//
