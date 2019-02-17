package test;

import core.Connect4;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

class Connect4Test {

    private final Connect4 game = new Connect4("P");

    @org.junit.jupiter.api.Test
    void gameIsActive() {
        assertTrue(game.gameIsActive());
    }

    @org.junit.jupiter.api.Test
    void endGame() {
        Connect4 atomicGame = new Connect4("P"); //we don't want this one to stick around.
        atomicGame.endGame();
        assertFalse(atomicGame.gameIsActive());
    }

    @org.junit.jupiter.api.Test
    void multiplayerGame() {
        assertTrue(game.multiplayerGame());
    }

    @org.junit.jupiter.api.Test
    void switchTurns() {
        boolean playerOnesTurn = game.currentlyPlayerOnesTurn();
        game.switchTurns();
        assertNotEquals(playerOnesTurn,game.currentlyPlayerOnesTurn());
    }

    @org.junit.jupiter.api.Test
    void getActivePlayer() {
        core.Connect4.Player player = game.getActivePlayer();
        assertNotNull(player);
    }

    @org.junit.jupiter.api.Test
    void getPlayer() {
        core.Connect4.Player player = game.getPlayer(1);
        assertNotNull(player);
    }

    @org.junit.jupiter.api.Test
    void currentlyPlayerOnesTurn() {
        if(!game.currentlyPlayerOnesTurn())
            game.switchTurns();
        assertTrue(game.currentlyPlayerOnesTurn());
    }

    @org.junit.jupiter.api.Test
    void move() {
        Connect4.Player player = game.getPlayer(1);
        if(!game.currentlyPlayerOnesTurn())
            game.switchTurns();

        assertFalse(game.move(player,-1));
        assertFalse(game.move(player,8));
        assertTrue(game.move(player,1));
    }

    @org.junit.jupiter.api.Test
    void columnIsEmpty() {
        Connect4.Player player = game.getPlayer(1);
        if(!game.currentlyPlayerOnesTurn())
            game.switchTurns();

        game.move(player,1);
        assertFalse(game.columnIsEmpty(1));
        assertTrue(game.columnIsEmpty(6));
    }

    @org.junit.jupiter.api.Test
    void columnIsFull() {
        Connect4.Player player = game.getPlayer(1);
        if(!game.currentlyPlayerOnesTurn())
            game.switchTurns();
        game.move(player,2);
        game.move(player,2);
        game.move(player,2);
        game.move(player,2);
        game.move(player,2);
        game.move(player,2);

        assertFalse(game.columnIsFull(1));
        assertTrue(game.columnIsFull(2));
    }

    @org.junit.jupiter.api.Test
    void getTopPieceInColumn() {
        Connect4.Player player = game.getPlayer(1);
        game.move(player,2);
        Connect4.GamePiece piece = game.getTopPieceInColumn(2);
        assertNotNull(piece);
    }

    @org.junit.jupiter.api.Test
    void matchingMovesHorizontal() {
        Connect4.Player player = game.getPlayer(1);
        game.move(player,1);
        game.move(player,3);
        assertTrue(game.matchingMovesHorizontal("left",5,2)>=0);
        assertTrue(game.matchingMovesHorizontal("right",5,2)>=0);
    }

    @org.junit.jupiter.api.Test
    void matchingMovesVertical() {
        Connect4.Player player = game.getPlayer(1);
        game.move(player,4);
        assertTrue(game.matchingMovesVertical(game.getTopPieceInColumn(4).getRow(),4)>=0);
    }

    @org.junit.jupiter.api.Test
    void matchingMovesDiagonal() {
        Connect4.Player player = game.getPlayer(1);
        game.move(player,3);
        game.move(player,4);
        game.move(player,4);
        game.move(player,5);
        game.move(player,5);
        game.move(player,5);
        Connect4.GamePiece piece = game.getTopPieceInColumn(5);
        System.out.println("Piece Info: "+piece.getRow()+", "+piece.getCol());
        assertTrue(game.matchingMovesDiagonal("DDL",piece.getRow(),piece.getCol())>=0);
        assertFalse(game.matchingMovesDiagonal("DUR",5,2)>0);
    }

    @org.junit.jupiter.api.Test
    void checkWinConditions() {
        Connect4.Player player = game.getPlayer(1);
        game.move(player,2);
        game.move(player,2);
        game.move(player,2);
        game.move(player,2);
        assertTrue(game.checkWinConditions(game.getTopPieceInColumn(2)));

        assertThrows(NoSuchElementException.class, () -> {
            game.checkWinConditions(game.getTopPieceInColumn(0));
        });
    }

    @org.junit.jupiter.api.Test
    void getItemInSlot() {
        Connect4.Player player = game.getPlayer(1);
        game.move(player,2);
        assertNotNull(game.getItemInSlot(5,2));
    }

    @org.junit.jupiter.api.Test
    void columnHasOpenSlots() {
        assertTrue(game.columnHasOpenSlots(6));
    }
}