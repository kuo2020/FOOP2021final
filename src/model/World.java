package model;

import boss.Boss;
import dropItem.DropItem;
import dropItem.HpPotion;
import dropItem.MpPotion;
import dropItem.Ring;
import ironBoar.IronBoar;
import knight.HealthPointBar;
import knight.Knight;
import media.AudioPlayer;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import obstacles.Obstacle;
import obstacles.Obstacle1;
import obstacles.Obstacle2;
import obstacles.Obstacle3;
import starPixie.StarPixie;

import java.util.ArrayList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class World {
    private Image background;
    private List<Obstacle1> floors;
    private List<Obstacle2> stairs;
    private List<Obstacle3> rocks;
    int sx, sy; // left-down corner's axis of background
    private Knight player;
    private final List<Sprite> sprites = new CopyOnWriteArrayList<>();
    private final CollisionHandler collisionHandler;
    public static final String BGM = "bgm";
    public static final String BOSS_BGM = "boss";
    public static final String GAME_CLEAR = "gameclear";
    public Clip clip;
    private Image pause;
    private Boss boss;
    private boolean bossAppear = false;
    public ArrayList<DropItem> dropItems = new ArrayList<>();
    private boolean bossDies = false;

    public World(String backgroundName, List<Obstacle1> floors, List<Obstacle2> stairs, List<Obstacle3> rocks, CollisionHandler collisionHandler, Knight player, Sprite... sprites) {
        try {
            background = ImageIO.read(new File(backgroundName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // int imgW = background.getWidth(null), imgH = background.getHeight(null);
        this.floors = floors;
        this.stairs = stairs;
        this.rocks = rocks;
        sx = 0;
        sy = background.getHeight(null);
        this.player = player;
        this.collisionHandler = collisionHandler;
        addSprite(player);
        addSprites(sprites);

        try {
            pause = ImageIO.read(new File("assets/others/pause.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boss = new Boss(500, new Point(1700, 800));

    }

    public void playSound() {
        if(bossAppear) clip = AudioPlayer.playSoundsloop(BOSS_BGM);
        else clip = AudioPlayer.playSoundsloop(BGM);
    }

    public void update() {
        for (Sprite sprite : sprites) {
            sprite.update();
        }
        if(player.exp >= player.lv*100) player.LVup();
        // adjust position
        if(player.getX() <= 300) {
            sx = 0;
        }
        else if(player.getX() >= background.getWidth(null)-1024+300) {
            sx = background.getWidth(null)-1024;
        }
        else{
            sx = player.getX()-300;
        }
        if(sx < 0) sx = 0;
        if(sx > background.getWidth(null)-1024) sx = background.getWidth(null)-1024;

        //if(player.jumpStep >= 0) return;

        if(player.getY() >= background.getHeight(null)-300) {
            sy = background.getHeight(null);
        }
        else if(player.getY() <= 300) {
            sy = background.getHeight(null)-768;
        }
        else{
            sy = player.getY()+768-300;
        }
        if(sy < 768) sy = 768;
        if(sy > background.getHeight(null)) sy = background.getHeight(null);
    }

    public void addSprites(Sprite... sprites) {
        stream(sprites).forEach(this::addSprite);
    }

    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
        sprite.setWorld(this);
    }

    public void removeSprite(Sprite sprite) {
        boolean monsterExist = false;
        sprites.remove(sprite);
        for (int i = 0; i < sprites.size(); i++){
            if (sprites.get(i) instanceof Dangerous) {
                monsterExist = true;
                break;
            }
        }
        if (!monsterExist && !bossAppear) {
            bossAppear = true;
            addSprite(boss);
            AudioPlayer.stopSounds(clip);
            clip = AudioPlayer.playSoundsloop(BOSS_BGM);
        }
        //sprite.setWorld(null);
        if (sprite instanceof IronBoar) {
            dropItems.add(new MpPotion(sprite.getBody().getLocation().x, sprite.getBody().getLocation().y+sprite.getBody().height));
        }
        else if (sprite instanceof StarPixie) {
            dropItems.add(new HpPotion(sprite.getBody().getLocation().x, sprite.getBody().getLocation().y+sprite.getBody().height));
        }
        else if (sprite instanceof Boss) {
            bossDies = true;
            AudioPlayer.stopSounds(clip);
            AudioPlayer.playSounds(GAME_CLEAR);
            dropItems.add(new Ring(sprite.getBody().getLocation().x, sprite.getBody().getLocation().y+sprite.getBody().height));
        }
    }

    public ArrayList<DropItem> getDropItems() {
        return dropItems;
    }
    public void removeItem(DropItem dropItem) {
        dropItems.remove(dropItem);
    }

    public void move(Sprite from, Dimension offset) {
        Point originalLocation = new Point(from.getLocation());
        Rectangle originalBody = new Rectangle(from.getBody());
        from.getLocation().translate(offset.width, offset.height);

        Rectangle body = from.getBody();
        // collision detection
        for (Sprite to : sprites) {
            if (to != from && body.intersects(to.getBody())) {
                collisionHandler.handle(originalLocation, from, to);
            }
        }
        
        for (Obstacle1 to : floors) {
	    Rectangle range = new Rectangle(to.getLocation(), to.getSize());
            if (body.intersects(range)) {
                to.collisionHandler(originalLocation, from);
            }
        }

        for (Obstacle2 to : stairs) {
	    Rectangle range = new Rectangle(to.getLocation(), to.getSize());
            if (body.intersects(range)) {
                to.collisionHandler(originalLocation, from);
            } else if (from instanceof Knight) {
	        if (originalBody.x + body.width >= range.x && originalBody.x <= range.x + range.width && (body.x + body.width < range.x || body.x > range.x + range.width))
		    if (originalBody.y + body.height < range.y + range.height && range.y - originalBody.y - body.height < 20)
			((Knight)from).fallCount = 0;  //start falling
	    }
        }

        for (Obstacle3 to : rocks) {
	    Rectangle range = new Rectangle(to.getLocation(), to.getSize());
            if (body.intersects(range)) {
                to.collisionHandler(originalLocation, from);
            } else if (from instanceof Knight) {
	        if (originalBody.x + body.width >= range.x && originalBody.x <= range.x + range.width && (body.x + body.width < range.x || body.x > range.x + range.width))
		    if (originalBody.y + body.height < range.y + range.height && range.y - originalBody.y - body.height < 20)
			((Knight)from).fallCount = 0;  //start falling
            }
        }
    }

    public void jump(Sprite from, Dimension offset) {
        Point originalLocation = new Point(from.getLocation());
        from.getLocation().translate(offset.width, offset.height);

        Rectangle body = from.getBody();
        // collision detection
        for (Sprite to : sprites) {
            if (to != from && body.intersects(to.getBody())) {
                collisionHandler.handle(originalLocation, from, to);
            }
        }
        
        for (Obstacle1 to : floors) {
	    Rectangle range = new Rectangle(to.getLocation(), to.getSize());
            if (body.intersects(range)) {
                to.collisionHandler(originalLocation, from);
            }
        }

        for (Obstacle2 to : stairs) {
	    Rectangle range = new Rectangle(to.getLocation(), to.getSize());
            if (body.intersects(range)) {
                to.collisionHandler(originalLocation, from);
            }
        }

        for (Obstacle3 to : rocks) {
	    Rectangle range = new Rectangle(to.getLocation(), to.getSize());
            if (body.intersects(range)) {
                to.collisionHandler(originalLocation, from);
            }
        }
    }

    public Collection<Sprite> getSprites(Rectangle area) {
        return sprites.stream()
                .filter(s -> area.intersects(s.getBody()))
                .collect(toSet());
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public Sprite getPlayer() { return player; }

    public Image getBackground() { return background; }

    // Actually, directly couple your model with the class "java.awt.Graphics" is not a good design
    // If you want to decouple them, create an interface that encapsulates the variation of the Graphics.
    public void render(Graphics g) {
        int sxtemp = sx, sytemp = sy;
        g.drawImage(background, 0, 0, 1024, 768, sxtemp, sytemp-768, sxtemp+1024, sytemp, null);
        
        for (Obstacle1 obstacle : floors) {
	    Rectangle range = new Rectangle(obstacle.getLocation(), obstacle.getSize());
            g.drawImage(obstacle.getImage(), range.x - sx              , range.y - sy + 768, range.width, range.height, null);
            //g.drawImage(obstacle, 666, 444, obstacle.getWidth(), obstacle.getHeight(), null);
	}
	for (Obstacle2 stair : stairs) {
	    Rectangle range = new Rectangle(stair.getLocation(), stair.getSize());
            if (stair.getFace() == Direction.RIGHT) {
                g.drawImage(stair.getImage(), range.x + range.width - sx, range.y - sy + 768, -range.width, range.height, null);
            } else {
                g.drawImage(stair.getImage(), range.x - sx              , range.y - sy + 768, range.width, range.height, null);
            }
            //g.drawImage(obstacle, 666, 444, obstacle.getWidth(), obstacle.getHeight(), null);
	}
	for (Obstacle3 obstacle : rocks) {
	    Rectangle range = new Rectangle(obstacle.getLocation(), obstacle.getSize());
            g.drawImage(obstacle.getImage(), range.x - sx              , range.y - sy + 768, range.width, range.height, null);
	}
	
        for (Sprite sprite : sprites) {
            //System.out.println(sprite.location);
            //System.out.printf("%d %d\n", sxtemp, sytemp);
            sprite.setLocation(new Point(sprite.getX()-sxtemp, sprite.getY()-sytemp+768));
            //System.out.printf("%d %d\n", sxtemp, sytemp);
            sprite.render(g);
            sprite.setLocation(new Point(sprite.getX()+sxtemp, sprite.getY()+sytemp-768));
        }
        for(DropItem dropItem : dropItems) {
            dropItem.setLocation(new Point(dropItem.getLocation().x-sxtemp, dropItem.getLocation().y-sytemp+768));
            dropItem.render(g);
            dropItem.setLocation(new Point(dropItem.getLocation().x+sxtemp, dropItem.getLocation().y+sytemp-768));
        }
        sx = sxtemp; sy = sytemp;

        BufferedImage bg = resizeImage((BufferedImage)background, background.getWidth(null)/16, background.getHeight(null)/16);
        g.drawImage(bg, 0, 0, null);
        for (Sprite sprite : sprites) {
            g.setColor(Color.RED);
            Point p = sprite.getLocation();
            if(sprite instanceof Boss) g.fillOval((int)(p.getX()/16), (int)(p.getY()/16), 16, 16);
            else g.fillOval((int)(p.getX()/16), (int)(p.getY()/16), 6, 6);
        }
        for(Obstacle obstacle : floors) {
            Point p = obstacle.getLocation();
            int w = obstacle.getImage().getWidth(null), h = obstacle.getImage().getHeight(null);
            BufferedImage img = resizeImage((BufferedImage)obstacle.getImage(), w/16, h/16);
            g.drawImage(img, (int)(p.getX()/16), (int)(p.getY()/16), null);
        }
        for(Obstacle obstacle : rocks) {
            Point p = obstacle.getLocation();
            int w = obstacle.getImage().getWidth(null), h = obstacle.getImage().getHeight(null);
            BufferedImage img = resizeImage((BufferedImage)obstacle.getImage(), w/16, h/16);
            g.drawImage(img, (int)(p.getX()/16), (int)(p.getY()/16), null);
        }
        for(Obstacle obstacle : stairs) {
            Point p = obstacle.getLocation();
            int w = obstacle.getImage().getWidth(null), h = obstacle.getImage().getHeight(null);
            BufferedImage img = resizeImage((BufferedImage)obstacle.getImage(), w/16, h/16);
            g.drawImage(img, (int)(p.getX()/16), (int)(p.getY()/16), null);
        }


        g.setColor(Color.green);
        Point p = player.getLocation();
        g.fillOval((int)(p.getX()/16), (int)(p.getY()/16), 8, 8);
        g.setColor(Color.black);
        g.drawOval((int)(p.getX()/16), (int)(p.getY()/16), 8, 8);

        g.drawImage(pause, 950, 0, null);

        g.setColor(Color.pink);
        g.fillRect(350, 650, 300, 80);
        g.setColor(Color.black);
        g.drawRect(350, 650, 300, 80);
        Image pl;
        try {
            pl = ImageIO.read(new File("assets/others/player.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        g.drawImage(pl, 365, 655, null);
        g.setColor(Color.black);
        g.setFont(new Font("TimesRoman", Font.BOLD, 16));
        g.drawString("LV. " + player.lv, 415, 700); //player.exp + "/" + player.lv*100
        g.drawString("exp ", 480, 675);

        g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        g.fillRect(510, 660, 120, 15);
        g.setColor(Color.YELLOW);
        g.fillRect(510, 660, (int) (player.exp * 120 / (player.lv*100)), 15);
        g.setColor(Color.BLACK);
        g.drawRect(510, 660, 120, 15);
        g.drawString(player.exp + "/" + player.lv*100, 520, 675);

        HealthPointBar hpBar = player.hpBar;
        int width = (int) (hpBar.getHp() * 120 / player.KNIGHT_HP);
        int widthMp = (int) (hpBar.getMp() * 120 / player.KNIGHT_MP);
        g.setColor(Color.RED);
        g.fillRect(510, 685, 120, 15);
        g.setColor(Color.GREEN);
        g.fillRect(510, 685, width, 15);
        g.setColor(Color.WHITE);
        g.fillRect(510, 705, 120, 15);
        g.setColor(Color.BLUE);
        g.fillRect(510, 705, widthMp, 15);
        g.setColor(Color.black);
        g.drawRect(510, 685, 120, 15);
        g.drawRect(510, 705, 120, 15);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        g.setColor(Color.black);
        g.drawString("HP ", 480, 700);
        g.drawString("MP ", 480, 720);
        g.drawString(hpBar.getHp() + "/" + player.KNIGHT_HP, 520, 700);
        g.drawString(hpBar.getMp() + "/" + player.KNIGHT_MP, 520, 720);


        //boss
        if(bossAppear && !bossDies) {
            g.setColor(Color.pink);
            g.fillRect(350, 0, 300, 80);
            g.setColor(Color.black);
            g.drawRect(350, 0, 300, 80);
            Image bo;
            try {
                bo = ImageIO.read(new File("assets/others/boss.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            g.drawImage(bo, 560, -5, null);
            g.setColor(Color.black);

            g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
            hpBar = boss.hpBar;
            width = (int) (hpBar.getHp() * 150 / boss.HP);
            g.setColor(Color.RED);
            g.fillRect(420, 35, 150, 15);
            g.setColor(Color.GREEN);
            g.fillRect(420, 35, width, 15);
            g.setColor(Color.black);
            g.drawRect(420, 35, 150, 15);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
            g.setColor(Color.black);
            g.drawString("HP ", 380, 50);
            g.drawString(hpBar.getHp() + "/" + boss.HP, 425, 50);

        }

        if(player.levelUping > 0) {
            player.levelUping--;
            Image lvup;
            try {
                lvup = ImageIO.read(new File("assets/lvup/" + player.lv + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            g.drawImage(lvup, 290, 80, null);
        }

        g.setFont(new Font("TimesRoman", Font.BOLD, 16));
        g.setColor(Color.gray);
        if(player.lv >= 1) {
            Image skillu;
            try {
                skillu = ImageIO.read(new File("assets/skillicon/skillu.jpg"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            g.drawImage(skillu, 0, 680, null);
            g.drawString("u", 20, 675);
        }
        if(player.lv >= 2) {
            Image twoStepJump;
            try {
                twoStepJump = ImageIO.read(new File("assets/skillicon/2stepjump.jpg"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            g.drawImage(twoStepJump, 50, 680, null);
            g.drawString("space", 50, 675);
        }
        if(player.lv >= 3) {
            Image skilli;
            try {
                skilli = ImageIO.read(new File("assets/skillicon/skilli.jpg"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            g.drawImage(skilli, 100, 680, null);
            g.drawString("i", 120, 675);
        }
        if(player.lv >= 4) {
            Image skillo;
            try {
                skillo = ImageIO.read(new File("assets/skillicon/heal.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            g.drawImage(skillo, 150, 680, null);
            g.drawString("o", 170, 675);
        }

        int hp = player.hpPotionCount, mp = player.mpPotionCount, ring = player.ringCount;
        g.setColor(Color.yellow);
        g.fillRect(860, 680, 150, 50);
        g.setColor(Color.black);
        g.drawRect(860, 680, 50, 50);
        g.drawRect(910, 680, 50, 50);
        g.drawRect(960, 680, 50, 50);

        g.setFont(new Font("TimesRoman", Font.BOLD, 16));
        g.setColor(Color.black);
        Image im;
        try {
            im = ImageIO.read(new File("assets/dropitem/33.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        g.drawImage(im, 860, 680, null);
        g.setColor(Color.gray);
        g.drawString("1", 895, 695);
        g.setColor(Color.black);
        g.drawString("" + hp, 895, 730);

        try {
            im = ImageIO.read(new File("assets/dropitem/22.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        g.drawImage(im, 910, 680, null);
        g.setColor(Color.gray);
        g.drawString("2", 945, 695);
        g.setColor(Color.black);
        g.drawString("" + mp, 945, 730);

        try {
            im = ImageIO.read(new File("assets/dropitem/11.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        g.drawImage(im, 960, 680, null);
        g.setColor(Color.gray);
        g.drawString("3", 995, 695);
        g.setColor(Color.black);
        g.drawString("" + ring, 995, 730);
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
