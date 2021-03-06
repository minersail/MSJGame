package woohoo.framework.loading;

import woohoo.gameworld.components.InputComponent;
import woohoo.gameworld.components.AnimMapObjectComponent;
import woohoo.gameworld.components.InventoryComponent;
import woohoo.gameworld.components.OpacityComponent;
import woohoo.gameworld.components.DialogueComponent;
import woohoo.gameworld.components.HealthBarComponent;
import woohoo.gameworld.components.SpawnComponent;
import woohoo.gameworld.components.IDComponent;
import woohoo.gameworld.components.HealthComponent;
import woohoo.gameworld.components.EventListenerComponent;
import woohoo.gameworld.components.HitboxComponent;
import woohoo.gameworld.components.MapObjectComponent;
import woohoo.gameworld.components.LOSComponent;
import woohoo.gameworld.components.MovementComponent;
import woohoo.gameworld.components.ContactComponent;
import woohoo.gameworld.components.AIComponent;
import woohoo.gameworld.components.WeaponComponent;
import woohoo.gameworld.components.PlayerComponent;
import woohoo.gameworld.components.ProjectileComponent;
import woohoo.gameworld.components.PositionComponent;
import woohoo.gameworld.components.ChaseComponent;
import woohoo.gameworld.components.ItemDataComponent;
import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import woohoo.framework.contactcommands.ContactData;
import woohoo.gameworld.components.ContactComponent.ContactType;
import woohoo.gameworld.components.ContactComponent.Faction;
import woohoo.gameworld.AIStateSystem;
import woohoo.gameworld.Mappers;
import woohoo.gameworld.RenderSystem;
import woohoo.screens.PlayingScreen;

public class EntityLoader
{
	PlayingScreen screen;
	
	public EntityLoader(PlayingScreen scr)
	{
		screen = scr;
		
		screen.getEngine().addEntityListener(Family.one(HitboxComponent.class, LOSComponent.class).get(), new ContactDataListener());
		screen.getEngine().addEntityListener(Family.one(MapObjectComponent.class, AnimMapObjectComponent.class, HealthBarComponent.class).get(), new MapObjectListener());
		screen.getEngine().addEntityListener(Family.all(ChaseComponent.class).get(), new ChaseListener());
		screen.getEngine().addEntityListener(Family.all(InventoryComponent.class).get(), new InventoryListener());
		screen.getEngine().addEntityListener(Family.all(AIComponent.class).get(), new AIListener());
		screen.getEngine().addEntityListener(Family.all(PositionComponent.class, HitboxComponent.class).get(), new PositionListener());
	}	
	
	public void loadPlayer()
	{
		FileHandle handle = Gdx.files.local("data/player.xml");
        
        XmlReader xml = new XmlReader();
        Element playerEl = xml.parse(handle.readString());   
		
		Entity player = loadEntity(new EntityMold(playerEl));
		
		screen.getInventoryManager().fillPlayerInventory(Mappers.inventories.get(player));
	}
	
	public void loadEntities(int area)
	{
		FileHandle handle = Gdx.files.local("data/entities.xml");
        
        XmlReader xml = new XmlReader();
        Element root = xml.parse(handle.readString());       
        Element entities = root.getChild(area);         
        
        for (Element e : entities.getChildrenByName("entity"))
        {
			if (!e.getBoolean("enabled")) continue;
			
			loadEntity(new EntityMold(e)); // Load entity
		}	
	}
	
	/**
	 * Loads an entity from a mold and adds it to the game engine
	 * @param mold the entity mold to create the entity from
	 * @return the entity created
	 */
	public Entity loadEntity(EntityMold mold)
	{
		Entity entity = new Entity();

		for (Element component : mold.getData())
		{
			loadComponent(entity, component);
		}
		screen.getEngine().addEntity(entity);
		
		return entity;
	}
	
