
package innopolis.homework6;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //GameOfLive game = new GameOfLive(1000, 50, args[1]);
        GameOfLive game = new GameOfLive(args[0], args[1], Integer.parseInt(args[2]), Boolean.parseBoolean(args[3]));
        new Thread(game).start();
    }
}


