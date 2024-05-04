package com.github.calebwhiting.runelite.plugins.actionprogress.detect;

import com.github.calebwhiting.runelite.api.InventoryManager;
import com.github.calebwhiting.runelite.data.Ingredient;
import com.github.calebwhiting.runelite.data.Magic;
import com.github.calebwhiting.runelite.plugins.actionprogress.Product;
import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import java.util.stream.IntStream;
import static com.github.calebwhiting.runelite.plugins.actionprogress.Action.*;
import static net.runelite.api.ItemID.*;

public class UseItemOnItemDetector extends ActionDetector
{

	private static final Ingredient PESTLE_AND_MORTAR = new Ingredient(ItemID.PESTLE_AND_MORTAR, 1, false);
	private static final Ingredient CHISEL = new Ingredient(ItemID.CHISEL, 1, false);
	private static final String BREAK_DOWN = "Break-down";

	private static final Product[] PRODUCTS = {
			new Product(GRIND, 				GROUND_ASHES, 					new Ingredient[]{ new Ingredient(ASHES)}, 														PESTLE_AND_MORTAR),
			new Product(GRIND, 				CRUSHED_NEST, 					new Ingredient[]{ new Ingredient(BIRD_NEST_5075)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				DRAGON_SCALE_DUST, 				new Ingredient[]{ new Ingredient(BLUE_DRAGON_SCALE)}, 											PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_CHARCOAL, 				new Ingredient[]{ new Ingredient(CHARCOAL)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				CHOCOLATE_DUST, 				new Ingredient[]{ new Ingredient(CHOCOLATE_BAR)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_CRAB_MEAT, 				new Ingredient[]{ new Ingredient(CRAB_MEAT)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				GOAT_HORN_DUST, 				new Ingredient[]{ new Ingredient(DESERT_GOAT_HORN)}, 											PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_THISTLE, 				new Ingredient[]{ new Ingredient(DRIED_THISTLE)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				GORAK_CLAW_POWDER, 				new Ingredient[]{ new Ingredient(GORAK_CLAWS)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_GUAM, 					new Ingredient[]{ new Ingredient(GUAM_LEAF)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				KEBBIT_TEETH_DUST, 				new Ingredient[]{ new Ingredient(KEBBIT_TEETH)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_KELP, 					new Ingredient[]{ new Ingredient(KELP)}, 														PESTLE_AND_MORTAR),
			new Product(GRIND, 				LAVA_SCALE_SHARD, 				new Ingredient[]{ new Ingredient(LAVA_SCALE)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_MUD_RUNES,				new Ingredient[]{ new Ingredient(MUD_RUNE)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				MYSTERIOUS_CRUSHED_MEAT, 		new Ingredient[]{ new Ingredient(MYSTERIOUS_MEAT)}, 											PESTLE_AND_MORTAR),
			new Product(GRIND, 				NIHIL_DUST, 					new Ingredient[]{ new Ingredient(NIHIL_SHARD)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				KARAMBWAN_PASTE, 				new Ingredient[]{ new Ingredient(POISON_KARAMBWAN)}, 											PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_COD, 					new Ingredient[]{ new Ingredient(RAW_COD)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				RUNE_DUST, 						new Ingredient[]{ new Ingredient(RUNE_SHARDS)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_SEAWEED, 				new Ingredient[]{ new Ingredient(SEAWEED)}, 													PESTLE_AND_MORTAR),
			new Product(GRIND, 				GROUND_TOOTH, 					new Ingredient[]{ new Ingredient(SUQAH_TOOTH)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				UNICORN_HORN_DUST, 				new Ingredient[]{ new Ingredient(UNICORN_HORN)}, 												PESTLE_AND_MORTAR),
			new Product(GRIND, 				CRUSHED_SUPERIOR_DRAGON_BONES, 	new Ingredient[]{ new Ingredient(SUPERIOR_DRAGON_BONES)}, 										PESTLE_AND_MORTAR),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONES)}, 												CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BAT_BONES)}, 											CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BIG_BONES)}, 											CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_ZOGRE_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BABYWYRM_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BABYDRAGON_BONES)}, 									CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_WYRM_BONES)}, 											CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(SUNKISSED_BONES)}, 											CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_WYVERN_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_DRAGON_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_DRAKE_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_FAYRG_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_LAVA_DRAGON_BONES)}, 									CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_RAURG_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_HYDRA_BONES)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(DAGANNOTH_BONES_29376)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_OURG_BONES)}, 											CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_SUPERIOR_DRAGON_BONES)}, 								CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONE_STATUETTE)}, 										CHISEL),
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONE_STATUETTE_29340)}, 								CHISEL), //Might not be required. Not sure what the difference is
			new Product(GRIND_BONE_SHARDS, 	BLESSED_BONE_SHARDS, 			new Ingredient[]{ new Ingredient(BLESSED_BONE_STATUETTE_29342)}, 								CHISEL), //Might not be required. Not sure what the difference is
			new Product(SUNFIRE_WINE, 		JUG_OF_SUNFIRE_WINE,			new Ingredient[]{ new Ingredient(JUG_OF_WINE), new Ingredient(SUNFIRE_SPLINTERS, 2)}, 	PESTLE_AND_MORTAR)
	};

	@Inject private InventoryManager inventoryManager;

	@Inject private Client client;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if(evt.getMenuOption().equals(BREAK_DOWN)){
			for (Product product : PRODUCTS) {
				if(product.IngredientsIsIncludedIn(evt.getMenuTarget(), client)){
					int amount = product.getMakeProductCount(this.inventoryManager);
					if (amount > 0) {
						this.actionManager.setAction(product.getAction(), amount, product.getProductId());
					}
				}
			}
		}
		if (evt.getMenuAction() != MenuAction.WIDGET_TARGET_ON_WIDGET) {
			return;
		}
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		Widget widget = this.client.getSelectedWidget();
		if (inventory == null|| widget == null) {
			return;
		}
		Item[] items = IntStream.of(widget.getId(), evt.getParam0())
								.mapToObj(inventory::getItem)
								.filter(n -> n!= null)
								.toArray(Item[]::new);
		outerloop:
		for (Product product : PRODUCTS) {
			//Not clean, but prevents the action bar from showing if an item is used on the same item, or double-clicked
			Ingredient[] ingredients = product.getRequirements();
			if(ingredients.length > 1) {
				for (Ingredient ingredient : ingredients) {
					ItemComposition itemName = client.getItemDefinition(ingredient.getItemId());
					String[] evtSourceTarget = evt.getMenuTarget().split("->");
					if (evtSourceTarget[0].toLowerCase().contains(itemName.getName().toLowerCase()) && evtSourceTarget[1].toLowerCase().contains(itemName.getName().toLowerCase())){
						continue outerloop;
					}
				}
			}

			if (product.isMadeWith(items)) {
				int amount = product.getMakeProductCount(this.inventoryManager);
				if (amount > 0) {
					this.actionManager.setAction(product.getAction(), amount, product.getProductId());
				}
			}
		}
	}

}
