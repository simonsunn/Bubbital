import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/** Contains all the information for the bubbles
 * @author AjwardSunYu
 */
public class BBBubble implements ActionListener
{
	// The current state of this bubble (launching/expanding/expanded)
	public final static int			BUBBLE_INITIALIZING			= 0;
	public final static int			BUBBLE_LAUNCHING			= 1;
	public final static int			BUBBLE_EXPANDING			= 2;
	public final static int			BUBBLE_EXPANDED				= 3;
	public final static int			BUBBLE_EXPLODING			= 4;
	private int						currentState;

	// How many hits (bounces) this bubble needs before popping
	public final static int			BUBBLE_HP_RED				= 1;
	public final static int			BUBBLE_HP_YELLOW			= 2;
	public final static int			BUBBLE_HP_GREEN				= 3;
	private int						currentHP;

	// Some constants
	private final static int		INITIAL_RADIUS				= 7;
	private final static int		CENTRE_BUBBLE_RADIUS		= 10;
	private int						currentRadius;

	// Velocity constants (and variables)
	private final static int		INITIAL_VELOCITY			= 17;
	private final static BBPoint	NORMAL_VELOCITY_REDUCTION	= new BBPoint(0.985, 0.985);
	private final static BBPoint	BOUNCE_VELOCITY_REDUCTION	= new BBPoint(0.975, 0.975);
	private final static BBPoint	NOOB_VELOCITY_REDUCTION		= new BBPoint(1, 1);
	private BBPoint					currentVelocity;

	// Other variables related to this bubble
	private BBPoint					currentPosition;
	private double					distanceToCollision;
	private int						currentPlayer;												// bottom index of 0
	private BBCollision				collisionData;
	private BBBubble				lastBouncedBubble;
	private float					currentAlpha;
	private int						expandedRadius;

	// Used to animate this bubble
	private Timer					timer;
	// Used to get calculated point of intersection and expanded radius
	public BBGameModal				gameModal;

	/** Constructor
	 * @param initialPosition The initial centre XY coordinates of the bubble
	 * @param angle The angle that the turret was facing
	 */
	public BBBubble()
	{
		currentPosition = new BBPoint(BBMain.MAIN_WIDTH / 2, (BBMain.GAME_HEIGHT) / 2);
		currentVelocity = new BBPoint();
		currentRadius = CENTRE_BUBBLE_RADIUS;
		currentState = BUBBLE_EXPANDED;
		currentHP = BUBBLE_HP_GREEN;
	}//*/

	/** Constructor
	 * @param initialPosition The initial centre XY coordinates of the bubble
	 * @param angle The angle that the turret was facing when it was fired
	 */
	public BBBubble(BBPoint initialPosition, double angle, int cPlayer)
	{
		// Try to avoid special cases:
		if (angle == 90 || angle == 270)
			angle += 0.5;

		timer = new Timer(BBMain.TIMER_DELAY, this);
		if (BBMain.IS_DEBUGGING)
			System.out.println("new bubble at angle:             " + angle);
		currentPlayer = cPlayer;
		currentRadius = INITIAL_RADIUS;
		currentState = BUBBLE_INITIALIZING;
		currentHP = BUBBLE_HP_GREEN;
		currentPosition = initialPosition;
		expandedRadius = -1;
		distanceToCollision = -1;
		// Calculate the vectors using trigonometry (cos for x-velocity, sin for y-velocity)
		currentVelocity = new BBPoint(-INITIAL_VELOCITY * Math.cos(Math.toRadians(angle)),
				-INITIAL_VELOCITY * Math.sin(Math.toRadians(angle)));
	}

	/** Get the collision data (if) any and starts animating this bubble
	 */
	public void launch()
	{
		getCollisionData();
		currentState = BUBBLE_LAUNCHING;
		timer.start();
	}

	/** Called when another bubble bounces with this bubble
	 *  This bubble will animate and be removed from the bubbles array if it is popped 
	 */
	public void hit()
	{
		currentHP--;
		gameModal.hitBubble();

		System.out.println("hit");
		if (currentHP <= 0)
		{
			currentState = BUBBLE_EXPLODING;
			if (timer == null)
				timer = new Timer(BBMain.TIMER_DELAY, this);
			timer.start();
		}
	}

