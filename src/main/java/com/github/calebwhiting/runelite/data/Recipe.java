package com.github.calebwhiting.runelite.data;

import com.github.calebwhiting.runelite.api.InventoryManager;
import lombok.Data;
import net.runelite.api.gameval.ItemID;

@Data
public class Recipe
{

	private final int productId;

	private final Ingredient[] requirements;

	private final Ingredient tool;

	/*
	 * Some actions are showing the ingredient as the final product. When this is set to true, the first ingredient will
	 * be the one used to register. 
	 */
	private final Boolean isSelectingIngredientAsProduct; 

	public Recipe(int productId, Ingredient[] requirements, Ingredient tool)
	{
		this.productId = productId;
		this.requirements = requirements;
		this.tool = tool;
		this.isSelectingIngredientAsProduct = false;
	}

	public Recipe(int productId, Ingredient... requirements)
	{
		this.productId = productId;
		this.requirements = requirements;
		this.tool = null;
		this.isSelectingIngredientAsProduct = false;
	}

	public Recipe(int productId, Boolean isSelectingIngredientAsProduct, Ingredient... requirements)
	{
		this.productId = productId;
		this.requirements = requirements;
		this.tool = null;
		this.isSelectingIngredientAsProduct = isSelectingIngredientAsProduct;
	}

	public static <T extends Recipe> T forProduct(T[] all, int productId, InventoryManager inventoryManager)
	{
		for (T v : all) {
			if (v.getIsSelectingIngredientAsProduct()) {
				for (Ingredient ingredient : v.getRequirements()){
					if(ingredient.getItemId() == productId && (v.getTool() == null || inventoryManager.getItems().anyMatch(item -> item.getId() == v.getTool().getItemId()))){
						return v;
					}
				}
			}
			else if (v.getProductId() == productId && (v.getTool() == null || inventoryManager.getItems().anyMatch(item -> item.getId() == v.getTool().getItemId()))) {
				return v;
			}
		}
		return null;
	}

	public int getMakeProductCount(InventoryManager inventoryManager)
	{
		int amount = Integer.MAX_VALUE;
		for (Ingredient requirement : this.getRequirements()) {
			if (inventoryManager.getItems().noneMatch(item -> item.getId() == requirement.getItemId())) {
				return 0;
			}

			if (requirement.isConsumed()) {
				if (this.getTool() != null && !inventoryManager.getItems().anyMatch(item -> item.getId() == tool.getItemId())){
					return 0;
				}
				amount = Math.min(
						amount,
						this.getTool() != null
						? getMakeProductCountWithTool(inventoryManager.getItemCountById(requirement.getItemId()) / requirement.getAmount())
						: inventoryManager.getItemCountById(requirement.getItemId()) / requirement.getAmount()
				);
			}
		}
		return amount;
	}

	private int getMakeProductCountWithTool(int amount){
		int toolAmount = Integer.MAX_VALUE;
		if (productId == ItemID.MCANNONBALL) {
			if (tool != null && tool.getItemId() == ItemID.DOUBLE_AMMO_MOULD) {
				//Round up since the double ammo mould is able to smelt with only one silver bar
				toolAmount = (int) Math.ceil((double) amount / 2);
			} else {
				toolAmount = amount;
			}
		} else {
			toolAmount = amount;
		}
		return toolAmount;
	}

}
