package organisms.g1;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.Random;

public class Group1PlayerV5 implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private int energyThreshold;
    private int maxEnergy;
    private Action[] preferredDirections;
    private Random random = new Random();

    // 设置最小和最大探索概率
    private final double minProbability = 0.0;
    private final double maxProbability = 0.5;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.energyThreshold = game.M() / 2; // 设置能量阈值为最大能量的一半
        this.maxEnergy = game.M();
        this.preferredDirections = getRandomDirections();
    }

    @Override
    public String name() {
        return "G1V5-Probabilistic-Explorer";
    }

    @Override
    public Color color() {
        return new Color(70, 100, 170, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        // 1. 无论何时，只要旁边有食物，就过去吃
        if (foodN) {
            return Move.movement(Action.NORTH);
        } else if (foodW) {
            return Move.movement(Action.WEST);
        } else if (foodS) {
            return Move.movement(Action.SOUTH);
        } else if (foodE) {
            return Move.movement(Action.EAST);
        }

        // 2. 能量高于阈值时，在邻域找一个没有被占据的格子进行分裂
        if (energyLeft > energyThreshold) {
            if (neighborN == -1) {
                return Move.reproduce(Action.NORTH, this.dna);
            } else if (neighborW == -1) {
                return Move.reproduce(Action.WEST, this.dna);
            } else if (neighborS == -1) {
                return Move.reproduce(Action.SOUTH, this.dna);
            } else if (neighborE == -1) {
                return Move.reproduce(Action.EAST, this.dna);
            }
        }

        // 3. 根据能量水平使用非线性概率函数决定是否探索
        if (shouldExplore(energyLeft) && !highCollision(neighborN, neighborE, neighborS, neighborW)) {
            // 4. 探索方向在东南西北四个方向中随机选择两个
            Action explorationDirection = preferredDirections[random.nextInt(2)];
            return Move.movement(explorationDirection);
        }

        // 默认保持不动
        return Move.movement(Action.STAY_PUT);
    }

    @Override
    public int externalState() {
        return dna;
    }

    // 选择两个随机方向进行探索
    private Action[] getRandomDirections() {
        Action[] directions = new Action[]{Action.NORTH, Action.WEST, Action.SOUTH, Action.EAST};
        Action[] selectedDirections = new Action[2];
        int index1 = random.nextInt(directions.length);
        int index2;
        do {
            index2 = random.nextInt(directions.length);
        } while (index1 == index2);
        selectedDirections[0] = directions[index1];
        selectedDirections[1] = directions[index2];
        return selectedDirections;
    }

    // 判断是否应该探索
    private boolean shouldExplore(int energyLeft) {
        double probability = minProbability + (maxProbability - minProbability) * Math.pow((double) energyLeft / maxEnergy, 2);
        return random.nextDouble() < probability;
    }

    private boolean highCollision(int neighborN, int neighborE,
                                  int neighborS, int neighborW) {
        int score = 0;
        if (neighborN == dna) {
            score++;
        }
        if (neighborE == dna) {
            score++;
        }
        if (neighborS == dna) {
            score++;
        }
        if (neighborW == dna) {
            score++;
        }
        return score > 3;
    }
}
