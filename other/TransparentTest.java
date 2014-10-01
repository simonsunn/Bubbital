import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/* Test code for transparency - Looks pretty cool
 */
public class TransparentTest extends JFrame
{
    public TransparentTest ()
    {
	super ("Transparent Test");

	TransPanel mybad = new TransPanel ();
	getContentPane ().add (mybad);
	pack ();
    }


    private class TransPanel extends JPanel
    {
	private int gap = 10, width = 60, offset = 20, deltaX = gap + width + offset;
	private Rectangle blueSquare = new Rectangle (gap + offset, gap + offset, width, width), redSquare = new Rectangle (gap, gap, width, width);

	public TransPanel ()
	{
	    setPreferredSize (new Dimension (1100, 300));
	}

	private void drawSquares (Graphics2D g2d, float alpha)
	{
	    Composite originalComposite = g2d.getComposite ();
	    g2d.setPaint (Color.blue);
	    g2d.fillOval (10,100, 80,80);
	    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, alpha));
	    g2d.setPaint (Color.red);
	    g2d.fillOval (10,115,80,80);
	    g2d.setComposite (originalComposite);
	}


	public void paintComponent (Graphics g)
	{
	    super.paintComponent (g);
	    Graphics2D g2d = (Graphics2D) g;
	    for (int i = 0 ; i < 11 ; i++)
	    {
		drawSquares (g2d, i * 0.1F);
		g2d.translate (deltaX, 0);
	    }
	}
    }


    public static void main (String[] args)
    {
	TransparentTest OBOne = new TransparentTest ();
	OBOne.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	OBOne.setLocation (500, 0);
	OBOne.setVisible (true);
	OBOne.setResizable (false);
    }
}
