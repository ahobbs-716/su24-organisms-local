package organisms.g1;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;

public class Group1PlayerV2 implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private static final int THRESHOLD_ENERGY_TO_REPRODUCE = 400;
    private static final int LOW_ENERGY_THRESHOLD = 200;

    // 用于记录发现食物的位置
    private boolean foundFood = false;
    private Action foodDirection;
    private int stepsSinceFoodFound = 0;
    private static final int STEPS_TO_WAIT = 5;

    // 用于记录是否前往食物位置以及原始位置
    private boolean movingToFood = false;
    private Action returnDirection;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
    }

    @Override
    public String name() {
        return "G1V1-Guard-and-Eat";
    }

    @Override
    public Color color() {
        return new Color(139, 34, 34, 218);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        // 能量充足时优先繁殖，但也只在附近有食物的时候才繁殖
        if (energyLeft >= THRESHOLD_ENERGY_TO_REPRODUCE) {
            if (foodN && neighborN == -1) return Move.reproduce(Action.NORTH, this.dna);
            if (foodE && neighborE == -1) return Move.reproduce(Action.EAST, this.dna);
            if (foodS && neighborS == -1) return Move.reproduce(Action.SOUTH, this.dna);
            if (foodW && neighborW == -1) return Move.reproduce(Action.WEST, this.dna);
        }

        // 低能量时若附近有食物，赶紧吃一下
        if (energyLeft <= LOW_ENERGY_THRESHOLD) {
            if (foodN) return Move.movement(Action.NORTH);
            if (foodE) return Move.movement(Action.EAST);
            if (foodS) return Move.movement(Action.SOUTH);
            if (foodW) return Move.movement(Action.WEST);
        }

        // 发现食物后，记录位置并开始守护
        if (!foundFood) {
            if (foodN) { foundFood = true; foodDirection = Action.NORTH; returnDirection = Action.SOUTH; }
            if (foodE) { foundFood = true; foodDirection = Action.EAST; returnDirection = Action.WEST; }
            if (foodS) { foundFood = true; foodDirection = Action.SOUTH; returnDirection = Action.NORTH; }
            if (foodW) { foundFood = true; foodDirection = Action.WEST; returnDirection = Action.EAST; }
        }

        // 如果发现了食物，则守护并周期性前往食物位置检查
        if (foundFood) {
            stepsSinceFoodFound++;
            if (movingToFood) {
                movingToFood = false;
                // 检查当前格子是否还有食物
                if (foodHere == 0) {
                    foundFood = false; // 如果没有食物了，重新开始探索
                }
                return Move.movement(returnDirection); // 吃完食物后返回原始位置
            } else if (stepsSinceFoodFound >= STEPS_TO_WAIT) {
                stepsSinceFoodFound = 0;
                movingToFood = true;
                return Move.movement(foodDirection); // 前往食物位置
            } else {
                return Move.movement(Action.STAY_PUT); // 守护在原地
            }
        }

        // 如果没有发现食物，随机移动或保持不动
        foundFood = false;
        Action[] actions = new Action[]{Action.NORTH, Action.EAST, Action.SOUTH, Action.WEST, Action.STAY_PUT};
        int actionIndex = (int) (Math.random()*(actions.length));
        Action actionChoice = actions[actionIndex];

        // 避免移动到被占据的格子
        if((actionChoice == Action.NORTH && neighborN != -1) ||
                (actionChoice == Action.EAST && neighborE != -1) ||
                (actionChoice == Action.SOUTH && neighborS != -1) ||
                (actionChoice == Action.WEST && neighborW != -1)) {
            return Move.movement(Action.STAY_PUT);
        } else {
            return Move.movement(actionChoice);
        }
    }

    @Override
    public int externalState() {
        return dna;
    }
}
