package innopolis.homework6;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * The main class of the "Game of Life" program. Implemented one and multi-threaded version of the game.
 * The program accepts the initial state of the field from the file as an input or randomly generates a field
 * of the specified size. Then the specified number of iterations of the field calculation is carried out.
 * After that, the final configuration of the field and the time spent on single and multi-threaded calculations
 * are recorded in the specified output file.
 */
public class GameOfLive implements Runnable {

    private final int ITERATIONS_NUM;
    private final int FIELD_SIZE;
    private int counter;
    private final int[][] fieldSplitIndex;
    private byte[][] lifeGeneration;
    private byte[][] nextGeneration;
    private byte[][] tempo;
    private final boolean randomFieldGenerate;
    private final String outputFile;

    /**
     * The class constructor takes as arguments the number of iterations of the calculation, the size of the field and
     * the path to the file for recording the result.
     *
     * @param ITERATIONS_NUM - number of iterations of field recalculation. The number is greater than 0,
     *                       otherwise the default number of iterations will be set to 10.
     * @param FIELD_SIZE     - edge size of the field. An integer greater than 3, otherwise the default size
     *                       will be set to 10.
     * @param outputFile     - file path for recording calculation results. If the file is missing, it will be created.
     *                       Each time the file is overwritten.
     */
    GameOfLive(int ITERATIONS_NUM, int FIELD_SIZE, String outputFile) {
        this.ITERATIONS_NUM = (ITERATIONS_NUM > 0) ? ITERATIONS_NUM : 10;
        this.FIELD_SIZE = (FIELD_SIZE > 3) ? FIELD_SIZE : 10;
        this.counter = 0;
        this.fieldSplitIndex = new int[][]{{0, 0, FIELD_SIZE / 2, FIELD_SIZE / 2},
                {FIELD_SIZE / 2, 0, FIELD_SIZE, FIELD_SIZE / 2},
                {0, FIELD_SIZE / 2, FIELD_SIZE / 2, FIELD_SIZE},
                {FIELD_SIZE / 2, FIELD_SIZE / 2, FIELD_SIZE, FIELD_SIZE}
        };
        this.lifeGeneration = new byte[FIELD_SIZE][FIELD_SIZE];
        this.nextGeneration = new byte[FIELD_SIZE][FIELD_SIZE];
        this.tempo = new byte[FIELD_SIZE][FIELD_SIZE];
        this.randomFieldGenerate = true;
        this.outputFile = outputFile;
    }

    /**
     * The class constructor takes as arguments the path to the file with the initial configuration of the field,
     * the path to the file to record the result, as well as the number of iterations of the calculation.
     *
     * @param filePath       - The path to the file with the initial field configuration. Missing a file will trigger
     *                       an IOException.
     * @param outputFile     - file path for recording calculation results. If the file is missing, it will be created.
     *                       Each time the file is overwritten.
     * @param ITERATIONS_NUM - number of iterations of field recalculation. The number is greater than 0,
     *                       otherwise the default number of iterations will be set to 10.
     */
    public GameOfLive(String filePath, String outputFile, String ITERATIONS_NUM) {
        readFile(filePath);
        this.ITERATIONS_NUM = (Integer.parseInt(ITERATIONS_NUM) > 0) ? Integer.parseInt(ITERATIONS_NUM) : 10;

        this.FIELD_SIZE = tempo.length;
        this.counter = 0;
        this.fieldSplitIndex = new int[][]{{0, 0, FIELD_SIZE / 2, FIELD_SIZE / 2},
                {FIELD_SIZE / 2, 0, FIELD_SIZE, FIELD_SIZE / 2},
                {0, FIELD_SIZE / 2, FIELD_SIZE / 2, FIELD_SIZE},
                {FIELD_SIZE / 2, FIELD_SIZE / 2, FIELD_SIZE, FIELD_SIZE}
        };
        this.lifeGeneration = new byte[FIELD_SIZE][FIELD_SIZE];
        byteArrayCopy(tempo, lifeGeneration);
        this.nextGeneration = new byte[FIELD_SIZE][FIELD_SIZE];
        randomFieldGenerate = false;
        this.outputFile = outputFile;
    }

