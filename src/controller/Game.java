package controller;

import dropItem.DropItem;
import knight.Knight;
import model.Direction;
import model.Sprite;
import model.World;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Game {
    private final Knight p1;
    private World world;
    GameLoop.View view;
    GameLoop gameLoop;
    public int gameID;

    public Game(World world, Knight p1, int ID) {
        this.p1 = p1;
        this.world = world;
        this.gameID = ID;
    }

    public void setGameLoop(GameLoop gameLoop) { this.gameLoop = gameLoop; }

    public void setView(GameLoop.View view) {
        this.view = view;
    }

    public void moveKnight(int playerNumber, Direction direction) {
        getPlayer(playerNumber).move(direction);
    }

    public void stopKnight(int playerNumber, Direction direction) {
        getPlayer(playerNumber).stop(direction);
    }

    public void attack(int playerNumber) {
        getPlayer(playerNumber).attack();
    }

    public void skillU(int playerNumber) {
        getPlayer(playerNumber).skillU();
    }
    public void skillI(int playerNumber) {
        getPlayer(playerNumber).skillI();
    }
    public void jump(int playerNumber) {
        if(getPlayer(playerNumber).fallCount >= 0) return;
        getPlayer(playerNumber).jumpLV++;
        if(getPlayer(playerNumber).jumpLV < 3) getPlayer(playerNumber).jump(0);
        else getPlayer(playerNumber).jumpLV--;
    }

    public void pick(int playerNumber) {
        getWorld().removeItem(getPlayer(playerNumber).pickItem());
    }
    public void useHpPotion(int playerNumber) {
        getPlayer(playerNumber).useHpPotion();
    }
    public void useMpPotion(int playerNumber) {
        getPlayer(playerNumber).useMpPotion();
    }

    public Knight getPlayer(int playerNumber) {
        return playerNumber == 1 ? p1 : null;
    }

    public World getWorld() {
        return world;
    }

    public void restart(World world) {
        this.world = world;
    }
}