	public void loadComponent(Entity entity, Element component)
	{
		Component base;
		
		switch (component.getName())
		{
			case "ai":
				if (component.get("state").equals("stay"))
					base = new AIComponent("stay");
				else if (component.get("state").equals("wander"))
					base = new AIComponent("wander");
				else if (component.get("state").equals("boss"))
					base = new AIComponent("boss");
				else if (component.get("state").equals("attackchase"))
					base = new AIComponent("attackchase", component.get("target"));
				else if (component.get("state").equals("chase"))
					base = new AIComponent("chase", component.get("target"));
				else if (component.get("state").equals("moveto"))
					base = new AIComponent(new Vector2(component.getFloat("targetX"), component.getFloat("targetY")));
				else if (component.get("state").equals("push"))
					base = new AIComponent(component.get("pushed"), new Vector2(component.getFloat("targetX"), component.getFloat("targetY")));
				else if (component.get("state").equals("sentry"))
				{
					Array<Vector2> patrol = new Array<>();
					for (Element patrolLoc : component.getChildrenByName("patrol"))
						patrol.add(new Vector2(patrolLoc.getFloat("x"), patrolLoc.getFloat("y")));
					
					base = new AIComponent(patrol);
				}
				else
                {
					base = new AIComponent();
                    Gdx.app.error("ERROR", "AIComponent pattern incorrect.");
                }
				break;
			case "anim":
				base = new AnimMapObjectComponent(screen.getAssets().get("images/entities/" + component.get("atlas"), TextureAtlas.class));
				break;
			case "chase":
				base = new ChaseComponent(screen.getAssets().get("ui/chasebar.pack", TextureAtlas.class));
				break;
			case "contact":
				base = new ContactComponent();
				break;
			case "dialogue":
				base = new DialogueComponent(component.getInt("id"), false);
				break;
			case "eventlistener":
				base = new EventListenerComponent();
				break;
			case "healthbar":
				base = new HealthBarComponent(screen.getAssets().get("ui/healthbar.pack", TextureAtlas.class));
				break;
			case "health":
				base = new HealthComponent(component.getInt("max"));
				break;
			case "hitbox":
				base = new HitboxComponent(screen.getWorld(), new HitboxMold(component));
				break;
			case "id":
				base = new IDComponent(component.get("name"));
				break;
			case "input":
				base = new InputComponent();
				break;
			case "inventory":
				base = new InventoryComponent(component.getInt("id"));
				break;
			case "itemdata":
				base = new ItemDataComponent(component.getInt("id"), component.getChildByName("metadata").getAttributes(), 
											 screen.getIDManager().getItem(component.getInt("id")).toObjectMap());
				break;
			case "lineofsight":
				base = new LOSComponent(screen.getWorld());
				break;
			case "mapobject":
				base = new MapObjectComponent(new TextureRegion(screen.getAssets().get("images/entities/" + component.get("texture"), Texture.class)));
				break;
			case "movement":
				base = new MovementComponent(component.getFloat("speed"));
				break;
			case "opacity":
				base = new OpacityComponent();
				break;
			case "player":
				base = new PlayerComponent();
				break;
			case "position":
				base = new PositionComponent(component.getFloat("x"), component.getFloat("y"));
				break;
			case "projectile":
				base = new ProjectileComponent(component.getFloat("damage"), component.getFloat("knockback", 1), component.getFloat("lifetime", 1));
				break;
			case "spawn":
				base = new SpawnComponent(component.get("entity"), component.getFloat("time", 1.0f));
				break;
			case "weapon":
				base = new WeaponComponent(component.getInt("projectileid"), component.getFloat("cooldown"));
				break;
			default:
				throw new IllegalArgumentException("No component with name " + component.getName() + " found.");
		}
		
		entity.add(base);
	}
	
	public class ContactDataListener implements EntityListener
	{
		@Override
		public void entityAdded(Entity entity)
		{
			if (Mappers.hitboxes.has(entity))
			{
				HitboxComponent hitbox = Mappers.hitboxes.get(entity);
				hitbox.mass.setUserData(new ContactData(hitbox.hitboxType, hitbox.faction, entity));
			}

			if (Mappers.sightLines.has(entity))
			{
				LOSComponent los = Mappers.sightLines.get(entity);
				los.mass.setUserData(new ContactData(ContactType.SightLine, Faction.Neutral, entity));
			}
		}

		@Override
		public void entityRemoved(Entity entity)
		{
		}
	}
	
	public class MapObjectListener implements EntityListener
	{
		@Override
		public void entityAdded(Entity entity) 
		{
			if (Mappers.mapObjects.has(entity))
			{
				screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get(getLayerString(entity)).getObjects().add(Mappers.mapObjects.get(entity));
			}
			
			if (Mappers.animMapObjects.has(entity))
			{
				screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get(getLayerString(entity)).getObjects().add(Mappers.animMapObjects.get(entity));
			}
			
			if (Mappers.healthBars.has(entity))
			{
				screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get(getLayerString(entity)).getObjects().add(Mappers.healthBars.get(entity));
			}
			
			if (Mappers.chasers.has(entity))
			{
				screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get(getLayerString(entity)).getObjects().add(Mappers.chasers.get(entity));
			}
		}

		@Override
		public void entityRemoved(Entity entity) 
		{
//			System.out.println(Mappers.mapObjects.has(entity));
//			if (Mappers.mapObjects.has(entity))
//			{
//				screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get(getLayerString(entity)).getObjects().remove(Mappers.mapObjects.get(entity));
//			}
//			else if (Mappers.animMapObjects.has(entity))
//			{
//				screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get(getLayerString(entity)).getObjects().remove(Mappers.animMapObjects.get(entity));
//			}
		}		
		
		private String getLayerString(Entity entity)
		{
			if (Mappers.items.has(entity))
				return "Items";
			else
				return "Entities";
		}
	}
	
	public class ChaseListener implements EntityListener
	{
		@Override
		public void entityAdded(Entity entity) 
		{
			screen.getEngine().getSystem(RenderSystem.class).getRenderer().getMap().getLayers().get("Entities").getObjects().add(Mappers.chasers.get(entity));
		}

		@Override
		public void entityRemoved(Entity entity) 
		{
		}		
	}
	
	public class InventoryListener implements EntityListener
	{
		@Override
		public void entityAdded(Entity entity)
		{
			screen.getInventoryManager().loadFromXML(Mappers.inventories.get(entity));
		}
		
		@Override
		public void entityRemoved(Entity entity)
		{
			
		}
	}
	
	public class AIListener implements EntityListener
	{
		@Override
		public void entityAdded(Entity entity)
		{
			screen.getEngine().getSystem(AIStateSystem.class).initialize(entity, screen.currentArea);
		}
		
		@Override
		public void entityRemoved(Entity entity)
		{
			if (Mappers.movements.has(entity))
				Mappers.movements.get(entity).direction = MovementComponent.Direction.None;
		}
	}
	
	public class PositionListener implements EntityListener
	{
		@Override
		public void entityAdded(Entity entity)
		{			
			Mappers.hitboxes.get(entity).mass.setTransform(Mappers.positions.get(entity).position.cpy().add(0.5f, 0.5f), 0);
		}

		@Override
		public void entityRemoved(Entity entity)
		{
		}		
	}
}