    @Override
    public void run() {
        fillRandomField();
        byteArrayCopy(lifeGeneration, tempo);
        singleThreadCompute();
        resetCounterAndFields();
        multiThreadCompute();
    }


    /**
     * Multithreaded calculation of the state of the field. The original field is divided into 4 parts and
     * each is processed by a separate thread.
     */
    private void multiThreadCompute() {
        long currentTime = System.currentTimeMillis();
        CyclicBarrier barrier = new CyclicBarrier(4, () -> {
            for (int x = 0; x < FIELD_SIZE; x++) {
                System.arraycopy(nextGeneration[x], 0, lifeGeneration[x], 0, FIELD_SIZE);
            }
            if (counter == ITERATIONS_NUM - 1) {
                String timeCompute = "multiThreadCompute time = " + (System.currentTimeMillis() - currentTime);
                System.out.println(timeCompute);
                printField();
                writeResultToFile(true, timeCompute);
            }
            counter++;
        });

        ExecuteClass inn1 = new ExecuteClass(fieldSplitIndex[0][0], fieldSplitIndex[0][1],
                fieldSplitIndex[0][2], fieldSplitIndex[0][3]);
        ExecuteClass inn2 = new ExecuteClass(fieldSplitIndex[1][0], fieldSplitIndex[1][1],
                fieldSplitIndex[1][2], fieldSplitIndex[1][3]);
        ExecuteClass inn3 = new ExecuteClass(fieldSplitIndex[2][0], fieldSplitIndex[2][1],
                fieldSplitIndex[2][2], fieldSplitIndex[2][3]);
        ExecuteClass inn4 = new ExecuteClass(fieldSplitIndex[3][0], fieldSplitIndex[3][1],
                fieldSplitIndex[3][2], fieldSplitIndex[3][3]);

        Thread thread1 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn1.processOfLife();
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd1");


        Thread thread2 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn2.processOfLife();
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd2");


        Thread thread3 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn3.processOfLife();
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd3");


        Thread thread4 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn4.processOfLife();
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd4");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();


        //  Maybe such a record is better?

//        InnerClass objInnerClassMass[] = new InnerClass[4];
//        for (int i = 0; i < 4; i++) {
//            objInnerClassMass[i] = new InnerClass(fieldSplitIndex[i][0], fieldSplitIndex[i][1],
//                    fieldSplitIndex[i][2], fieldSplitIndex[i][3]);
//        }
//
//        Arrays.stream(objInnerClassMass).forEach((x) -> new Thread(() -> {
//            while (counter < ITERATIONS_NUM) {
//                try {
//                    x.processOfLife();
//                    barrier.await();
//                } catch (BrokenBarrierException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start());
    }


    /**
     * Single-threaded calculation of the state of the field.
     */
    private void singleThreadCompute() {
        long currentTime = System.currentTimeMillis();
        ExecuteClass singleThreadExecute = new ExecuteClass(0, 0, FIELD_SIZE, FIELD_SIZE);
        while (counter < ITERATIONS_NUM) {
            singleThreadExecute.processOfLife();
            for (int x = 0; x < FIELD_SIZE; x++) {
                System.arraycopy(nextGeneration[x], 0, lifeGeneration[x], 0, FIELD_SIZE);
            }
            counter++;
        }
        printField();
        String timeCompute = "singleThreadCompute time = " + (System.currentTimeMillis() - currentTime);
        writeResultToFile(false, timeCompute);
        System.out.println(timeCompute);
    }


    /**
     * A class containing general methods for calculating a single and multi-threaded game algorithm.
     */
    private class CountNeighbors {
        /**
         * The method determines the state of the current cell based on information about its neighbors.
         *
         * @param x - cell coordinate in a two-dimensional array along the x axis
         * @param y - cell coordinate in a two-dimensional array along the y axis
         */
        private void generate(int x, int y) {
            int count = countNeighbors(x, y);
            nextGeneration[x][y] = lifeGeneration[x][y];
            nextGeneration[x][y] = ((count == 3) || (nextGeneration[x][y] == 1)) ? (byte) 1 : (byte) 0;
            nextGeneration[x][y] = (((count >= 2) && (count <= 3)) && (nextGeneration[x][y] == 1)) ? (byte) 1 : (byte) 0;
        }

