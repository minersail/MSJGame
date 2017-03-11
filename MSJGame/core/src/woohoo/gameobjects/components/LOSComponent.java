package woohoo.gameobjects.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import woohoo.framework.contactcommands.ContactData;
import woohoo.gameobjects.components.MapObjectComponent.Direction;

public class LOSComponent implements Component
{
	public Body mass;
	public Fixture fixture;
	
    public LOSComponent(World world) 
    {
		BodyDef bodyDef = new BodyDef();
		mass = world.createBody(bodyDef);
        mass.setType(BodyDef.BodyType.DynamicBody);
		mass.setUserData(new ContactData());
		
		PolygonShape shape = new PolygonShape();
		float LOSradius = 5;
		
		float SIN60 = (float)Math.sin(Math.PI / 3);
		float COS60 = (float)Math.cos(Math.PI / 3);
		float SIN70 = (float)Math.sin(1.22);
		float COS70 = (float)Math.cos(1.22);
		float SIN80 = (float)Math.sin(1.4);
		float COS80 = (float)Math.cos(1.4);
		
		Vector2[] vertices = {new Vector2(0, 0), new Vector2(LOSradius * SIN60, LOSradius * COS60), new Vector2(LOSradius * SIN70, LOSradius * COS70), 
							  new Vector2(LOSradius * SIN80, LOSradius * COS80), new Vector2(LOSradius * SIN60, LOSradius * -COS60),
							  new Vector2(LOSradius * SIN70, LOSradius * -COS70), new Vector2(LOSradius * SIN80, LOSradius * -COS80)};
		
		shape.set(vertices);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		
        fixture = mass.createFixture(fixtureDef);
		fixture.setSensor(true);
        fixture.setDensity(100f);
        fixture.setFriction(0);
        fixture.setRestitution(0);
    }
	
	public void rotate(Direction direction)
	{
		switch(direction)
		{
			case Up:
				mass.setTransform(mass.getPosition(), 3 * (float)Math.PI / 2);
				break;
			case Down:
				mass.setTransform(mass.getPosition(), (float)Math.PI / 2);
				break;
			case Left:
				mass.setTransform(mass.getPosition(), (float)Math.PI);
				break;
			case Right:
				mass.setTransform(mass.getPosition(), 0);
				break;
		}
	}
}
