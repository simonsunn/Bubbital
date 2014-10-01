import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class BBMenuPanel implements ActionListener, KeyListener
{
	// DEFINE STATES
	// Used by the images array & menuState variables
	private final static int	TITLE						= 0;
	private final static int	INSTRUCTIONS				= 1;
	private final static int	HIGHSCORES					= 2;
	private final static int	CREDITS						= 3;
	private final static int	SELECT_GAME_MODE			= 4;
	private final static int	GAME_OVER					= 5;
	private final static int	GAME_OVER_FISH				= 6;
	private final static int	GAME_OVER_TOP_TEXT			= 7;
	private final static int	GAME_OVER_LEFT_TEXT			= 8;
	private int					menuState;
	private Image[]				images;

	// Used by the buttons array
	private final static int	NO_OF_BUTTONS				= 11;
	private final static int	MAIN_PLAY_GAME_BUTTON		= 0;
	private final static int	INSTRUCTIONS_BUTTON			= 1;
	private final static int	HIGHSCORES_BUTTON			= 2;
	private final static int	CREDITS_BUTTON				= 3;
	private final static int	BACK_BUTTON					= 4;
	private final static int	ONE_PLAYER_BUTTON			= 5;
	private final static int	TWO_PLAYER_BUTTON			= 6;
	private final static int	AI_PLAYER_BUTTON			= 7;
	private final static int	INSTRUCTIONS_PLAY_BUTTON	= 8;
	private final static int	GAME_OVER_MENU_BUTTON		= 9;
	private final static int	GAME_OVER_PLAY_AGAIN_BUTTON	= 10;

	// Highscore variables
	private final static String	HIGHSCORES_TITLE			= "Hiscores";
	private final static int	HIGHSCORE_DISPLAY_Y_OFFSET	= 120;
	private final static int	MAX_PLAYER_NAME_LENGTH		= 8;
	// Variables to store the player's highscore info
	private String[]			highScoreTextBubbleStrings;
	private String				playerName;
	private boolean				isEnteringPlayerName;
	private int					playerScore;
	// Variables to display the UI for entering names
	private final static int	CARET_DEFAULT_BLINK_DELAY	= 3;
	private boolean				isCaretShowing;
	private Timer				caretBlinkTimer;
	private int					caretBlinkCountDown;

	// Game over animation offset variables
	private int					gameOverFishOffset;
	private int					gameOverTopTextOffset;
	private int					gameOverLeftTextOffset;

	// Event handler instance variables
	private MouseHandler		mouseHandler;
	private MouseMotionHandler	motionHandler;
	private boolean				didAddMouseListeners;

	// Array of objects used to draw on screen
	private BBButton[]			buttons;
	private Font[]				fonts;
	private static Color[]		colours						= { Color.white,
															Color.red,
															Color.yellow,
															Color.green,
															Color.blue };

	/** Constructor
	 */
	public BBMenuPanel()
	{
		mouseHandler = new MouseHandler();
		motionHandler = new MouseMotionHandler();

		menuState = TITLE;
		images = new Image[10];
		images[TITLE] = new ImageIcon("resources/mainmenu.png").getImage();
		images[INSTRUCTIONS] = new ImageIcon("resources/instructionmenu.png").getImage();
		images[HIGHSCORES] = new ImageIcon("resources/background.png").getImage();
		images[CREDITS] = new ImageIcon("resources/creditsmenu.png").getImage();
		images[SELECT_GAME_MODE] = new ImageIcon("resources/gamemodes.png").getImage();
		images[GAME_OVER] = images[HIGHSCORES];
		images[GAME_OVER_FISH] = new ImageIcon("resources/fish.png").getImage();
		images[GAME_OVER_TOP_TEXT] = new ImageIcon("resources/textbubble-top.png").getImage();
		images[GAME_OVER_LEFT_TEXT] = new ImageIcon("resources/textbubble-left.png").getImage();

		fonts = new Font[3];
		fonts[0] = new Font("Kristen ITC", Font.BOLD, 50);
		fonts[1] = new Font("Kristen ITC", Font.BOLD, 42);
		fonts[2] = new Font("Kristen ITC", Font.BOLD, 37);

		buttons = new BBButton[NO_OF_BUTTONS];
		// Main menu buttons
		buttons[MAIN_PLAY_GAME_BUTTON] = new BBButton("Play Game", new Rectangle(75, 200, 300, 65), fonts[0]);
		buttons[MAIN_PLAY_GAME_BUTTON].normalColour = Color.green;
		buttons[INSTRUCTIONS_BUTTON] = new BBButton("Instructions", new Rectangle(75, 300, 400, 65), fonts[0]);
		buttons[INSTRUCTIONS_BUTTON].normalColour = Color.yellow;
		buttons[HIGHSCORES_BUTTON] = new BBButton(HIGHSCORES_TITLE, new Rectangle(75, 400, 250, 65), fonts[0]);
		buttons[HIGHSCORES_BUTTON].normalColour = Color.red;
		buttons[CREDITS_BUTTON] = new BBButton("Credits", new Rectangle(75, 500, 250, 65), fonts[0]);
		buttons[CREDITS_BUTTON].normalColour = Color.blue;

		// Selecte game mode buttons
		buttons[ONE_PLAYER_BUTTON] = new BBButton("1P", new Rectangle(73, 355, 65, 65), fonts[0]);
		buttons[ONE_PLAYER_BUTTON].normalColour = Color.black;
		buttons[ONE_PLAYER_BUTTON].xOffset = 0;
		buttons[TWO_PLAYER_BUTTON] = new BBButton("2P", new Rectangle(275, 355, 65, 65), fonts[0]);
		buttons[TWO_PLAYER_BUTTON].normalColour = Color.black;
		buttons[TWO_PLAYER_BUTTON].xOffset = 0;
		buttons[AI_PLAYER_BUTTON] = new BBButton("AI", new Rectangle(470, 355, 65, 65), fonts[0]);
		buttons[AI_PLAYER_BUTTON].normalColour = Color.black;
		buttons[AI_PLAYER_BUTTON].xOffset = 0;

		// Re-use same back button
		buttons[BACK_BUTTON] = new BBButton("<- Back", new Rectangle(15, 590, 200, 65), fonts[0]);
		buttons[BACK_BUTTON].normalColour = Color.yellow;
		buttons[BACK_BUTTON].xOffset *= -1;

		// Instruction screen buttons
		buttons[INSTRUCTIONS_PLAY_BUTTON] = new BBButton("Play ->", new Rectangle(405, 590, 200, 65), fonts[0]);
		buttons[INSTRUCTIONS_PLAY_BUTTON].normalColour = Color.green;

		// Game over screen buttons
		buttons[GAME_OVER_MENU_BUTTON] = new BBButton("<- Menu", new Rectangle(20, 590, 190, 65), fonts[1]);
		buttons[GAME_OVER_MENU_BUTTON].normalColour = Color.yellow;
		buttons[GAME_OVER_MENU_BUTTON].xOffset *= -1;
		buttons[GAME_OVER_PLAY_AGAIN_BUTTON] = new BBButton("Play Again ->", new Rectangle(270, 590, 310, 65), fonts[1]);
		buttons[GAME_OVER_PLAY_AGAIN_BUTTON].normalColour = Color.green;
	}

	/** Show the main menu
	 */
	public void showMainMenu()
	{
		// Add mouse listeners to the drawing panel
		changeMouseListenerStatus(true);

		menuState = TITLE;
		// Show the menu buttons
		for (int i = 0; i < buttons.length; i++)
			buttons[i].isHidden = !(i >= MAIN_PLAY_GAME_BUTTON && i <= CREDITS_BUTTON);
	}

	/** Show the game modes  
	 */
	public void showGameModes()
	{
		menuState = SELECT_GAME_MODE;
		// Show the menu buttons
		for (int i = 0; i < buttons.length; i++)
			buttons[i].isHidden = !((i >= ONE_PLAYER_BUTTON && i <= AI_PLAYER_BUTTON) || i == BACK_BUTTON);
	}

	/** Show the instructions menu
	 */
	public void showInstructions()
	{
		menuState = INSTRUCTIONS;
		// Show the back and play buttons
		for (int i = 0; i < buttons.length; i++)
			buttons[i].isHidden = !(i == INSTRUCTIONS_PLAY_BUTTON || i == BACK_BUTTON);
	}

	/** Show the highscores menu
	 */
	public void showHighscores()
	{
		menuState = HIGHSCORES;
		// Show the back and play buttons
		for (int i = 0; i < buttons.length; i++)
			buttons[i].isHidden = !(i == BACK_BUTTON);
	}

	/** Show the credits menu
	 */
	public void showCredits()
	{
		menuState = CREDITS;
		// Show the menu buttons
		for (int i = 0; i < buttons.length; i++)
			buttons[i].isHidden = !(i == BACK_BUTTON);
	}

	/** Show the game over buttons if needed (hides other buttons too)
	 * @param showOrNot Shows only the bottom buttons (menu and play again) if true, hides everything otherwise
	 */
	public void showGameOverButtons(boolean showOrNot)
	{
		// Show game over buttons
		for (int i = 0; i < buttons.length; i++)
		{
			buttons[i].isHidden = !(showOrNot && (i == GAME_OVER_MENU_BUTTON || i == GAME_OVER_PLAY_AGAIN_BUTTON));
		}
	}

	/** Show the game over screen with animations
	 *  @param scoreOrWinner The players score (if one player mode), or which player won
	 *  1 is player one won (or computer lost), 2 is player 2 won
	 *  It can determine which game mode by asking the BBMain class
	 */
	public void showGameOver(int scoreOrWinner)
	{
		// Add mouse listeners to the drawing panel
		changeMouseListenerStatus(true);

		menuState = GAME_OVER;
		highScoreTextBubbleStrings = new String[5];

		if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_ONE_PLAYER)
		{
			// Check if the score qualifies as a highscore
			playerScore = scoreOrWinner;
			highScoreTextBubbleStrings[0] = "You scored: " + playerScore + " point";
			if (playerScore > 1)
				highScoreTextBubbleStrings[0] += "s";
			else if (playerScore == 0)
				highScoreTextBubbleStrings[0] = "Better luck next time!";

			int rankOrPointsNeeded = BBHighScoreManager.getSingleton().rankOrPointsToQualify(playerScore);
			if (rankOrPointsNeeded > 0 && playerScore > 0)
			{
				BBMain.getSingleton().getDrawingPanel().addKeyListener(this);
				highScoreTextBubbleStrings[1] = "You are #" + rankOrPointsNeeded + "!";
				if (rankOrPointsNeeded == 1)
					highScoreTextBubbleStrings[2] = "Congratulations!";
				else
					highScoreTextBubbleStrings[2] = "The " + HIGHSCORES_TITLE + " is: " + BBHighScoreManager.getSingleton().getHighScore();
				highScoreTextBubbleStrings[3] = "Enter name:";
				highScoreTextBubbleStrings[4] = "";
				isEnteringPlayerName = true;
				playerName = "";
				caretBlinkCountDown = CARET_DEFAULT_BLINK_DELAY;
				caretBlinkTimer = new Timer(150, this);
				caretBlinkTimer.start();
			}
			else
			{
				highScoreTextBubbleStrings[1] = "You need " + (Math.abs(rankOrPointsNeeded) + 2) + " more point";
				if (Math.abs(rankOrPointsNeeded) > 1)
					highScoreTextBubbleStrings[1] += "s";
				if (BBHighScoreManager.getSingleton().getHighScore() == 0)
					highScoreTextBubbleStrings[2] = "to qualify for highscores";
				else
					highScoreTextBubbleStrings[2] = "The highscore is: " + BBHighScoreManager.getSingleton().getHighScore();
				// Center text
				highScoreTextBubbleStrings[3] = "       Play";
				highScoreTextBubbleStrings[4] = "     Again?";
			}
		}
		else if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_TWO_PLAYER && !BBMain.getSingleton().isAI)
		{
			// 2 player mode game over screen
			highScoreTextBubbleStrings[0] = "";
			highScoreTextBubbleStrings[1] = "Congratulations Player " + scoreOrWinner + "!";
			highScoreTextBubbleStrings[2] = "";

			// Center text
			highScoreTextBubbleStrings[3] = "       Play";
			highScoreTextBubbleStrings[4] = "     Again?";
		}
		else
		{
			// AI mode game over screen
			if (scoreOrWinner == 1)
			{
				highScoreTextBubbleStrings[0] = "Congratulations, you beat";
				highScoreTextBubbleStrings[2] = "          AL the ai!";
			}
			else
			{
				highScoreTextBubbleStrings[0] = "Al the ai beat you!";
				highScoreTextBubbleStrings[2] = "Better luck next time!";
			}

			highScoreTextBubbleStrings[1] = "";

			// Center text
			highScoreTextBubbleStrings[3] = "       Play";
			highScoreTextBubbleStrings[4] = "     Again?";
		}

		// Decide whether or not to show menus: show if not entering name, dont show otherwise
		showGameOverButtons(!isEnteringPlayerName);

		// Show game over animation
		gameOverFishOffset = 580;
		gameOverTopTextOffset = -100;
		gameOverLeftTextOffset = -135;

		// Loop through 50 times
		// 50 * 20 ms delay = 1sec in total
		for (int i = 0; i < 50; i++)
		{
			gameOverFishOffset -= 5;
			gameOverTopTextOffset += 3;
			gameOverLeftTextOffset += 3;
			BBMain.getSingleton().repaintNow();
			BBMain.delay(20);
		}
	}

	/** Player clicked a play button, show the game
	 */
	public void play(int gameMode)
	{
		// Remove mouse listeners since not needed when playing
		changeMouseListenerStatus(false);
		BBMain.getSingleton().showGame(gameMode);
	}

	/** Draw buttons, texts, and images for the menu
	 */
	public void paint(Graphics2D g)
	{
		// Draw the background image using the menuState as the index of the image in the array
		g.drawImage(images[menuState], 0, 0, null);

		// Display all the buttons (they will draw only if are not hidden)
		for (int i = 0; i < buttons.length; i++)
			if (buttons[i] != null)
				buttons[i].draw(g);

		// Select game mode
		if (menuState == SELECT_GAME_MODE)
		{
			// Draw 3 circles
			for (int i = 0; i < 3; i++)
			{
				g.setColor(colours[3 - i]);
				g.fillOval(i * 200, 300, 200, 200);
				// Redraw the buttons (because they are covered but the circles)
				buttons[ONE_PLAYER_BUTTON + i].draw(g);
			}
		}
		// Draw the highscores
		else if (menuState == HIGHSCORES)
		{
			// Heading
			g.setColor(Color.white);
			g.setFont(fonts[0]);
			g.drawString(HIGHSCORES_TITLE, 30, 76);

			// Draw names and scores
			String[] names = BBHighScoreManager.getSingleton().getHighScoreNames();
			int[] scores = BBHighScoreManager.getSingleton().getHighScores();

			g.setFont(fonts[1]);

			for (int i = 0; i < names.length && names[i] != null && names[i].length() > 0; i++)
			{
				// Set colour depending on the position in the highscore
				if (i < colours.length)
					g.setColor(colours[i]);

				// Names on the left side
				g.drawString(names[i], 60, 75 * (1 + i) + HIGHSCORE_DISPLAY_Y_OFFSET);

				// Scores on the right side
				g.drawString(Integer.toString(scores[i]), 450, 75 * (1 + i) + HIGHSCORE_DISPLAY_Y_OFFSET);
			}
		}
		// Game over animations
		else if (menuState == GAME_OVER)
		{
			// Draw animating images
			g.drawImage(images[GAME_OVER_FISH], gameOverFishOffset, 280, null);
			g.drawImage(images[GAME_OVER_TOP_TEXT], 15, gameOverTopTextOffset, null);
			g.drawImage(images[GAME_OVER_LEFT_TEXT], gameOverLeftTextOffset, 365, null);

			// Top text bubble
			g.setFont(fonts[2]);
			g.setColor(Color.black);
			for (int i = 0; i < 3; i++)
				g.drawString(highScoreTextBubbleStrings[i], 40, 53 * i + 60 + gameOverTopTextOffset);

			// Left text bubble
			g.drawString(highScoreTextBubbleStrings[3], 23 + gameOverLeftTextOffset, 435);

			// Set the colour to red if the player's name will exceed the max length
			if (isEnteringPlayerName && playerName.length() >= MAX_PLAYER_NAME_LENGTH)
				g.setColor(Color.red);

			g.drawString(highScoreTextBubbleStrings[4], 23 + gameOverLeftTextOffset, 495);

			// Draw the whole string plus '|' character as a caret
			//(to get the correct position since characters have different lengths)
			if (isEnteringPlayerName && isCaretShowing && playerName.length() < MAX_PLAYER_NAME_LENGTH)
				g.drawString(highScoreTextBubbleStrings[4] + "|", 23 + gameOverLeftTextOffset, 495);
		}
	}

	/** Gets the player's name if needed
	 */
	public void keyPressed(KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE)
		{
			if (playerName.length() >= 1)
			{
				playerName = playerName.substring(0, playerName.length() - 1);
				highScoreTextBubbleStrings[4] = playerName;
			}
		}
		else if (event.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (playerName.length() < 1)
				return;

			// Got player's name, remove key listener
			BBMain.getSingleton().getDrawingPanel().removeKeyListener(this);
			isEnteringPlayerName = false;

			// Stop timer and set to null
			caretBlinkTimer.stop();
			caretBlinkTimer = null;

			// Update the highscore with the player's name
			int rank = BBHighScoreManager.getSingleton().updateHighScores(playerScore, playerName);
			System.out.println(highScoreTextBubbleStrings[2]);

			highScoreTextBubbleStrings[1] = playerName + ", you are #" + rank + "!";
			// Center text
			highScoreTextBubbleStrings[3] = "       Play";
			highScoreTextBubbleStrings[4] = "     Again?";

			// Show bottom buttons
			showGameOverButtons(true);
		}
		else if (Character.isLetter(event.getKeyChar()) && playerName.length() < MAX_PLAYER_NAME_LENGTH)
		{
			playerName += event.getKeyChar();
			highScoreTextBubbleStrings[4] = playerName;
		}

		// Restart caret delay countdown
		isCaretShowing = false;
		caretBlinkCountDown = CARET_DEFAULT_BLINK_DELAY;
		// Repaint now
		BBMain.getSingleton().repaintNow();
	}


	/** Inner class that handles all the mouse clicked events
	 */
	private class MouseHandler extends MouseAdapter
	{
		/** Changes the menu state depending on the button that was clicked
		*@param event The event created by the mouse click
		*/
		public void mousePressed(MouseEvent event)
		{
			// Get the location of the click
			Point p = event.getPoint();

			// Go through all buttons and since there are no overlapping buttons, 'stop' when found
			int i;
			for (i = 0; i < buttons.length && !buttons[i].recheckMouseOver(p); i++)
			{
			}

			// Set the mouseOver variable to false for that button because that button will be hidden from view
			// (we are switching the state of the menu)
			// Also checks to make sure that the index 'i' is within bounds
			if (i < NO_OF_BUTTONS - 1)
				buttons[i].isMouseOver = false;

			// Found the button, now execute the correct action
			if (i == MAIN_PLAY_GAME_BUTTON || i == INSTRUCTIONS_PLAY_BUTTON)
				showGameModes();
			else if (i >= ONE_PLAYER_BUTTON && i <= AI_PLAYER_BUTTON)
				play(i - ONE_PLAYER_BUTTON + 1);
			if (i == GAME_OVER_PLAY_AGAIN_BUTTON)
				BBMain.getSingleton().playAgain();
			else if (i == INSTRUCTIONS_BUTTON)
				showInstructions();
			else if (i == HIGHSCORES_BUTTON)
				showHighscores();
			else if (i == CREDITS_BUTTON)
				showCredits();
			else if (i == BACK_BUTTON || i == GAME_OVER_MENU_BUTTON)
				showMainMenu();
			else if (i == INSTRUCTIONS_PLAY_BUTTON || i == MAIN_PLAY_GAME_BUTTON)
				showGameModes();

			BBMain.getSingleton().repaintNow();
		}
	}


	/** Inner class that handles all the mouse movement events
	 */
	private class MouseMotionHandler extends MouseMotionAdapter
	{
		/** Gives feedback whenever the player mouses-over a button
		*@paramevent The event created by the mouse movement
		*/
		public void mouseMoved(MouseEvent event)
		{
			Point p = event.getPoint();

			for (int i = 0; i < buttons.length; i++)
			{
				if (buttons[i] != null)
					buttons[i].recheckMouseOver(p);
			}

			BBMain.getSingleton().repaintNow();
		} // Mouse moved method
	} // Mouse Motion Handler


	/** Add or remove the mouse (and motion) handlers if needed
	 */
	public void changeMouseListenerStatus(boolean on)
	{
		// Check if the mouse listener state is what is asked
		if (on == didAddMouseListeners)
			return; // Since the the requested mouse listener status is correct

		// Otherwise change it to the requested state
		if (on)
		{
			BBMain.getSingleton().getDrawingPanel().addMouseListener(mouseHandler);
			BBMain.getSingleton().getDrawingPanel().addMouseMotionListener(motionHandler);
		}
		else
		{
			BBMain.getSingleton().getDrawingPanel().removeMouseListener(mouseHandler);
			BBMain.getSingleton().getDrawingPanel().removeMouseMotionListener(motionHandler);
		}

		didAddMouseListeners = on;
	}

	/** Responds to timer events to animate the blinking carat while typing
	 */
	public void actionPerformed(ActionEvent event)
	{
		caretBlinkCountDown--;

		if (caretBlinkCountDown <= 0)
		{
			isCaretShowing = !isCaretShowing;
			caretBlinkCountDown = CARET_DEFAULT_BLINK_DELAY;
			BBMain.getSingleton().repaintNow();
		}
	}


	/** Extra methods related to KeyListeners that are not used
	 */
	public void keyReleased(KeyEvent event)
	{
	}

	public void keyTyped(KeyEvent event)
	{
	}
} // OBMenuPanel