        /**
         * Calculates the number of living neighbors of the transferred cell.
         *
         * @param x  - cell coordinate in a two-dimensional array along the x axis
         * @param y- cell coordinate in a two-dimensional array along the y axis
         * @return - number of living neighbors
         */
        private int countNeighbors(int x, int y) {
            int count = 0;
            for (int dx = -1; dx < 2; dx++) {
                for (int dy = -1; dy < 2; dy++) {
                    int nX = x + dx;
                    int nY = y + dy;
                    nX = (nX < 0) ? FIELD_SIZE - 1 : nX;
                    nY = (nY < 0) ? FIELD_SIZE - 1 : nY;
                    nX = (nX > FIELD_SIZE - 1) ? 0 : nX;
                    nY = (nY > FIELD_SIZE - 1) ? 0 : nY;
                    count += (lifeGeneration[nX][nY] == 1) ? 1 : 0;
                }
            }
            if (lifeGeneration[x][y] == 1) {
                count--;
            }
            return count;
        }
    }

    /**
     * The class recalculates the field according to the specified coordinates.
     */
    private class ExecuteClass {
        private final int x1, y1, x2, y2;

        ExecuteClass(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         * The method recalculates the field according to the specified coordinates.
         */
        private void processOfLife() {
            CountNeighbors computing = new CountNeighbors();
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    computing.generate(x, y);
                }
            }
        }
    }


    private void fillRandomField() {
        if (randomFieldGenerate) {
            for (int i = 0; i < FIELD_SIZE; i++) {
                for (int j = 0; j < FIELD_SIZE; j++) {
                    lifeGeneration[i][j] = (byte) (new Random().nextInt(2));
                }
            }
        }
    }

    private synchronized void printField() {
        System.out.println("**************************  lifeGeneration  *******************************");
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                System.out.print(((lifeGeneration[i][j] == 1) ? 1 : 0) + "  ");
            }
            System.out.println();
        }
    }

    private void writeResultToFile(boolean appendFile, String str) {
        try (BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(outputFile, appendFile))) {
            for (int i = 0; i < FIELD_SIZE; i++) {
                for (int j = 0; j < FIELD_SIZE; j++) {
                    bufferWriter.write(lifeGeneration[i][j] + "  ");
                }
                bufferWriter.write("\n");
            }
            bufferWriter.write(str);
            bufferWriter.write("\n");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void readFile(String path) {
        try (FileInputStream inStream = new FileInputStream(path)) {
            byte[] localMass = new byte[inStream.available()];
            inStream.read(localMass, 0, inStream.available());
            inStream.close();
            parseFile(localMass);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }


    private void parseFile(byte[] localMass) {
        int numElements = 0;
        int count = 0;
        for (int i = 0; i < localMass.length; i++) {
            if ((localMass[i] == 48) || (localMass[i] == 49)) {
                localMass[count] = (localMass[i] == 48) ? (byte) 0 : (byte) 1;
                numElements++;
                count++;
            }
            if (localMass[i] == (byte) 10) {
                numElements = 0;
            }
        }
        tempo = new byte[numElements][numElements];
        for (int i = 0; i < numElements * numElements; i++) {
            tempo[i / numElements][i % numElements] = localMass[i];
        }
        for (int i = 0; i < numElements; i++) {
            System.out.println(Arrays.toString(tempo[i]));
        }
    }


    private void resetCounterAndFields() {
        counter = 0;
        byteArrayCopy(tempo, lifeGeneration);
        nextGeneration = new byte[FIELD_SIZE][FIELD_SIZE];
    }

    private void byteArrayCopy(byte[][] from, byte[][] to) {
        for (int x = 0; x < FIELD_SIZE; x++) {
            System.arraycopy(from[x], 0, to[x], 0, FIELD_SIZE);
        }
    }
}



