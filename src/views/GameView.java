package views;

import controller.Game;
import controller.GameLoop;
import controller.Login;
import model.Direction;
import model.LoginWorld;
import model.Sprite;
import model.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class GameView extends JFrame {
    public static final int HEIGHT = 768;
    public static final int WIDTH = 1024;
    public static final int P1 = 1;
    public static final int P2 = 2;
    private final Canvas canvas = new Canvas();
    private final Game game;
    private final Login login;
    private final GameLoop gameLoop;

    public GameView(Login login, Game game) throws HeadlessException {
        this.login = login;
        this.game = game;
        login.setView(canvas);
        game.setView(canvas);
        gameLoop = login.gameLoop;
    }

    public void launch() {
        // GUI Stuff
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(canvas);
        setSize(WIDTH, HEIGHT);
        setContentPane(canvas);
        setVisible(true);

        // Keyboard listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (gameLoop.running) {
                    case 0:
                        switch (keyEvent.getKeyCode()) {
                            case KeyEvent.VK_ENTER:
                                login.loginSuccess();
                                break;
                        }
                    case 1:
                        switch (keyEvent.getKeyCode()) {
                            case KeyEvent.VK_W:
                                game.moveKnight(P1, Direction.UP);
                                break;
                            case KeyEvent.VK_S:
                                game.moveKnight(P1, Direction.DOWN);
                                break;
                            case KeyEvent.VK_A:
                                game.moveKnight(P1, Direction.LEFT);
                                break;
                            case KeyEvent.VK_D:
                                game.moveKnight(P1, Direction.RIGHT);
                                break;
                            case KeyEvent.VK_E:
                                game.attack(P1);
                                break;
                            case KeyEvent.VK_SPACE:
                                game.jump(P1);
                                break;
                            case KeyEvent.VK_I:
                                game.moveKnight(P2, Direction.UP);
                                break;
                            case KeyEvent.VK_K:
                                game.moveKnight(P2, Direction.DOWN);
                                break;
                            case KeyEvent.VK_J:
                                game.moveKnight(P2, Direction.LEFT);
                                break;
                            case KeyEvent.VK_L:
                                game.moveKnight(P2, Direction.RIGHT);
                                break;
                            case KeyEvent.VK_U:
                                game.attack(P2);
                                break;
                        }
                        break;
                }

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                switch (gameLoop.running) {
                    case 0:
                        switch (keyEvent.getKeyCode()) {
                            case KeyEvent.VK_ENTER:
                                login.loginSuccess();
                                break;
                        }
                        break;
                    case 1:
                        switch (keyEvent.getKeyCode()) {
                            case KeyEvent.VK_W:
                                game.stopKnight(P1, Direction.UP);
                                break;
                            case KeyEvent.VK_S:
                                game.stopKnight(P1, Direction.DOWN);
                                break;
                            case KeyEvent.VK_A:
                                game.stopKnight(P1, Direction.LEFT);
                                break;
                            case KeyEvent.VK_D:
                                game.stopKnight(P1, Direction.RIGHT);
                                break;
                            case KeyEvent.VK_I:
                                game.stopKnight(P2, Direction.UP);
                                break;
                            case KeyEvent.VK_K:
                                game.stopKnight(P2, Direction.DOWN);
                                break;
                            case KeyEvent.VK_J:
                                game.stopKnight(P2, Direction.LEFT);
                                break;
                            case KeyEvent.VK_L:
                                game.stopKnight(P2, Direction.RIGHT);
                                break;
                        }
                        break;
                }
            }
        });
    }

    public static class Canvas extends JPanel implements GameLoop.View {
        private World world;
        private LoginWorld loginWorld;

        @Override
        public void render(World world) {
            this.world = world;
            repaint(); // ask the JPanel to repaint, it will invoke paintComponent(g) after a while.
        }

        @Override
        public void render(LoginWorld loginWorld) {
            this.loginWorld = loginWorld;
            repaint(); // ask the JPanel to repaint, it will invoke paintComponent(g) after a while.
        }

        @Override
        protected void paintComponent(Graphics g /*paintbrush*/) {
            super.paintComponent(g);
            // Now, let's paint
            g.setColor(Color.WHITE); // paint background with all white
            g.fillRect(0, 0, GameView.WIDTH, GameView.HEIGHT);

            if(world != null) world.render(g); // ask the world to paint itself and paint the sprites on the canvas
            else if(loginWorld != null) loginWorld.render(g);
        }
    }
}