	/** Draw this bubble (sometimes with transparencies)
	 * @param g The Graphics2D context
	 */
	public void draw(Graphics2D g)
	{
		if (currentState == BUBBLE_LAUNCHING)
		{
			g.setColor(new Color(128, 255, 255));
			g.fillOval((int) currentPosition.x - currentRadius, (int) currentPosition.y - currentRadius, currentRadius * 2, currentRadius * 2);
		}
		else if (currentState == BUBBLE_EXPLODING)
		{
			g.setPaint(Color.red);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f - currentAlpha * 0.02f));

			// Paint translucent circle
			g.fillOval((int) currentPosition.x - currentRadius, (int) currentPosition.y - currentRadius, currentRadius * 2, currentRadius * 2);
			// Reset colour

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		}
		else
		{
			g.setColor(Color.white);
			g.fillOval((int) currentPosition.x - currentRadius, (int) currentPosition.y - currentRadius, currentRadius * 2, currentRadius * 2);

			// Fill the insides with a colour depending on the HP
			if (currentHP == BBBubble.BUBBLE_HP_RED)
				g.setColor(Color.red);
			else if (currentHP == BBBubble.BUBBLE_HP_YELLOW)
				g.setColor(Color.yellow);
			else if (currentHP == BBBubble.BUBBLE_HP_GREEN)
				g.setColor(Color.green);

			g.fillOval(((int) currentPosition.x - currentRadius) + 3, ((int) currentPosition.y - currentRadius) + 3, (currentRadius * 2) - 6, (currentRadius * 2) - 6);
		}
	}

	/** Gets the data from modal and calculates distance to collision (if will collide)
	 */
	public void getCollisionData()
	{
		collisionData = gameModal.checkCollisionFromPositionWithVelocity(getPosition(), new BBPoint(currentPosition, currentVelocity));
		if (collisionData != null)
		{
			distanceToCollision = BBGameModal.distanceBetweenPoints(currentPosition, collisionData.point);
			// Correct the collision point due to radius
		}
	}

	/** Helper method to check if collision has happened or not (calls its self if a collision happened
	 *  to see if it has collided again before the next frame has happened
	 */
	private void checkBubbleCollision()
	{
		// Check for collision with circle
		if (collisionData != null && !collisionData.wasHit && collisionData.bubble != lastBouncedBubble)
		{
			if (BBGameModal.distanceBetweenPoints(currentPosition, collisionData.point) <= currentRadius)
			{
				if (BBMain.IS_DEBUGGING)
					System.out.println("collided:     position: " + currentPosition.toString() + "collided at: " + collisionData.point.toString());

				lastBouncedBubble = collisionData.bubble;
				currentPosition = collisionData.point;
				double velocity = Math.sqrt(currentVelocity.x * currentVelocity.x + currentVelocity.y * currentVelocity.y);

				currentVelocity = new BBPoint(-velocity * Math.cos(Math.toRadians(collisionData.angle)),
						-velocity * Math.sin(Math.toRadians(collisionData.angle)));

				collisionData.bubble.hit();
				collisionData.wasHit = true;

				getCollisionData();
				checkBubbleCollision();
			}
		} // Collision check
	}

	/** Update the position and velocity of this bubble, as well as check for bouncing, collision, and game over
	 */
	public void update()
	{
		if (currentState == BUBBLE_LAUNCHING)
		{
			// Update the position depending on velocity
			currentPosition.addVelocity(currentVelocity);

			// Check for game over
			if ((currentPlayer == 0 && currentVelocity.y > 0 && currentPosition.y + currentVelocity.y >= BBMain.MAIN_HEIGHT - 100)
					|| (currentPlayer == 1 && currentVelocity.y < 0 && currentPosition.y + currentVelocity.y <= 0))
			{
				stopTimer();
				gameModal.gameOver();
				return;
			}

			// Bounce on walls
			boolean didBounce = false;

			// Left wall
			if (currentPosition.x - currentRadius <= BBMain.BORDER_SIZE)
			{
				currentVelocity.x = Math.abs(currentVelocity.x);
				didBounce = true;
			}
			// Right Wall
			else if (currentPosition.x + currentRadius / 2 >= BBMain.MAIN_WIDTH - 5 - BBMain.BORDER_SIZE)
			{
				currentVelocity.x = Math.abs(currentVelocity.x) * -1;
				didBounce = true;
			}
			// Top border
			if (currentPlayer == 0 && currentPosition.y - currentRadius <= BBMain.BORDER_SIZE)
			{
				// Player one horizontal wall bounce
				currentVelocity.y = Math.abs(currentVelocity.y);
				didBounce = true;
			}
			if (currentPlayer == 1 && currentPosition.y - currentRadius >= BBMain.MAIN_HEIGHT - 100 - BBMain.BORDER_SIZE * 4)
			{
				// Player two horizontal wall bounce
				currentVelocity.y = Math.abs(currentVelocity.y) * -1;
				didBounce = true;
			}

			// Bounce on walls
			if (didBounce)
			{
				currentVelocity.multiplyVelocity(BOUNCE_VELOCITY_REDUCTION);

				lastBouncedBubble = null;
				getCollisionData();
			}

			// Check for any bubble collision
			checkBubbleCollision();

			// Reduce velocity
			currentVelocity.multiplyVelocity(NORMAL_VELOCITY_REDUCTION);

			// Additional slow if you are close to dying
			if ((currentPlayer == 0 && currentVelocity.y > 0 && currentPosition.y >= BBMain.GAME_HEIGHT - 50) || 
					(currentPlayer == 1 && currentVelocity.y < 0 && currentPosition.y <= 50))
				currentVelocity.multiplyVelocity(NOOB_VELOCITY_REDUCTION);

			double speed = currentVelocity.calculateSpeed();
			if (speed < 0.25)
			{
				currentState = BUBBLE_EXPANDING;
				// Get the largest possible expanded size 
				expandedRadius = (int) gameModal.expandedBubbleSizeAtPoint(currentPosition);
			}
			else if (speed < 2)
				currentVelocity.multiplyVelocity(BOUNCE_VELOCITY_REDUCTION);
		}
		// Launching state
		else if (currentState == BUBBLE_EXPANDING)
		{
			if (currentRadius < expandedRadius)
				currentRadius += 3;
			else
			{
				currentRadius = expandedRadius;
				currentState = BUBBLE_EXPANDED;
				timer.stop();
				gameModal.finishedFiring();
			}
		}
		else if (currentState == BUBBLE_EXPLODING)
		{
			if (currentAlpha < 30)
			{
				currentRadius += 6;
				currentAlpha++;
			}
			else
			{
				currentState = BUBBLE_EXPANDED;
				stopTimer();
				gameModal.popBubble(this);
			}
		}

		BBMain.getSingleton().repaintNow();
	}

	/** Stop this bubble's timer (called when player presses quit, bubble finished expanding, or game over)
	 */
	public void stopTimer()
	{
		// Check since this bubble might be the Center Bubble
		if (timer != null)
			timer.stop();
	}

	/** Responds to timer events and updates the bubble animation (by calling update() method)
	 */
	public void actionPerformed(ActionEvent event)
	{
		update();
	}

	/** Gets the current radius of this bubble 
	 * @return The current radius of this bubble
	 */
	public int getRadius()
	{
		return currentRadius;
	}

	/** Gets the current state of this bubble (Launching/Moving/Expanding/Expanded)
	 * @return The current state of this bubble
	 */
	public int getState()
	{
		return currentState;
	}

	/** Gets the current coordinate of this bubble
	 * @return The current coordinate of this bubble
	 */
	public BBPoint getPosition()
	{
		return currentPosition;
	}

	/** Gets the current velocity of this bubble
	 * @return The current velocity of this bubble
	 */
	public BBPoint getVelocity()
	{
		return currentVelocity;
	}

	/** Gets the last bubble (if any) that this bubble collided with
	 * @return The last bubble (if any) that this bubble collided with
	 */
	public BBBubble getLastBubble()
	{
		return lastBouncedBubble;
	}

	/** Gets the HP (Health Points) of a bubble
	 * @return The number of hits left before this bubble will pop
	 */
	public int getHP()
	{
		return currentHP;
	}

	/** Set this bubble's current state (exploding)
	 */
	public void setState(int s)
	{
		currentState = s;
	}
}
