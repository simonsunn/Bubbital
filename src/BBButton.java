import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class BBButton
{
	// Default amount to move this button over on mouseover (positive is right/down, negative is left/up)
	private final static int	MOUSEOVER_OFFSET	= 5;
	// Adds 'padding' to the box to make it easier to click this button
	private final static int	CLICK_AREA			= 15;
	// The default text colour of this button
	private final static Color	DEFAULT_TEXT_COLOUR	= Color.white;

	// Stores the data related to this specific instance of this button
	public String				title;
	public Rectangle			box;
	public Font					font;
	public boolean				isHidden;
	public int					xOffset;
	public int					yOffset;
	public boolean				isMouseOver;
	public Color				normalColour;
	public Color				mouseOverColour;

	/** Constructor
	 * @param t The text to be drawn
	 * @param b The box around the text where the mouse will work
	 * @param f The font used to draw the text
	 */
	public BBButton(String t, Rectangle b, Font f)
	{
		title = t;
		box = b;
		font = f;
		normalColour = Color.white;
		mouseOverColour = DEFAULT_TEXT_COLOUR;
		xOffset = MOUSEOVER_OFFSET;
	}

	/** Draws the button
	 * @param g the graphics to draw with
	 */
	public void draw(Graphics g)
	{
		if (!isHidden)
		{
			g.setFont(font);

			if (isMouseOver)
			{
				g.setColor(mouseOverColour);
				g.drawString(title, box.x + xOffset, box.y + yOffset + box.height);
			}
			else
			{
				g.setColor(normalColour);
				g.drawString(title, box.x, box.y + box.height);
			}
		}
	}

	/** Called when the mouse is moved to check if it is over a button
	 * @param p the point where the mouse is
	 * @return true if the point is inside this button's 'box', false otherwise
	 */
	public boolean recheckMouseOver(Point p)
	{
		isMouseOver = ((p.x > box.x - CLICK_AREA && p.x < box.x + box.width + CLICK_AREA) &&
				(p.y > box.y && p.y - CLICK_AREA < box.y + box.height + CLICK_AREA) && !isHidden);

		return isMouseOver;
	}
}
