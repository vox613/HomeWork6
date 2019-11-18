package innopolis.homework6;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class GameOfLive implements Runnable {

    private final int ITERATIONS_NUM;
    private final int FIELD_SIZE;
    private int counter;
    private final int[][] fieldSplitIndex;
    private byte[][] lifeGeneration;
    private byte[][] nextGeneration;
    private byte[][] tempo;
    private final boolean fileWrite;
    private final boolean randomFieldGenerate;
    private final String outputFile;


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
        this.fileWrite = true;
        this.randomFieldGenerate = true;
        this.outputFile = outputFile;
    }


    public GameOfLive(String filePath, String outputFile, int iterations, boolean fileWrite) {
        readFile(filePath);
        this.fileWrite = fileWrite;
        this.ITERATIONS_NUM = (iterations > 0) ? iterations : 10;

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


    @Override
    public void run() {
        fillRandomField();
        byteArrayCopy(lifeGeneration, tempo);
        singleThreadCompute();
        reset();
        multiThreadCompute();

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


    /**
     *
     */
    private class CountNeighbors {
        /**
         * @param x
         * @param y
         */
        private void generate(int x, int y) {
            int count = countNeighbors(x, y);
            nextGeneration[x][y] = lifeGeneration[x][y];
            nextGeneration[x][y] = ((count == 3) || (nextGeneration[x][y] == 1)) ? (byte) 1 : (byte) 0;
            nextGeneration[x][y] = (((count >= 2) && (count <= 3)) && (nextGeneration[x][y] == 1)) ? (byte) 1 : (byte) 0;
        }

        /**
         * @param x
         * @param y
         * @return
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
     *
     */
    private class InnerClass {
        private int x1, y1, x2, y2;

        InnerClass(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         *
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


    /**
     *
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

        InnerClass inn1 = new InnerClass(fieldSplitIndex[0][0], fieldSplitIndex[0][1],
                fieldSplitIndex[0][2], fieldSplitIndex[0][3]);
        InnerClass inn2 = new InnerClass(fieldSplitIndex[1][0], fieldSplitIndex[1][1],
                fieldSplitIndex[1][2], fieldSplitIndex[1][3]);
        InnerClass inn3 = new InnerClass(fieldSplitIndex[2][0], fieldSplitIndex[2][1],
                fieldSplitIndex[2][2], fieldSplitIndex[2][3]);
        InnerClass inn4 = new InnerClass(fieldSplitIndex[3][0], fieldSplitIndex[3][1],
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
     *
     */
    private void singleThreadCompute() {
        long currentTime = System.currentTimeMillis();
        CountNeighbors computing = new CountNeighbors();
        while (counter < ITERATIONS_NUM) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                for (int y = 0; y < FIELD_SIZE; y++) {
                    computing.generate(x, y);
                }
            }
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

    /**
     *
     */
    private void reset() {
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



