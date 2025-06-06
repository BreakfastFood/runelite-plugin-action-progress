package com.github.calebwhiting.runelite.data;

import net.runelite.api.gameval.ItemID;

public interface Fletching
{
	int[] UNENCHANTED_BOLTS_AND_ARROWS = {
			ItemID.BOLT, ItemID.BRONZE_ARROW, ItemID.IRON_ARROW, ItemID.STEEL_ARROW, ItemID.MITHRIL_ARROW,
			ItemID.ADAMANT_ARROW, ItemID.RUNE_ARROW, ItemID.OGRE_ARROW, ItemID.SLAYER_BROAD_ARROWS,
			ItemID.ZOGRE_BRUTAL_BRONZE, ItemID.ZOGRE_BRUTAL_IRON, ItemID.ZOGRE_BRUTAL_STEEL, ItemID.ZOGRE_BRUTAL_BLACK, ItemID.ZOGRE_BRUTAL_MITHRIL,
			ItemID.ZOGRE_BRUTAL_ADAMANT, ItemID.ZOGRE_BRUTAL_RUNE, ItemID.XBOWS_CROSSBOW_BLURITE, ItemID.XBOWS_CROSSBOW_BOLTS_IRON,
			ItemID.XBOWS_CROSSBOW_BOLTS_STEEL,ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL, ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE, ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE, ItemID.XBOWS_CROSSBOW_BOLTS_SILVER,
			ItemID.DRAGON_ARROW, ItemID.SLAYER_BROAD_BOLT, ItemID.DRAGON_BOLTS, ItemID.BARBED_BOLT
	};
	int[] BOLT_TIPS = {
			ItemID.OPAL_BOLTTIPS, ItemID.PEARL_BOLTTIPS, ItemID.XBOWS_BOLT_TIPS_JADE, ItemID.XBOWS_BOLT_TIPS_REDTOPAZ,
			ItemID.XBOWS_BOLT_TIPS_SAPPHIRE, ItemID.XBOWS_BOLT_TIPS_EMERALD, ItemID.XBOWS_BOLT_TIPS_RUBY, ItemID.XBOWS_BOLT_TIPS_DIAMOND,
			ItemID.XBOWS_BOLT_TIPS_DRAGONSTONE, ItemID.XBOWS_BOLT_TIPS_ONYX
	};

	int[] JAVELINS = {
			ItemID.BRONZE_JAVELIN, ItemID.IRON_JAVELIN, ItemID.STEEL_JAVELIN, ItemID.MITHRIL_JAVELIN, 
			ItemID.ADAMANT_JAVELIN, ItemID.RUNE_JAVELIN, ItemID.AMETHYST_JAVELIN, ItemID.DRAGON_JAVELIN,
	};
}
