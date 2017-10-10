package cryptoDragon1;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameManager extends JApplet implements Runnable, MouseListener, KeyListener {

	private static final long serialVersionUID = 1L;
	final int WIDTH = 1200;
	final int HEIGHT = 800;
	private long desiredFPS = 50;
	private long desiredDeltaLoop = (1000 * 1000 * 1000) / desiredFPS;
	private boolean running = true;

	JFrame frame;
	Canvas canvas;
	BufferStrategy bufferStrategy;
	GameLogic logic;
	Thread gameloopThread;
		
	@Override
	public void init() {
		frame = new JFrame("Crypto-Dragon Game");
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.setLayout(null);

		canvas = new Canvas();
		canvas.setBounds(0, 0, WIDTH, HEIGHT);
		add(canvas);
		canvas.setIgnoreRepaint(true);

		panel.add(canvas);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();

		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		canvas.requestFocus();
		
		try {
			logic = new GameLogic();
		} 
		catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	@Override
	public void start() {
		gameloopThread = new Thread(this);
		gameloopThread.start();
	}

	@Override
	public void stop() {
		setRunning(false);
		try {
			gameloopThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private synchronized void setRunning(boolean running) {
		this.running = running;
	}

	private synchronized boolean isRunning() {
		return running;
	}

	public void run() {

		setRunning(true);

		long beginLoopTime;
		long endLoopTime;
		long currentUpdateTime = System.nanoTime();
		long lastUpdateTime;
		long deltaLoop;

		while (!isActive()) {
			Thread.yield();
		}
		while (isRunning()) {
			beginLoopTime = System.nanoTime();

			render();

			lastUpdateTime = currentUpdateTime;
			currentUpdateTime = System.nanoTime();
			logic.update((int) ((currentUpdateTime - lastUpdateTime) / (1000 * 1000)));

			endLoopTime = System.nanoTime();
			deltaLoop = endLoopTime - beginLoopTime;

			if (deltaLoop > desiredDeltaLoop) {
				// Do nothing. We are already late.
			} 
			else {
				try {
					Thread.sleep((desiredDeltaLoop - deltaLoop) / (1000 * 1000));
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void render() {
		try {
			Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
			g.clearRect(0, 0, WIDTH, HEIGHT);
			render(g);
			bufferStrategy.show();
			g.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void render(Graphics2D g) {
		logic.graphicsLogic(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		logic.mouseLogic();	
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		logic.keyLogic(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
