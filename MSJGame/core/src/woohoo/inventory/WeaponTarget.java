package woohoo.inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import woohoo.gameworld.components.ItemDataComponent;
import woohoo.gameworld.Mappers;
import woohoo.inventory.inventoryactions.DeselectAction;
import woohoo.inventory.inventoryactions.EquipWeaponAction;

public class WeaponTarget extends InventoryTarget
{
	public WeaponTarget(InventorySlot slot)
	{
		super(slot);
	}

	@Override
	public void drop(Source source, Payload payload, float x, float y, int pointer)
	{
		InventorySlot sourceSlot = (InventorySlot) source.getActor();
		ItemDataComponent itemData = Mappers.items.get(sourceSlot.getItem());

		if (itemData.type == ItemDataComponent.ItemType.Weapon)
		{
			actions.add(new EquipWeaponAction(sourceSlot.getItem()));
			
			super.drop(source, payload, x, y, pointer);
		} 
		else
		{
			sourceSlot.setDragged(false);
			actions.add(new DeselectAction());
		}
	}
}
