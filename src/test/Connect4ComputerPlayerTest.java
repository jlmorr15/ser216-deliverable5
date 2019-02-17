package test;

import core.Connect4;
import core.Connect4ComputerPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Connect4ComputerPlayerTest {
    private final Connect4 game = new Connect4("P");

    @Test
    void move() {
        core.Connect4ComputerPlayer player = new Connect4ComputerPlayer(game.getPlayer(1));
        int pcMove = player.move(game);
        assertTrue(pcMove>=1 && pcMove<=7);
    }
}