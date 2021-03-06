package woohoo.gameworld.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class PositionComponent implements Component
{
	public enum Orientation
	{
		North("up"), 
		South("down"),
		West("left"),
		East("right");
		
		private String text;
		
		Orientation(String str)
		{
			text = str;
		}
		
		public String text()
		{
			return text;
		}
		
		public static Orientation fromString(String str) 
		{
			for (Orientation b : Orientation.values()) 
			{
				if (b.text.equalsIgnoreCase(str))
				{
					return b;
				}
			}
			throw new IllegalArgumentException("No Orientation with text " + str + " found.");
		}
		
		// Returns the direction pos1 needs to face pos2
		public static Orientation fromVectors(Vector2 pos1, Vector2 pos2)
		{		
			Vector2 diff = pos1.cpy().sub(pos2);
			if (Math.abs(diff.x) > Math.abs(diff.y))
			{
				if (diff.x > 0)
					return Orientation.West;
				else
					return Orientation.East;
			}
			else
			{
				if (diff.y > 0)
					return Orientation.North;
				else
					return Orientation.South;
			}
		}

		// Random orientation
		public static Orientation getRandom()
		{		
			int randomNum = (int)Math.floor(Math.random() * 4);
			
			return Orientation.values()[randomNum];
		}
		
		public Vector2 getVector()
		{
			switch(text)
			{
				case "up":
					return new Vector2(0, -1);
				case "down":
					return new Vector2(0, 1);
				case "left":
					return new Vector2(-1, 0);
				case "right":
					return new Vector2(1, 0);
			}
			return new Vector2(0, 0);
		}
		
		public float getAngle(boolean radians)
		{
			float angle = 0;
			switch(text)
			{
				case "up":
					angle = 90;
					break;
				case "down":
					angle = 270;
					break;
				case "left":
					angle = 0;
					break;
				case "right":
					angle = 180;
					break;
			}
			return radians ? angle / 180 * (float)Math.PI : angle;
		}
	}
	
	public Vector2 position;
	public Orientation orientation;
	
	public PositionComponent()
	{
		this(0, 0);
	}
	
	public PositionComponent(float startX, float startY)
	{
		this(new Vector2(startX, startY));
	}
	
	public PositionComponent(Vector2 pos)
	{
		position = pos;
		orientation = Orientation.South;
	}
}
