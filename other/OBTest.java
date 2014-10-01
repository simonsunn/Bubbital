import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/* Test calss for Bubbital game
 * Draw anywhere on the screen to move the "turret" and watch where it bounces back at :)
 */

// A Frame for the main program
public class OBTest extends JFrame
{
    // Main drawing area inside the main frame
    private DrawingPanel drawingArea;

    /** Constructs a new main frame
      */
    public OBTest ()
    {
	super ("OBTest");
	setLocation (50, 0);
	Container contentPane = getContentPane ();
	drawingArea = new DrawingPanel ();
	contentPane.add (drawingArea, BorderLayout.CENTER);
    }


    // Inner class for the drawing area
    private class DrawingPanel extends JPanel implements KeyListener
    {
	private static final int bubbleRadius = 130;
	private static final int turretRadius = 25;
	private static final int kLineLength = 1000;

	private OBPoint turretPoint;
	private OBPoint bubblePoint;
	private OBPoint collisionPoint;
	private OBPoint bouncePoint;

	private double angle;

	private Timer timer;
	private boolean timerToggle;
	private double rotateClockwise;
	/** Constructs a new DrawingPanel object
	  */
	public DrawingPanel ()
	{
	    setBackground (Color.black); //(new Color (39, 172, 252));
	    setPreferredSize (new Dimension (600, 720));

	    // Add key listenere
	    setFocusable (true);
	    requestFocusInWindow ();
	    addKeyListener (this);

	    turretPoint = new OBPoint (300, 600);
	    bubblePoint = new OBPoint (200, 600);
	    collisionPoint = new OBPoint (200, 500);
	    bouncePoint = new OBPoint (0, 0);
	    angle = 90;
	    rotateClockwise = -0.5;

	    timer = new Timer (50, new TimerEventHandler ());
	    timer.start ();

	    addMouseListener (new MouseHandler ());
	    addMouseMotionListener (new MouseMotionHandler ());
	}


	public double getAngle (double A, double B)
	{
	    return Math.toDegrees (Math.atan (-A / B));
	}

