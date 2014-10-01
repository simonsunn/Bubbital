import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import javax.swing.ImageIcon;

/** Contains all the information about turrets
 * @author AjwardSunYu
 */
/* The arrows show what the angle is depending on which way the turret is facing
 * <- 0 degrees
 * ^  90 degrees
 * -> 180 degrees
 * v  270- degrees
 */
public class BBTurret
{
	// The x coordinate to rotate the screen in order to draw a rotating turret
	private final int	CENTER_X	= BBMain.MAIN_WIDTH / 2;
	// The y coordinate to fire a bubble at
	private final int[]	Y_OFFSET	= { BBMain.MAIN_HEIGHT, -100 };

	// The index of the player for this turret
	private int			playerIndex;								// bottom index of 0
	// The current angle of this turret
	public double		angle;
	// Which direction this turret is turning. Positive is clockwise, negative is counter-clockwise
	private double		rotateDirection;
	// The images for the turret
	private Image		turretImage, bodyImage;
	// The XY coordinate to rotate this turret
	private Point		position;
	// Font to draw the player #
	private Font		playerFont;

	/** Constructor
	 * @param player The index of this player
	 */
	public BBTurret(int player)
	{
		playerIndex = player;
		angle = 90;
		rotateDirection = 1.5;

		if (BBMain.getSingleton().currentGameMode != BBMain.GAME_MODE_ONE_PLAYER)
			playerFont = new Font("Kristen ITC", Font.BOLD, 25);

		// We are using different images for different players to have it stand out
		turretImage = new ImageIcon("resources/turret" + player + ".png").getImage();
		bodyImage = new ImageIcon("resources/turret_body" + player + ".png").getImage();

		position = new Point(CENTER_X, BBMain.MAIN_HEIGHT);
	}

	/** Create a new bubble, set its properties (position and angle) and return it
	 * @return A new bubble with its properties set
	 */
	public BBBubble fire()
	{
		// USE ARRAY IF POSSIBLE HERE
		if (playerIndex == 0)
			return new BBBubble(new BBPoint(CENTER_X, Y_OFFSET[playerIndex]), angle, playerIndex);
		else
			return new BBBubble(new BBPoint(CENTER_X, Y_OFFSET[playerIndex]), angle + 180, playerIndex);
	}

	/** Helper method for AI (animates rotating the turret to the given angle)
	 */
	public void rotateTo(double a)
	{
		while (Math.abs(angle - a) > 2)
		{
			update();
			System.out.println("current angle " + angle);
			// Delay
			BBMain.getSingleton().repaintNow();
			BBMain.delay(BBMain.TIMER_DELAY);
		}
	}

	/** Rotate this turret
	 */
	public void update()
	{
		angle += rotateDirection;
		if (angle < 10 || angle > 170)
			rotateDirection *= -1;
	}

	/** Reverses the direction that this turret will rotate
	 */
	public void reverse()
	{
		rotateDirection *= -1;
	}

	/** Draws the image of this turret in the given Graphics context
	 */
	public void draw(Graphics2D g2D)
	{
		// Find the angle in radians and the x and y position of the centre of the object
		double angleInRadians = Math.toRadians(angle + 90);

		// Rotate the graphic context, draw the image and then rotate back
		g2D.rotate(angleInRadians, CENTER_X, position.y);
		g2D.drawImage(turretImage, CENTER_X - turretImage.getWidth(null) / 2,
				BBMain.MAIN_HEIGHT - turretImage.getHeight(null) / 2 + 40, null);
		g2D.rotate(-angleInRadians, CENTER_X, position.y);

		// Draw body
		g2D.drawImage(bodyImage, CENTER_X - bodyImage.getWidth(null) / 2,
				BBMain.MAIN_HEIGHT - bodyImage.getHeight(null) + 5, null);
		// Draw player text
		if (BBMain.getSingleton().currentGameMode != BBMain.GAME_MODE_ONE_PLAYER)
		{
			g2D.setColor(Color.black);
			g2D.setFont(playerFont);
			if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_AI_PLAYER && playerIndex == 1)
				g2D.drawString("AI", CENTER_X - 13, position.y - 5);
			else
				g2D.drawString("P" + (playerIndex + 1), CENTER_X - 13, position.y - 5);
		}
	}

	/** Getter method for the position of this turret (for AI)
	 */
	public BBPoint getBBPoint()
	{
		return new BBPoint(CENTER_X, Y_OFFSET[playerIndex]);
	}

	/** Getter method for the angle this turret is facing
	 */
	public double getCurrentAngle()
	{
		return angle;
	}

	/** Set this turret's angle (use in multiplayer to make both 1st and 2nd face the same angle after rotating screen
	 */
	public void setAngle(double a)
	{
		angle = a;
	}
}
