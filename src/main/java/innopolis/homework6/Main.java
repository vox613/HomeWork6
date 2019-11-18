
package innopolis.homework6;

import java.io.IOException;

/**
 * The main class of the program from which the launch of the "Game of Life".
 */
public class Main {
    /**
     * The main method of the program from which the launch of the "Game of Life" is carried out.
     *
     * @param args - Array of command line arguments.
     *             args [0] - the path to the file with the input data, the initial state of the playing field,
     *                        for example, ".\\resources\\input"
     *             args [1] - the path to the file with the output data, the final state of the playing field, and the
     *                        execution time of the single-threaded and multi-threaded implementation,
     *                        for example, ".\\resources\\output".
     *             args [2] - the number of iterations of the calculation.
     * @throws IOException - throws when reading a file fails or the file is missing.
     */
    public static void main(String[] args) throws IOException {
        //GameOfLive game = new GameOfLive(1000, 50, args[1]);
        GameOfLive game = new GameOfLive(args[0], args[1], args[2]);
        new Thread(game).start();
    }
}


