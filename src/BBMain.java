import java.awt.*;
import java.awt.event.KeyAdapter;

import javax.swing.*;
import java.io.*;

/**      RashadSimonJerry
 * @author AjwardSunYu
 */
public class BBMain extends JFrame
{
	// Constants about the UI
	public final static int		MAIN_WIDTH				= 600;
	public final static int		MAIN_HEIGHT				= 720;
	// The height of of frame - bottom 'game over' border
	public final static int		GAME_HEIGHT				= MAIN_HEIGHT - 100;
	public final static int		BORDER_SIZE				= 5;
	public final static int		JAVA_FRAME_OFFSET		= 10;
	public final Dimension		PREFFERED_SIZE			= new Dimension(MAIN_WIDTH - JAVA_FRAME_OFFSET, MAIN_HEIGHT - JAVA_FRAME_OFFSET);

	// Constants about the 3 game modes
	public final static int		GAME_MODE_ONE_PLAYER	= 1;
	public final static int		GAME_MODE_TWO_PLAYER	= 2;
	public final static int		GAME_MODE_AI_PLAYER		= 3;
	public int					currentGameMode;
	public boolean				isAI;

	// Constant about the refresh rate or delays (in milliseconds)
	public final static int		TIMER_DELAY				= 15;																				// 15 ms delay
	public final static int		SHOW_GAME_OVER_DELAY	= 50;																				// 100 * 15ms = 1.5 s delay

	// The single instance of BBMain
	private static BBMain		singleton;

	// Variables related to the instances of UI components
	private boolean				isShowingMenu;
	private BBPanel				drawingPanel;
	private BBGamePanel			gamePanel;
	private BBMenuPanel			menuPanel;

	public final static boolean	IS_DEBUGGING			= true;

	/** Keeps track of one instance of this object (BBMain)
	 * @return The current instance
	 */
	public static BBMain getSingleton()
	{
		if (singleton == null)
			singleton = new BBMain();

		return singleton;
	}

	/** Constructor
	 * 
	 */
	public BBMain()
	{
		// Sets up the frame for the game
		super("Bubbital");

		drawingPanel = new BBPanel();
		getContentPane().add(drawingPanel);

		isShowingMenu = true;
		menuPanel = new BBMenuPanel();

		pack();
	}

	/** Ask the JPanel to repaint immediately 
	 */
	public void repaintNow()
	{
		// Add smart repainting
		drawingPanel.paintImmediately(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
	}
	
	public void playAgain()
	{
		isShowingMenu = false;
		drawingPanel.requestFocusInWindow();
		gamePanel.newGame(currentGameMode);
	}

	/** Show and start a new game
	 */
	public void showGame(int gameMode)
	{
		currentGameMode = Math.min(gameMode, 2);
		isAI = (gameMode == GAME_MODE_AI_PLAYER);

		// Create game panel lazily (create it when needed to improve loading times)
		if (gamePanel == null)
			gamePanel = new BBGamePanel(drawingPanel);

		isShowingMenu = false;
		drawingPanel.requestFocusInWindow();
		gamePanel.newGame(currentGameMode);
	}

	/** Show the menu
	 */
	public void showMenu()
	{
		isShowingMenu = true;
		menuPanel.showMainMenu();
		repaintNow();
	}

	/** Show the game over screen
	 * @param score The score that the player got
	 */
	public void showGameOver(int score)
	{
		isShowingMenu = true;
		drawingPanel.requestFocusInWindow();
		menuPanel.showGameOver(score);
	}

	/** Gets the JPanel for drawing
	 * @return the current JPanel for drawing
	 */
	public BBPanel getDrawingPanel()
	{
		return drawingPanel;
	}

	/** A subclass of a JPanel to do the drawing for this application
	* @author AjwardSunYu
	*/
	public class BBPanel extends JPanel
	{
		/** Constructor
		 */
		public BBPanel()
		{
			setPreferredSize(PREFFERED_SIZE);
			setBackground(new Color(128, 255, 255));
			setBackground(new Color(39, 172, 252));
			setBackground(new Color(255, 255, 128));

			// Add keyboard listener
			setFocusable(true);
			requestFocusInWindow();
		}

		// Calls either the menu or game object to do the required drawing
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Graphics2D g2D = (Graphics2D) g;
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (isShowingMenu)
				menuPanel.paint(g2D);
			else
				gamePanel.paint(g2D);
		}
	}

	// Main method
	public static void main(String[] args)
	{
		// Starts up the OBMain frame
		singleton = new BBMain();

		singleton.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		singleton.setLocation(500, 0);
		singleton.setVisible(true);
		singleton.setResizable(false);
		singleton.setIconImage(Toolkit.getDefaultToolkit().getImage("resources/icon.png"));

		singleton.showMenu();
	}

	/** 
	 * @param milliSec Number of milliseconds to delay the program for
	 */
	public static void delay(int milliSec)
	{
		try
		{
			Thread.sleep(milliSec);
		} catch (InterruptedException e)
		{
		}
	}
} // BBMain
