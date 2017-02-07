package woohoo.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import woohoo.framework.contactcommands.SensorContact;
import woohoo.framework.fixturedata.HitData;
import woohoo.gameobjects.components.AIComponent;
import woohoo.gameobjects.components.AIComponent.AIMode;
import woohoo.gameobjects.components.SensorComponent;
import woohoo.screens.PlayingScreen.WBodyType;

public class Enemy extends Character
{    
	private SensorComponent hitBox;
	private AIComponent brain;
	
    public Enemy(Texture texture)
    {
		super(new TextureRegion(texture), WBodyType.Enemy);
		
		hitBox = new SensorComponent(WBodyType.Enemy);
		brain = new AIComponent();
		
		hitBox.setContactData(new SensorContact(hitBox, WBodyType.Weapon));
		
		super.add(hitBox);
		super.add(brain);
		
		speed = 1f;
	}    
	
	@Override
	public void update(float delta)
	{
		super.update(delta);
		
		brain.update(delta);
		
		stop();
		move(brain.calculateDirection(collision.getPosition()));
		
		hitBox.setPosition(collision.getPosition().x, collision.getPosition().y);
		
		if (hitBox.hasContact())
		{
            HitData hit = (HitData)hitBox.getContact();
			
			float knockback = hit.getKnockback() * 1000;
            
			switch(hit.getDirection())
			{
				case Up:
					collision.applyImpulse(0, -knockback);
					break;
				case Down:
					collision.applyImpulse(0, knockback);
					break;
				case Left:
					collision.applyImpulse(-knockback, 0);
					break;
				case Right:
					collision.applyImpulse(knockback, 0);
					break;
			}
            healthBar.damage(hit.getDamage());
		}
	}
	
	public void setAIMode(String mode)
	{
		switch (mode)
		{
			case "stay":
				brain.setAIMode(AIMode.Stay);
				break;
			case "follow":
				brain.setAIMode(AIMode.Follow);
				break;
			case "moveto":
				brain.setAIMode(AIMode.MoveTo);
				break;
		}
	}
}
