/** Contains data about a collision between a shot and a circle 
 * @author AjwardSunYu
 */
public class BBCollision
{
	// The coordinate where the bubble will collide
	public BBPoint	point;
	// The angle that the bubble will bounce off at
	public double	angle;
	// The bubble that will be hit
	public BBBubble	bubble;
	// Whether or not a collision occurred
	public boolean	wasHit;

	/** Constructor
	 * @param p The collision point of the shot
	 * @param a The angle that the shot will bounce off at
	 * @param b The bubble that the shot will collide with
	 */
	public BBCollision(BBPoint p, double a, BBBubble b)
	{
		point = p;
		angle = a;
		bubble = b;
	}
}
