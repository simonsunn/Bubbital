import java.awt.Point;

public class OBPoint extends Point
{
	public double	x;
	public double	y;

	public OBPoint(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	public OBPoint(OBPoint p1, OBPoint p2)
	{
		this.x = p1.x + p2.x;
		this.y = p1.y + p2.y;
	}
	public OBPoint()
	{
		this.x = 0;
		this.y = 0;
	}
	public void addVelocity(OBPoint p)
	{
		this.x += p.x;
		this.y += p.y;
	}
}
