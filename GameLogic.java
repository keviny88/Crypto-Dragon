package cryptoDragon1;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class GameLogic {

	final int WIDTH = 1200;
	final int HEIGHT = 800;

	public int ticks, speed, yMotion, score;
	public boolean gameOver, started;
	public Rectangle dragon;
	public ArrayList<Polygon> columns;
	public ArrayList<Ellipse2D.Double> coins;
	public Random random;
	public BufferedImage backgroundImage1, backgroundImage2;
	public int scrollX1, scrollY1, scrollX2 = WIDTH, scrollY2, scrollB1 = WIDTH, scrollB2;
	public BufferedImage spriteImageUp, spriteImageDown, spriteImageDead, spriteImageStart, coinImage, rockBorderImage1, rockBorderImage2;
	public Font customFontTitle, customFontClick = null;

	public GameLogic() throws IOException {
		setTicks(0);

		// create a new instance of the Random class
		random = new Random(); 
		
		// read in the background images for parallax
		backgroundImage1 = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/Cave_Stage_Background3.png"));
		backgroundImage2 = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/Cave_Stage_Background3.png"));

		// read in sprite images
		spriteImageDown = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/blackDragon.png"));
		spriteImageUp = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/BlackDragonUp.png"));
		spriteImageDead = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/BlackDragonDead.png"));
		spriteImageStart = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/BlackDragon.png"));

		// read in coin image
		coinImage = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/bitCoin2.png"));
		coinImage = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/bitCoin2.png"));

		// read in rock border background images for parallax
		rockBorderImage1 = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/transparentBack2.png"));
		rockBorderImage2 = ImageIO.read(new File("/Applications/XAMPP/htdocs/MySideScroller/bin/cryptoDragon1/transparentBack2.png"));

		// create the main objects
		dragon = new Rectangle(WIDTH / 2 - 180, HEIGHT / 2 - 50, 80, 40);
		columns = new ArrayList<Polygon>();
		coins = new ArrayList<Ellipse2D.Double>();

		// generate the initial columns
		generateColumns(true);

		// create new font
		newFont();
	}

	public void update(int deltaTime) {

		// if game isn't over, but has started, start the moving components of the game
		ticks++;

		if (!gameOver && started) {

			// speed
			speed = 11;
			scrollX1 -= 1;
			scrollX2 -= 1;
			scrollB1 -= speed;
			scrollB2 -= speed;

			// calculate frequency of coins
			if (random.nextInt(1000) % 50 == 0) {
				addCoin(true);
			}

			// score is based on time lasted in game and distance traveled
			if (ticks % 40 == 0) {
				score++;
			}

			// land on ground => die!
			if (dragon.x <= 0 || dragon.y >= 650)
				gameOver = true;

			// simulate movement in the horizontal direction
			for (int i = 0; i < columns.size(); i++) {
				Polygon column = columns.get(i);
				column.translate(-speed, 0);
			}

			// if ticks is even and yMotion is less than 15 (simulates gravity
			// for the dragon)
			if (ticks % 2 == 0 && yMotion < 15)
				yMotion += 2;

			// Changing speed of coins
			for (int i = 0; i < coins.size(); i++) {
				Ellipse2D.Double coin = coins.get(i);
				coin.x -= speed - 2;
			}

			for (int i = 0; i < coins.size(); i++) {
				Ellipse2D.Double coin = coins.get(i);
				if ((coin.x + coin.width) < 0) {
					coins.remove(coin);
				}
			}

			// iterate through columns and delete the ones that finish going by
			for (int i = 0; i < columns.size(); i++) {
				Polygon column = columns.get(i);

				// moved left outside of window
				if (column.xpoints[2] < 0) {
					columns.remove(column);

					// add new column to list to keep the game going infinitely
					// until "game over"
					if (column.ypoints[0] == 0)
						addColumn(false);
				}
			}

			// move the dragon down
			dragon.y += yMotion;

			// collision
			for (Polygon column : columns) {

				// if dragon collides into a column
				if (column.intersects(dragon)) {
					gameOver = true;

					int height = column.ypoints[1] - column.ypoints[0];

					// when dragon falls, column moves dragon
					if (dragon.x <= column.xpoints[0])
						dragon.x = column.xpoints[0] - dragon.width;
					else {
						if (column.ypoints[0] != 0)
							dragon.y = column.ypoints[0] - dragon.height;
						else if (dragon.y < height)
							dragon.y = height;
					}
				}
			}

			// CHECKING COLLISONS FOR COINS
			for (int i = 0; i < coins.size(); i++) {
				Ellipse2D.Double coin = coins.get(i);
				if (coin.intersects(dragon)) {
					score += 10;
					coins.remove(coin);
				}
			}

			// if dragon goes out of bounds
			if (dragon.y > HEIGHT - 120 || dragon.y < 0)
				gameOver = true;

			if (dragon.y + yMotion >= HEIGHT - 120)
				dragon.y = HEIGHT - 120 - dragon.height;
		}

		// gameOver case
		else if (gameOver) {

			// if ticks is even and yMotion is less than 15 (simulates gravity
			// for the dragon)
			if (ticks % 2 == 0 && yMotion < 15)
				yMotion += 2;

			// move the dragon down for death
			dragon.y += yMotion;
		}

		// always scroll unless otherwise
		else {
			scrollX1 -= 1;
			scrollX2 -= 1;
		}
	}

	public void newFont() {
		try {
			customFontTitle = Font
					.createFont(
							Font.TRUETYPE_FONT,
							new File(
									"/Users/Dylanharcourt/Desktop/workspace/Flappy Dragon 3/src/flappyDragon/newfontw.ttf"))
					.deriveFont(90f);
			customFontClick = Font
					.createFont(
							Font.TRUETYPE_FONT,
							new File(
									"/Users/Dylanharcourt/Desktop/workspace/Flappy Dragon 3/src/flappyDragon/newfontw.ttf"))
					.deriveFont(18f);
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			ge.registerFont(Font
					.createFont(
							Font.TRUETYPE_FONT,
							new File(
									"/Users/Dylanharcourt/Desktop/workspace/Flappy Dragon 3/src/flappyDragon/newfontw.ttf")));
		} catch (IOException | FontFormatException e) {
			System.out.println(e);
		}
	}

	public void addCoin(boolean start) {
		int y = 80 + random.nextInt(600) - 120;
		coins.add(new Ellipse2D.Double(WIDTH + 300, y, 40, 50));
	}

	// helper method to generate 4 columns
	public void generateColumns(boolean generate) {
		addColumn(generate);
		addColumn(generate);
		addColumn(generate);
		addColumn(generate);
	}

	public void addColumn(boolean start) {

		// space between columns
		int space = 75;

		// width and height
		int width = 100 + random.nextInt(100);
		int height = 190 + random.nextInt(100);

		// starting columns before game begins
		if (start) {

			// lower spike
			columns.add(new Polygon(new int[] {
					WIDTH + width + columns.size() * 300, // x1
					WIDTH + width + columns.size() * 300 + (width / 2), // x2
					WIDTH + width + columns.size() * 300 + width }, // x3
					new int[] { HEIGHT - 120, HEIGHT - height - 120,
							HEIGHT - 120 }, 3));

			// higher spike
			width = 100 + random.nextInt(100);
			height = 190 + random.nextInt(100);
			columns.add(new Polygon(new int[] {
					WIDTH + width + (columns.size() - 1) * 300,
					WIDTH + width + (columns.size() - 1) * 300 + (width / 2),
					WIDTH + width + (columns.size() - 1) * 300 + width },
					new int[] { 0, height - space, 0 }, 3));

		}

		// columns after game is started
		else {

			// lower spike
			columns.add(new Polygon(new int[] {
					columns.get(columns.size() - 1).xpoints[0] + 600, // x1
					columns.get(columns.size() - 1).xpoints[0] + (width / 2)
							+ 600, // x2
					columns.get(columns.size() - 1).xpoints[0] + width + 600 }, // x3
					new int[] { HEIGHT - 120, HEIGHT - height - 120,
							HEIGHT - 120 }, 3));

			// higher spike
			width = 100 + random.nextInt(100);
			height = 190 + random.nextInt(100);

			columns.add(new Polygon(new int[] {
					columns.get(columns.size() - 1).xpoints[0],
					columns.get(columns.size() - 1).xpoints[0] + (width / 2),
					columns.get(columns.size() - 1).xpoints[0] + width },
					new int[] { 0, height - space, 0 }, 3));
		}
	}

	public void paintColumn(Graphics g, Polygon column) {
		g.setColor(Color.DARK_GRAY.darker());
		g.fillPolygon(column.xpoints, column.ypoints, 3);
	}

	public void paintCoin(Graphics g, Ellipse2D.Double coin) {
		// g.setColor(Color.yellow);
		int x1 = (int) coin.x;
		int x2 = (int) coin.y;
		int w = (int) coin.width;
		int h = (int) coin.height;
		g.fillOval(x1, x2, w, h);
	}

	public void jump() {

		// if not started, begin the game once clicked
		if (!started) {
			started = true;
		}

		// begin gravity unless game is over
		else if (!gameOver) {
			if (yMotion > 0)
				yMotion = 0;
			yMotion = -15;
		}
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public void keyLogic(KeyEvent e) {
		
		// set space bar as alt key for game play
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {

			// only move in the game
			jump();

			// pause until new press and game over animation finishes
			if (gameOver && dragon.y >= 800) {
				columns.clear();
				coins.clear();
				yMotion = 0;
				score = 0;
				generateColumns(true);
				gameOver = false;
				started = false;
				dragon = new Rectangle(WIDTH / 2 - 180, HEIGHT / 2 - 50, 80, 40);
			}
		}
	}

	public void mouseLogic() {

		// only move in the game
		jump();

		// pause until new click and game over animation finishes
		if (gameOver && dragon.y >= 800) {
			columns.clear();
			coins.clear();
			yMotion = 0;
			score = 0;
			generateColumns(true);
			gameOver = false;
			started = false;
			dragon = new Rectangle(WIDTH / 2 - 180, HEIGHT / 2 - 50, 80, 40);
		}
	}

	public void graphicsLogic(Graphics g) {

		// Background -- CHANGE LATER FOR GOT THEME -- current background is
		// SCROLLING -- YAY -- ALWAYS PRESENT ON GAME WINDOW
		g.drawImage(backgroundImage1, scrollX1, scrollY1,
				backgroundImage1.getWidth(), backgroundImage1.getHeight(), null);
		g.drawImage(backgroundImage2, scrollX2, scrollY2,
				backgroundImage2.getWidth(), backgroundImage2.getHeight(), null);

		// beginning of game
		if (!gameOver && !started) {
			g.setColor(Color.WHITE);
			g.setFont(customFontClick);
			g.drawString("Click To Play!", 500, HEIGHT / 2 - 140);
			g.setFont(customFontTitle);
			g.drawString("Crypto-Dragon", 101, HEIGHT / 2 - 220);
			g.drawImage(spriteImageStart, dragon.x - 70, dragon.y - 75, null);
			if (scrollX2 <= 0) {
				scrollX1 = 0;
				scrollX2 = WIDTH;
			}
			if (scrollB2 <= 0) {
				scrollB1 = 0;
				scrollB2 = WIDTH;
			}
		}

		// during game play
		else if (!gameOver && started) {

			if (scrollX2 <= 0) {
				scrollX1 = 0;
				scrollX2 = WIDTH;
			}

			if (scrollB2 <= 0) {
				scrollB1 = 0;
				scrollB2 = WIDTH;
			}

			// COLOR OF DRAGON RECT UNDER SPRITE
			// g.setColor(Color.RED.darker());
			// g.fillRect(dragon.x, dragon.y, dragon.width, dragon.height);

			// bottom layer
			g.drawImage(rockBorderImage1, scrollB1, 678, null);
			g.drawImage(rockBorderImage2, scrollB2, 678, null);

			// sprite movement (2 moves)
			if (yMotion >= 4)
				g.drawImage(spriteImageDown, dragon.x - 70, dragon.y - 75, null);
			else
				g.drawImage(spriteImageUp, dragon.x - 70, dragon.y - 45, null);

			// loop through columns and paint the spikes
			for (Polygon column : columns) {
				paintColumn(g, column);
			}

			// loop through columns and paint the coins
			for (Ellipse2D.Double coin : coins) {
				paintCoin(g, coin);
				g.drawImage(coinImage, (int) coin.x - 10, (int) coin.y - 10,
						null);
			}

			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", 1, 100));
			g.drawString(String.valueOf(score), WIDTH / 2 - 25, 100);
		}

		// game over
		else if (gameOver) {
			for (Polygon column : columns) {
				paintColumn(g, column);
			}
			g.setFont(customFontTitle);
			g.setColor(Color.WHITE);
			g.drawString("Game Over!", 211, HEIGHT / 2 - 50);
			g.drawImage(spriteImageDead, dragon.x - 15, dragon.y - 75, null);

			// bottom layer
			g.drawImage(rockBorderImage1, scrollB1, 678, null);
			g.drawImage(rockBorderImage2, scrollB2, 678, null);
		}
	}
}
