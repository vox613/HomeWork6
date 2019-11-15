package innopolis.homework6;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {

    private static final int ITERATIONS_NUM = 2000;
    private final static int FIELD_SIZE = 10000;
    private static int counter = 0;
    private static boolean[][] lifeGeneration = new boolean[FIELD_SIZE][FIELD_SIZE];
    private static boolean[][] nextGeneration = new boolean[FIELD_SIZE][FIELD_SIZE];
    private static boolean[][] tempo = new boolean[FIELD_SIZE][FIELD_SIZE];


    public static void main(String[] args) {
        fillRandomField();
        arrayCopy(lifeGeneration, tempo);
        singleThreadCompute();
        reset();
        multiThreadCompute();
    }


    private static void reset() {
        counter = 0;
        arrayCopy(tempo, lifeGeneration);
        nextGeneration = new boolean[FIELD_SIZE][FIELD_SIZE];
    }

    private static void arrayCopy(boolean[][] from, boolean[][] to) {
        for (int x = 0; x < FIELD_SIZE; x++) {
            System.arraycopy(from[x], 0, to[x], 0, FIELD_SIZE);
        }
    }


    private static class InnerClass {

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
                    count += (lifeGeneration[nX][nY]) ? 1 : 0;
                }
            }
            if (lifeGeneration[x][y]) {
                count--;
            }
            return count;
        }

        private void processOfLife(int x1, int y1, int x2, int y2) {
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    int count = countNeighbors(x, y);
                    nextGeneration[x][y] = lifeGeneration[x][y];
                    nextGeneration[x][y] = (count == 3) ? true : nextGeneration[x][y];
                    nextGeneration[x][y] = ((count < 2) || (count > 3)) ? false : nextGeneration[x][y];
                }
            }
        }
    }


    private static void multiThreadCompute() {

        long currentTime = System.currentTimeMillis();
        CyclicBarrier barrier = new CyclicBarrier(4, () -> {
            for (int x = 0; x < FIELD_SIZE; x++) {
                System.arraycopy(nextGeneration[x], 0, lifeGeneration[x], 0, FIELD_SIZE);
            }
            if (counter == ITERATIONS_NUM - 1) {
                System.out.println("multiThreadCompute time = " + (System.currentTimeMillis() - currentTime));
                //printField();
            }
            counter++;
        });

        InnerClass inn1 = new InnerClass();
        InnerClass inn2 = new InnerClass();
        InnerClass inn3 = new InnerClass();
        InnerClass inn4 = new InnerClass();

        Thread thread1 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn1.processOfLife(0, 0, FIELD_SIZE / 2, FIELD_SIZE / 2);
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd1");


        Thread thread2 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn2.processOfLife(FIELD_SIZE / 2, 0, FIELD_SIZE, FIELD_SIZE / 2);
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd2");


        Thread thread3 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn3.processOfLife(0, FIELD_SIZE / 2, FIELD_SIZE / 2, FIELD_SIZE);
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "thrd3");


        Thread thread4 = new Thread(() -> {
            while (counter < ITERATIONS_NUM) {
                try {
                    inn4.processOfLife(FIELD_SIZE / 2, FIELD_SIZE / 2, FIELD_SIZE, FIELD_SIZE);
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
    }


    private static int countNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                int nX = x + dx;
                int nY = y + dy;
                nX = (nX < 0) ? FIELD_SIZE - 1 : nX;
                nY = (nY < 0) ? FIELD_SIZE - 1 : nY;
                nX = (nX > FIELD_SIZE - 1) ? 0 : nX;
                nY = (nY > FIELD_SIZE - 1) ? 0 : nY;
                count += (lifeGeneration[nX][nY]) ? 1 : 0;
            }
        }
        if (lifeGeneration[x][y]) {
            count--;
        }
        return count;
    }


    private static void singleThreadCompute() {
        long currentTime = System.currentTimeMillis();
        while (counter < ITERATIONS_NUM) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                for (int y = 0; y < FIELD_SIZE; y++) {
                    int count = countNeighbors(x, y);
                    nextGeneration[x][y] = lifeGeneration[x][y];
                    nextGeneration[x][y] = (count == 3) ? true : nextGeneration[x][y];
                    nextGeneration[x][y] = ((count < 2) || (count > 3)) ? false : nextGeneration[x][y];
                }
            }
            for (int x = 0; x < FIELD_SIZE; x++) {
                System.arraycopy(nextGeneration[x], 0, lifeGeneration[x], 0, FIELD_SIZE);
            }
            //printField();
            counter++;
        }
        //printField();
        System.out.println("singleThreadCompute time = " + (System.currentTimeMillis() - currentTime));
    }


    private static void fillRandomField() {
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                lifeGeneration[i][j] = (new Random().nextInt(2) == 1);
            }
        }
        System.out.println("---------------Start field----------------");
        //printField();
    }

    private static synchronized void printField() {
        System.out.println("**************************lifeGeneration*******************************");
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                System.out.print((lifeGeneration[i][j] ? 1 : 0) + "  ");
            }
            System.out.println();
        }
/*
        System.out.println("**************************nextGeneration*******************************" + Thread.currentThread().getName());
        for (int i = 0; i < LIFE_SIZE; i++) {
            for (int j = 0; j < LIFE_SIZE; j++) {
                System.out.print((nextGeneration[i][j] ? 1 : 0) + "  ");
            }
            System.out.println();
        }
*/
    }


}


