/** A better version of the Point object that is able to use doubles to store the x and y coordinates to provide greater precision
 * @author AjwardSunYu
 */
public class BBPoint
{
	public double	x;
	public double	y;

	/** Constructor - Initializes with the given x and y coordinates
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 */
	public BBPoint(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	/** Constructor - Initializes by adding the coordinates. It is like adding vectors 'A' and 'B' to get vector 'C'
	 * @param p1 The first BBPoint
	 * @param p2 The second BBPoint
	 */
	public BBPoint(BBPoint p1, BBPoint p2)
	{
		x = p1.x + p2.x;
		y = p1.y + p2.y;
	}

	/** Constructor - Initializes a default BBPoint
	 */
	public BBPoint()
	{
		x = 0;
		y = 0;
	}

	/** Adds the X and Y values of a BBPoint to the current one. It is like adding vectors 'A' and 'B' to get vector 'C'
	 * @param p The given velocity
	 */
	public void addVelocity(BBPoint p)
	{
		x += p.x;
		y += p.y;
	}

	/** Multiplies the X and Y values of a BBPoint to the current one
	 * @param p The given velocity
	 */
	public void multiplyVelocity(BBPoint p)
	{
		x *= p.x;
		y *= p.y;
	}

	/** Calculates the speed assuming that the coordinates stores the velocities
	 * @return The calculated speed using the distance formula
	 */
	public double calculateSpeed()
	{
		return Math.sqrt(x * x + y * y);
	}
}
