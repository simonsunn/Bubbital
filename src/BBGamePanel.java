import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

public class BBGamePanel
{
	// The object that keeps track of all the game objects (Turrets & Bubbles)
	private BBGameModal	gameModal;

	// Font is created once and reused to improve speed
	private Font		scoreFont;

	/** Constructor
	 */
	public BBGamePanel(BBMain.BBPanel panel)
	{
		// Create game modal
		gameModal = new BBGameModal();

		scoreFont = new Font("Kristen ITC", Font.BOLD, 22);
	}

	/** Tells the game modal to start a new game (if possible)
	 */
	public void newGame(int gameMode)
	{
		gameModal.newGame(gameMode);
	}

	/** Repaint the board's drawing panel
	 * @param g The Graphics2D context
	 */
	public void paint(Graphics2D g)
	{
		// background image
		g.setColor(new Color(39, 172, 252));
		g.fillRect(0, 0, BBMain.MAIN_WIDTH, BBMain.MAIN_HEIGHT);

		// Top and bottom borders
		g.fillRect(BBMain.BORDER_SIZE, 0, BBMain.MAIN_WIDTH - 2 * BBMain.BORDER_SIZE, BBMain.BORDER_SIZE);

		g.setColor(new Color(2, 108, 172));
		// left border
		g.fillRect(0, 0, BBMain.BORDER_SIZE, BBMain.MAIN_HEIGHT);
		// right border
		g.fillRect(BBMain.MAIN_WIDTH - 5, 0, BBMain.BORDER_SIZE, BBMain.MAIN_HEIGHT);
		// Game over border (bottom)

		g.fillRect(BBMain.BORDER_SIZE, 0, BBMain.MAIN_WIDTH - 2 * BBMain.BORDER_SIZE, BBMain.BORDER_SIZE);
		g.setColor(new Color(128, 255, 255));
		g.fillRect(BBMain.BORDER_SIZE, BBMain.MAIN_HEIGHT - 100, BBMain.MAIN_WIDTH - 2 * BBMain.BORDER_SIZE, BBMain.BORDER_SIZE);
		g.setColor(Color.red);
		// Always draw at bottom because we rotate the entire screen
		if (gameModal.shouldBottomBorderBeRed() || gameModal.shouldTopBorderBeRed())
			g.fillRect(BBMain.BORDER_SIZE, BBMain.MAIN_HEIGHT - 100, BBMain.MAIN_WIDTH - 2 * BBMain.BORDER_SIZE, BBMain.BORDER_SIZE);
			
		// Draw sandbar
		g.setColor(new Color(255, 255, 128));
		g.fillRect(0, BBMain.MAIN_HEIGHT - 30, BBMain.MAIN_WIDTH, 30);

		// Rotate the screen (for 2 player mode)
		double angleInRadians = Math.toRadians(gameModal.getRotateAngle());
		g.rotate(angleInRadians, 300, 310);

		//Draw bubbles
		for (int i = 0; i < gameModal.getBubbles().size(); i++)
			gameModal.getBubbles().get(i).draw(g);

		// Rotate back before drawing other UI
		g.rotate(-angleInRadians, 300, 310);

		//  Draw turret(s)
		BBTurret[] turrets = gameModal.getTurrets();
		for (int i = 0; i < turrets.length; i++)
			if (turrets[i] != null && i == gameModal.getCurrentPlayer())
				turrets[i].draw(g);

		// Draw (Q)uit text at the bottom "status bar"
		g.setColor(new Color(2, 108, 172));
		g.setFont(scoreFont);
		g.drawString("(q)uit", BBMain.MAIN_WIDTH - 80, BBMain.MAIN_HEIGHT - 7);

		if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_ONE_PLAYER)
		{
			// Draw score
			int combo = gameModal.getCurrentCombo();
			if (combo <= 0)
				g.drawString("" + gameModal.getCurrentScore(), 5, BBMain.MAIN_HEIGHT - 7);
			else if (combo == 1)
				g.drawString("" + gameModal.getCurrentScore() + " + " + combo, 5, BBMain.MAIN_HEIGHT - 7);
			else
				g.drawString("" + gameModal.getCurrentScore() + " + " + combo + " combo!", 5, BBMain.MAIN_HEIGHT - 7);
		}
	}
} // BBGamePanel
