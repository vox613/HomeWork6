package innopolis.homework6;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GameOfLiveTest {

    @Test
    void run() throws InterruptedException {
        String ITERATIONS_NUM = "1000";
        String inFilePath = ".\\resources\\input";
        String outFilePath = ".\\resources\\output";
        GameOfLive game = new GameOfLive(inFilePath, outFilePath, ITERATIONS_NUM);

        long singleThreadTime = game.getSingleThreadGameTime();
        byte[][] singleThreadMass = game.getFinalMatrix();
        long multiThreadTime = game.getMultiThreadGameTime();
        byte[][] multiThreadMass = game.getFinalMatrix();

        for (int i = 0; i < singleThreadMass.length; i++) {
            assertArrayEquals(singleThreadMass[i], multiThreadMass[i]);
        }
        System.out.println("singleThreadTime = " + singleThreadTime);
        System.out.println("multiThreadTime  = " + multiThreadTime);

    }
}