import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.Timer;

public class BBGameModal implements KeyListener, ActionListener
{
	// Variables about the game
	private final static int	TURRET_UPDATE_DELAY	= 15;
	private int					currentPlayer;				// bottom index of 0
	// Which player lost (first or second/computer)
	private int					playerWhoWon;
	private boolean				isPlaying;
	// Prevent multiple keyboard presses from firing shots in rapid successions
	private boolean				keyboardSpamProtection;
	// Variables for the UI and animations
	private Timer				timer;
	private int					turretUpdateDelay;
	// Keep track of the last fired bubble to stop animations if player wants to quit
	private BBBubble			lastFiredBubble;
	// Variables for multiplayer (keep track of the rotation angle of the screen)
	private int					currentRotateAngle;
	private boolean				shouldRotateScreen;
	// Variable for ame over animations
	private boolean				isPoppingAllBubbles;
	// Whether the game was quit with 'q'
	private boolean				wasGameQuitWithQ;
	private int					AIFireAngle;

	// Array (lists) that store multiple  Turrets (and Bubbles)
	private BBTurret[]			turrets;
	private ArrayList<BBBubble>	bubbles;

	/* Variables related to the score
	 * One round is the time a bubble is either launching or expanding
	 * 1 pop = 1^2 = 1
	 * 3 pops= 3^2 = 9
	 */
	private int					poppedThisRound;			// add ^ 2 to score
	/* Number of hits this round. Adds a arithmetic sum to score
	*  3 hits = 1+2+3 = 3*(3+1)/2
	*  5 hits = 1+2+3+4+5 = 5*(5+1)/2
	*/
	private int					hitsThisRound;
	// The sum of all the points earned in the current game
	private int					currentScore;

	/** Constructor
	 */
	public BBGameModal()
	{
		BBMain.getSingleton().getDrawingPanel().addKeyListener(this);
		bubbles = new ArrayList<BBBubble>(20);
		timer = new Timer(BBMain.TIMER_DELAY, this);
	}

	/** Start the game if one hasn't began yet
	 * @param BBMain..getSingleton().currentGameMode The number of players in the game (1 or 2)
	 */
	public void newGame(int gm)
	{
		if (isPlaying)
			return;

		// Reset variables
		bubbles.clear();

		keyboardSpamProtection = false;
		lastFiredBubble = null;
		currentPlayer = 0;
		currentRotateAngle = 0;
		turretUpdateDelay = 0;
		isPoppingAllBubbles = false;
		poppedThisRound = 0;
		hitsThisRound = 0;
		currentScore = 0;
		wasGameQuitWithQ = false;
		playerWhoWon = 0;

		// If there are 2 players (two player/AI mode), place a small circle in the center
		if (BBMain.getSingleton().currentGameMode != BBMain.GAME_MODE_ONE_PLAYER)
		{
			BBBubble centerBubble = new BBBubble();
			centerBubble.gameModal = this;
			bubbles.add(centerBubble);
		}

		// Add turret(s)
		turrets = new BBTurret[Math.min(BBMain.getSingleton().currentGameMode, 2)]; // So that if game mode is AI, it will also use 2 turrets
		for (int i = 0; i < turrets.length; i++)
			turrets[i] = new BBTurret(i);

		isPlaying = true;
		timer.start();
	}
	