	public double distanceBetweenPoints (OBPoint p, double x, double y)
	{
	    double xDiff = p.x - x;
	    double yDiff = p.y - y;
	    return Math.sqrt (xDiff * xDiff + yDiff * yDiff);
	}
	public void update ()
	{
	    // Update line
	    collisionPoint.x = -kLineLength * Math.cos (Math.toRadians (angle)) + turretPoint.x;
	    collisionPoint.y = -kLineLength * Math.sin (Math.toRadians (angle)) + turretPoint.y;

	    // Check for collision
	    double x1, x2, y1, y2;
	    double A = collisionPoint.y - turretPoint.y;
	    double B = turretPoint.x - collisionPoint.x;
	    double C = collisionPoint.x * turretPoint.y - turretPoint.x * collisionPoint.y;
	    double cx = bubblePoint.x;
	    double cy = bubblePoint.y;
	    double r = bubbleRadius / 2;
	    double i = B * B + A * A;
	    double j = (2 * B * C) + (2 * A * B * cx) - (2 * A * A * cy);
	    double k = (C * C) + (2 * A * C * cx) + (A * A * cx * cx) + (A * A * cy * cy) - (A * A * r * r);
	    double discriminant = j * j - 4 * i * k;
	    if (discriminant >= 0)
	    {
		// Calculate the first coordinate
		y1 = (-j - Math.sqrt (discriminant)) / (2 * i);
		x1 = -1 * (B * y1 + C) / A;
		collisionPoint.x = x1;
		collisionPoint.y = y1;
		double distance = distanceBetweenPoints (turretPoint, x1, y1);
		if (discriminant > 0)
		{
		    // Secant intersection
		    y2 = (-j + Math.sqrt (discriminant)) / (2 * i);
		    x2 = -1 * (B * y2 + C) / A;
		    if (distanceBetweenPoints (turretPoint, x2, y2) < distance)
		    {
			collisionPoint.x = x2;
			collisionPoint.y = y2;
		    }
		}

		// Calculate bounce angle
		double firedAngle = getAngle (A, B);

		// Reuse some variables like (A, B, C)
		OBPoint p1 = bubblePoint;
		OBPoint p2 = collisionPoint;
		A = p2.y - p1.y;
		B = p1.x - p2.x;
		C = p2.x * p1.y - p1.x * p2.y;
		// Angle of the tangant

		double tanAngle = getAngle (B * B, -A * B); // to get the bottom portion
		double bouAngle = 0;
		// Special cases:
		if (B == 0 && (firedAngle == 90 || firedAngle == 270))
		{
		    System.out.println ("  \n\n          collision at special angle (vertical)");
		    bouAngle = 360 - firedAngle;
		}
		/*else if (firedAngle == 0 || firedAngle == 180)
		{
		    System.out.println ("collision at special angle (horizontal)" + tanAngle);
		    bouAngle = tanAngle;
		    if (tanAngle < 0)
			    bouAngle = 180 + tanAngle;
		}*/
		else if (turretPoint.y < bubblePoint.y)
		{
		    System.out.print ("top, shooting ");
		    // If the collision point is below the turret point then we are firing down
		    if (turretPoint.y < collisionPoint.y)
		    {
			// Firing DOWN
			System.out.println ("down");
			if (firedAngle < 0)
			    firedAngle += 180;
			firedAngle = 180 - firedAngle;
			bouAngle = 2 * tanAngle + firedAngle;
		    }
		    // If the turret point is to the RIGHT of the bubble
		    else if (turretPoint.x > collisionPoint.x)
		    {
			// Firing LEFT
			System.out.println ("left");
			if (firedAngle < 0)
			    firedAngle += 180;
			bouAngle = 2 * tanAngle - firedAngle;
		    }
		    // If the turret point is to the LEFT of the bubble
		    else
		    {
			// Firing RIGHT
			System.out.println ("Right");
			bouAngle = (180 + tanAngle) + (tanAngle - firedAngle);
		    }
		}
		// MAIN CASE WHERE TURRET IS below COLLISION BUBBLE
		else
		{
		    System.out.print ("bot, shooting ");
		    // Left and right side firing down
		    // Main case for turret below bubble firing UP
		    if (turretPoint.y > collisionPoint.y)
		    {
			// Firing UP
			System.out.println ("up");
			if (firedAngle < 0)
			    firedAngle += 180;
			bouAngle = 2 * tanAngle - firedAngle;
		    }
		    // If the turret point is to the RIGHT of the bubble
		    else if (turretPoint.x > collisionPoint.x)
		    {
			// Firing LEFT
			System.out.println ("left");
			if (firedAngle < 0)
			    firedAngle += 180;
			//System.out.println ("tan angle :" + (tanAngle));
			bouAngle = 360 - 2 * Math.abs (tanAngle) + 180 - firedAngle;
		    }
		    // If the turret point is to the LEFT of the bubble
		    else
		    {
			// Firing RIGHT
			System.out.println ("Right");
			if (firedAngle < 0)
			    firedAngle += 180;
			firedAngle = 180 - firedAngle;
			bouAngle = 2 * tanAngle + firedAngle;
		    }
		}

		// Special CaSes
		// fired angle is 0 degrees (facing directly towards left)
		if (firedAngle == 0 /* && originalP1.x < originalP2.x*/)
		{
		    System.out.println ("0");
		    bouAngle = 360 - 2 * tanAngle;
		}

		//System.out.println ("fired: " + firedAngle + ", tan is: " + tanAngle + ", bou is: " + bouAngle + "\n");

		// Calculate the bounce point
		bouncePoint.x = -kLineLength * Math.cos (Math.toRadians (bouAngle)) + collisionPoint.x;
		bouncePoint.y = -kLineLength * Math.sin (Math.toRadians (bouAngle)) + collisionPoint.y;
	    }
	    else
	    {
		collisionPoint.x = -kLineLength * Math.cos (Math.toRadians (angle)) + turretPoint.x;
		collisionPoint.y = -kLineLength * Math.sin (Math.toRadians (angle)) + turretPoint.y;
		//bouncePoint = collisionPoint;
	    }

	    System.out.println ("x: " + collisionPoint.x + ", y: " + collisionPoint.y + ", angle: " + angle);

	    angle += rotateClockwise;

	    // Redraw
	    drawingArea.paintImmediately (0, 0, drawingArea.getWidth (), drawingArea.getHeight ());
	}
	/** Repaint the drawing panel
	  * @param g The Graphics context
	  */
	public void paintComponent (Graphics g)
	{
	    super.paintComponent (g);
	    g.setColor (Color.white);

	    // main bubble
	    g.fillOval ((int) bubblePoint.x - bubbleRadius / 2, (int) bubblePoint.y - bubbleRadius / 2, bubbleRadius, bubbleRadius);

	    // turret point
	    g.setColor (Color.magenta);
	    g.fillOval ((int) turretPoint.x - turretRadius / 2, (int) turretPoint.y - turretRadius / 2, turretRadius, turretRadius);

	    // collision line
	    g.setColor (Color.green);
	    g.drawLine ((int) turretPoint.x, (int) turretPoint.y, (int) collisionPoint.x, (int) collisionPoint.y);

	    // bounce line
	    g.setColor (Color.green);
	    g.drawLine ((int) bouncePoint.x, (int) bouncePoint.y, (int) collisionPoint.x, (int) collisionPoint.y);
	} // paint component method

	public void keyPressed (KeyEvent event)
	{
	    if (event.getKeyCode () == KeyEvent.VK_R)
		rotateClockwise *= -1;
	    else if (event.getKeyCode () == KeyEvent.VK_A)
		angle = 90;
	    else if (event.getKeyCode () == KeyEvent.VK_S)
		angle = 180;
	    else if (event.getKeyCode () == KeyEvent.VK_D)
		angle = 270;
	    else if (event.getKeyCode () == KeyEvent.VK_F)
		angle = 0;
	    else
	    {
		if (timerToggle)
		    timer.start ();
		else
		    timer.stop ();
	    }
	    timerToggle = !timerToggle;
	}

	// Extra methods needed since this game board is a KeyListener
	public void keyReleased (KeyEvent event)
	{
	}

	public void keyTyped (KeyEvent event)
	{
	}


	private Point lastPoint;
	boolean isClickDown;
	private class MouseHandler extends MouseAdapter
	{
	    public void mousePressed (MouseEvent event)
	    {
		lastPoint = event.getPoint ();
	    }
	}


	private class MouseMotionHandler extends MouseMotionAdapter
	{
	    public void mouseDragged (MouseEvent event)
	    {
		Point newPoint = event.getPoint ();

		turretPoint.x += newPoint.x - lastPoint.x;
		turretPoint.y += newPoint.y - lastPoint.y;

		lastPoint = newPoint;
	    }
	}


	private class TimerEventHandler implements ActionListener
	{
	    public void actionPerformed (ActionEvent event)
	    {
		update ();
	    }
	}
    }


    public static void main (String[] args)
    {
	// Create the main frame
	OBTest mainFrame = new OBTest ();
	mainFrame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	mainFrame.pack ();
	mainFrame.setVisible (true);
    }
}



