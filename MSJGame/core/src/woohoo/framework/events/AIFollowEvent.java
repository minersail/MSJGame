package woohoo.framework.events;

import com.badlogic.ashley.core.Entity;
import woohoo.ai.aipatterns.ChasePattern;
import woohoo.gameworld.components.AIComponent;
import woohoo.gameworld.components.PositionComponent;

public class AIFollowEvent implements Event<Entity>
{
	private AIComponent aiChar; // AIComponent of the following entity
	private PositionComponent followChar; // PositionComponent of the followed entity
	
	public AIFollowEvent(AIComponent character, PositionComponent follow)
	{
		aiChar = character;
		followChar = follow;
	}
	
	@Override
	public void activate()
	{
		aiChar.setPattern(new ChasePattern(followChar));
	}	
}