	/** Show the game over screen as well as the winner
	 */
	public void showGameOver()
	{
		if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_ONE_PLAYER)
			BBMain.getSingleton().showGameOver(currentScore);
		else
		{
			if (wasGameQuitWithQ)
				// Show the menu instead of game over since no one won (bonus marks -> pun :) or lost
				BBMain.getSingleton().showMenu();
			else
				BBMain.getSingleton().showGameOver(playerWhoWon);
		}
	}

	/** Stops the game and shows the game over screen as well as the score
	 */
	public void gameOver()
	{
		isPlaying = false;
		addRoundScore();

		// Get winner if needed
		if (BBMain.getSingleton().currentGameMode != BBMain.GAME_MODE_ONE_PLAYER)
		{
			if (currentPlayer == 0)
				playerWhoWon = 2;
			else if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_TWO_PLAYER)
				playerWhoWon = 1;
			else
				playerWhoWon = 3;
			System.out.println("winner: " + playerWhoWon);
		}

		// Loop through array list of bubbles and tell them to animate exploding
		isPoppingAllBubbles = true;
		for (int i = 0; i < bubbles.size(); i++)
			bubbles.get(i).setState(BBBubble.BUBBLE_EXPLODING);
	}

	/** A bubble was hit so increment the counter
	 */
	public void hitBubble()
	{
		hitsThisRound++;
	}

	/** Update any game objects and tells the JPanel to redraw immediately
	 */
	public void update()
	{
		// If we are popping all the bubbles,
		if (isPoppingAllBubbles)
		{
			if (bubbles.size() == 0)
			{
				timer.stop();

				// Popped all the bubbles, we will delay for a set time and show the game over menu
				if (!wasGameQuitWithQ)
					BBMain.delay(BBMain.SHOW_GAME_OVER_DELAY);

				// Show the game over menu
				showGameOver();
				return;
			}
			for (int i = 0; i < bubbles.size(); i++)
				bubbles.get(i).update();
		}
		else
		{
			// Update the turret
			if ((BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_ONE_PLAYER && turretUpdateDelay <= 0) ||
					(BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_TWO_PLAYER && turretUpdateDelay == 0 && !shouldRotateScreen))
				turrets[currentPlayer].update();
			else
				turretUpdateDelay--;
			if (shouldRotateScreen)
			{
				if (currentPlayer % 2 == 1 && currentRotateAngle < 180)
					currentRotateAngle += 3;
				else if (currentPlayer % 2 == 0 && currentRotateAngle > 0)
					currentRotateAngle -= 3;
				else
				{
					turretUpdateDelay = 0;
					shouldRotateScreen = false;

					// Check
					if (BBMain.getSingleton().isAI && currentPlayer == 1)
					{
						// AI MOVE
						// Different animation
						AIFireAngle = AICalculateFireAngle();
						// Pause timer
						timer.stop();
						turrets[1].rotateTo(AIFireAngle);
						timer.start();
						// Always reset to '0'
						System.out.println("AI fired: " + AIFireAngle);
						fire();
					}
				}
			}
		}
		// Repaint
		BBMain.getSingleton().repaintNow();
	}

	/** The the score recieved this round
	 */
	public void addRoundScore()
	{
		// Add combo's
		if (hitsThisRound > 0)
			currentScore += hitsThisRound * (hitsThisRound + 1) / 2 + poppedThisRound * poppedThisRound;

		hitsThisRound = 0;
		poppedThisRound = 0;
	}

	/** Fire a bubble from the current player's turret
	 */
	private void fire()
	{
		// Prevent any more keyboard presseds (except for q - quit) before done firing
		keyboardSpamProtection = true;

		// Reset the stats for this round
		hitsThisRound = 0;
		poppedThisRound = 0;

		// Always specify 0 because the turret with index 0 will be the player(s) turrets, 1 is the computer's turret
		lastFiredBubble = turrets[currentPlayer].fire();
		lastFiredBubble.gameModal = this;
		bubbles.add(lastFiredBubble);
		lastFiredBubble.launch();

		// Delay depends on game mode
		turretUpdateDelay = TURRET_UPDATE_DELAY;
		if (BBMain.getSingleton().currentGameMode != BBMain.GAME_MODE_ONE_PLAYER)
			turretUpdateDelay = -1;
	}

	/** Called when the last bubble fired has finished moving so the turret can be fired again
	 */
	public void finishedFiring()
	{
		// Re-allow space bar key pressed
		keyboardSpamProtection = false;
		lastFiredBubble = null;

		// set the angle for the next player to this players angle (for UI looks :)
		double angleToChangeTo = turrets[currentPlayer].getCurrentAngle();
		currentPlayer = (currentPlayer + 1) % Math.min(BBMain.getSingleton().currentGameMode, 2); // Always be either 0 or 1
		System.out.println("player #:" + currentPlayer);
		turrets[currentPlayer].setAngle(angleToChangeTo);

		// Add the score to the current round (if needed)
		if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_ONE_PLAYER)
			addRoundScore();

		else if (BBMain.getSingleton().currentGameMode == BBMain.GAME_MODE_TWO_PLAYER)
		{
			System.out.println("rotate");
			turretUpdateDelay = 0;
			shouldRotateScreen = true;
		}
	}
	
	/** Basic AI: Calculates a random angle to shoot at, and on without collision if possible
	 */
	public int AICalculateFireAngle()
	{
		// Loop through all the angles at 5 degree intervals and find one where there is no collision
		// Use distance formula to check for if there will be collision

		// Calculate points
		BBPoint p1 = turrets[1].getBBPoint();
		int rand = (int) (Math.random() * 160 + 10); // 10 - 170
		for (int angle = rand; angle < 160; angle += 3)
		{
			BBPoint p2 = new BBPoint(5 * Math.cos(Math.toRadians(angle)), 5 * Math.sin(Math.toRadians(angle)));
			p2.addVelocity(p1);

			// Ask if there is collision data
			BBCollision data = checkCollisionFromPositionWithVelocity(p1, p2);
			if (data == null && angle > 35 && angle < 145)
			{
				System.out.println(angle);
				return angle;
			}
		}
		for (int angle = rand; angle > 10; angle -= 3)
		{
			BBPoint p2 = new BBPoint(5 * Math.cos(Math.toRadians(angle)), 5 * Math.sin(Math.toRadians(angle)));
			p2.addVelocity(p1);

			// Ask if there is collision data
			BBCollision data = checkCollisionFromPositionWithVelocity(p1, p2);
			if (data == null && angle > 35 && angle < 145)
			{
				System.out.println(angle);
				return angle;
			}
		}

		System.out.println("random angle");
		// When all else false, go random :)
		return (int) (Math.random() * 160 + 10);
	}

	/** Removes the given bubble from the bubbles array list.
	 *  Also adds animations
	 */
	public void popBubble(BBBubble b)
	{
		bubbles.remove(b);

		// Don't add to score if we are quiting
		if (!isPoppingAllBubbles)
			poppedThisRound++;
	}

	/** Checks to see if a bubble will collide with another bubble
	 * @param p1 The current position of the moving bubble
	 * @param p2 The position of the moving bubble in the next frame
	 * @return An OBCollision object with collision info if there will be a collision, null otherwise
	 */
	/* Steps:
	 * 1) create standard equation of a line with the bubble's trajectory
	 * 2) use derived formula to get points of intersections, if any (determined using the determinant of the quadratic formula)
	 * 3) if there is more than one intersection, take the distance closest to the beginning of the line
	 * 4) remember this distance only if:
	 * 		A) it is the shortest distance to the beginning of the line (orininalP1)
	 * 		B) the distance from the collision point to originalP2 is less than the distance from collision point to originalP1
	 * 5) after going through all the bubbles return info (the point of intersection, the angle to reflect at,
	 *    and the bubble hit) if there will be a collision, null otherwise
	 */
	public BBCollision checkCollisionFromPositionWithVelocity(BBPoint p1, BBPoint p2)
	{
		if (bubbles.size() <= 1)
			return null;

		// Remember the original points for later
		BBPoint originalP1 = p1;
		BBPoint originalP2 = p2;

		// Keep track of the collision point and bubble
		double shortestDistance = Double.MAX_VALUE;
		BBPoint collisionPoint = new BBPoint();
		BBBubble collisionBubble = null;

		// Step 1) Get standard equation of line of the turret's trajectory
		double A = p2.y - p1.y;
		double B = p1.x - p2.x;
		double C = p2.x * p1.y - p1.x * p2.y;
		double firedAngle = calculateAngle(A, B);

		// Go through all the bubbles
		for (int bubbleIndex = 0; bubbleIndex < bubbles.size() - 1; bubbleIndex++)
		{
			// Make sure we are not checking the launching bubble nor the last bubble that it hit
			BBBubble bubble = bubbles.get(bubbleIndex);
			if (bubble != bubble.getLastBubble() && bubble != lastFiredBubble && bubble.getHP() >= BBBubble.BUBBLE_HP_RED)
			{
				// The center XY coordinates and radius of the current bubble
				double cx = bubble.getPosition().x;
				double cy = bubble.getPosition().y;
				double r = bubble.getRadius();

				// Create variables to store the POI(s)
				double x1, x2, y1, y2;

				// Step 2) The following is a derived formula to calculate the POI(s) of a circle and line
				double i = B * B + A * A;
				double j = (2 * B * C) + (2 * A * B * cx) - (2 * A * A * cy);
				double k = (C * C) + (2 * A * C * cx) + (A * A * cx * cx) + (A * A * cy * cy) - (A * A * r * r);
				double discriminant = j * j - 4 * i * k;

				// Check for any collision(s)
				if (discriminant >= 0)
				{
					// Calculate the first coordinate
					y1 = (-j - Math.sqrt(discriminant)) / (2 * i);
					x1 = -1 * (B * y1 + C) / A;

					double distance = distanceBetween(originalP1, x1, y1);
					double distance1 = distanceBetween(originalP2, x1, y1);

					// Remember the shortest distance (either the old value or this new calculation
					if (distance1 < distance && distance < shortestDistance)
					{
						//System.out.println("first :" + distance + "px at (" + x1 + ", " + y1 + ")");
						shortestDistance = distance;
						collisionBubble = bubble;
						collisionPoint.x = x1;
						collisionPoint.y = y1;
					}

					// Step 3) Check if there is 2 collisions
					if (discriminant > 0)
					{
						// Secant intersection
						y2 = (-j + Math.sqrt(discriminant)) / (2 * i);
						x2 = -1 * (B * y2 + C) / A;
						distance = distanceBetween(originalP1, x2, y2);
						distance1 = distanceBetween(originalP2, x2, y2);
						//System.out.println("second :" + distance + "px at (" + x2 + ", " + y2 + ")");

						// Recheck the shortest distance
						if (distance1 < distance && distance < shortestDistance)
						{
							shortestDistance = distance;
							collisionBubble = bubble;
							collisionPoint.x = x2;
							collisionPoint.y = y2;
						}
					} // Second case
				} // Check discriminant
			} // Make sure didn't hit this bubble before
		} // Loop through all bubbles

		if (collisionBubble == null)
			return null;

		// Step 5) Get the collision data
		// First check for special cases (example: hitting the bubble at 90 degree straight on from bottom)
		/* Please note: if hitting at 0/180 degrees, the tangent angle would give NaN (division by zero)
		 * However since bouncing horizontally is highly unlikely, we have not added a special case for this
		*/
		if ((B == 0 && (firedAngle == 90 || firedAngle == 270)))
		{
			// Only checked for 90 and 270 degrees
			System.out.println("  \n\n          collision at special angle");
			return new BBCollision(collisionPoint, 360 - firedAngle, collisionBubble);
		}

		// Reuse some variables like (A, B, C)
		p1 = collisionBubble.getPosition();
		p2 = collisionPoint;
		A = p2.y - p1.y;
		B = p1.x - p2.x;
		C = p2.x * p1.y - p1.x * p2.y;

		double tanAngle = calculateAngle(B * B, -A * B); // to get the bottom portion
		double bouAngle = 0;

		// MAIN CASE WHERE TURRET IS above COLLISION BUBBLE
		if (originalP1.y < collisionBubble.getPosition().y)
		{
			System.out.print("top, shooting ");
			// If the collision point is below the turret point then we are firing down
			if (originalP1.y < collisionPoint.y)
			{
				// Firing DOWN
				System.out.println("down");
				if (firedAngle < 0)
					firedAngle += 180;
				firedAngle = 180 - firedAngle;
				bouAngle = 2 * tanAngle + firedAngle;
			}
			// If the turret point is to the RIGHT of the bubble
			else if (originalP1.x > collisionPoint.x)
			{
				// Firing LEFT
				System.out.println("left");
				if (firedAngle < 0)
					firedAngle += 180;
				bouAngle = 2 * tanAngle - firedAngle;
			}
			// If the turret point is to the LEFT of the bubble
			else
			{
				// Firing RIGHT
				System.out.println("Right");
				bouAngle = (180 + tanAngle) + (tanAngle - firedAngle);
			}
		}
		// MAIN CASE WHERE TURRET IS below COLLISION BUBBLE
		else
		{
			System.out.print("bot, shooting ");
			// Left and right side firing down
			// Main case for turret below bubble firing UP
			if (originalP1.y > collisionPoint.y)
			{
				// Firing UP
				System.out.println("up");
				if (firedAngle < 0)
					firedAngle += 180;
				bouAngle = 2 * tanAngle - firedAngle;
			}
			// If the turret point is to the RIGHT of the bubble
			else if (originalP1.x > collisionPoint.x)
			{
				// Firing LEFT
				System.out.println("left");
				if (firedAngle < 0)
					firedAngle += 180;
				//System.out.println ("tan angle :" + (tanAngle));
				bouAngle = 360 - 2 * Math.abs(tanAngle) + 180 - firedAngle;
			}
			// If the turret point is to the LEFT of the bubble
			else
			{
				// Firing RIGHT
				System.out.println("Right");
				if (firedAngle < 0)
					firedAngle += 180;
				firedAngle = 180 - firedAngle;
				bouAngle = 2 * tanAngle + firedAngle;
			}
		}

		return new BBCollision(collisionPoint, bouAngle, collisionBubble);
	}

	/** Calculates the radius of a bubble at the given point (the shortest distance to any bubble or wall)
	 * @return The radius
	 */
	public double expandedBubbleSizeAtPoint(BBPoint p)
	{
		/* Take the shortest distance of either the
		 * 1) the shortest distance from the given point to the 4 walls
		 * 2) distance to closest bubble minus that bubble's radius
		 */

		// Get minimum distance to walls
		double horizDistance = Math.min(p.x - BBMain.BORDER_SIZE, BBMain.MAIN_WIDTH - BBMain.BORDER_SIZE - p.x);
		double vertiDistance = Math.min(p.y - BBMain.BORDER_SIZE, BBMain.MAIN_HEIGHT - BBMain.BORDER_SIZE - p.y - 100);
		double size = Math.min(horizDistance, vertiDistance);

		// Check all bubbles EXCEPT last one because that is the one that is expanding
		for (int i = 0; i < bubbles.size(); i++)
			if (bubbles.get(i) != lastFiredBubble && bubbles.get(i).getHP() >= BBBubble.BUBBLE_HP_RED)
				size = Math.min(size, distanceBetweenPoints(p, bubbles.get(i).getPosition()) - bubbles.get(i).getRadius());

		// Return the distance
		return size;
	}

	/** Responds to when the player presses a keyboard key (like spacebar)
	  * @param event information about the key pressed event
	  */
	public void keyPressed(KeyEvent event)
	{
		if (!isPlaying)
			return;

		if (event.getKeyCode() == KeyEvent.VK_SPACE && !keyboardSpamProtection)
			fire();
		else if (event.getKeyCode() == KeyEvent.VK_Q)
		{
			if (lastFiredBubble != null)
				lastFiredBubble.stopTimer();
			wasGameQuitWithQ = true;
			gameOver();
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

	/** Responds to timer events and updates the game (by calling update() method)
	 */
	public void actionPerformed(ActionEvent event)
	{
		update();
	}

	/** Gets the array list of all the bubbles
	 * @return An array list of all the bubbles
	 */
	public ArrayList<BBBubble> getBubbles()
	{
		return bubbles;
	}

	/** Gets whether or not a game is underway
	 * @return true if currently playing, false otherwise
	 */
	public boolean isPlaying()
	{
		return isPlaying;
	}

	/** Get the array of turrets
	 * @return a normal array of turrets
	 */
	public BBTurret[] getTurrets()
	{
		return turrets;
	}

	/** Get the current score
	 * @return The current score
	 */
	public int getCurrentScore()
	{
		return currentScore;
	}

	/** Get the current hits
	 * @return The current hits
	 */
	public int getCurrentCombo()
	{
		// Add combo's
		int combo = poppedThisRound * poppedThisRound;
		if (hitsThisRound > 0)
			combo += hitsThisRound * (hitsThisRound + 1) / 2;

		return combo;
	}

	/** Calculates the distance between two given points
	 * @return The distance
	 */
	public static double distanceBetweenPoints(BBPoint p1, BBPoint p2)
	{
		double xDiff = p1.x - p2.x;
		double yDiff = p1.y - p2.y;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	/** Calculates the distance between two given points (accepts x and y coordinate instead of a BBPoint object)
	 * @return The distance
	 */
	public static double distanceBetween(BBPoint p, double x, double y)
	{
		double xDiff = p.x - x;
		double yDiff = p.y - y;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}

	/** Calculates the angle given the 'A' and 'B' values of a standard equation of a line
	 * (of the form Ax + By + C = 0)
	 * @return The angle in degrees
	 */
	public double calculateAngle(double A, double B)
	{
		return Math.toDegrees(Math.atan(-A / B));
	}
	
	/** Getter method for the current rotation angle (multiplayer)
	 */
	public int getRotateAngle()
	{
		return currentRotateAngle;
	}

	/** Getter method for the current rotation player (multiplayer)
	 */
	public int getCurrentPlayer()
	{
		return currentPlayer;
	}

	/** Should the 'game over' border for second player be highlighted red?
	 */
	public boolean shouldTopBorderBeRed()
	{
		return (currentPlayer == 1 && lastFiredBubble != null && lastFiredBubble.getVelocity().y < 0 && lastFiredBubble.getPosition().y < 150);
	}

	/** Should the 'game over' border for first player be highlighted red?
	 */
	public boolean shouldBottomBorderBeRed()
	{
		return (currentPlayer == 0 && lastFiredBubble != null && lastFiredBubble.getVelocity().y > 0 && lastFiredBubble.getPosition().y > BBMain.MAIN_HEIGHT - 150);
	}
} // BBGameModal
