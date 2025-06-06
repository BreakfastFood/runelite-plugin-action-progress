package com.github.calebwhiting.runelite.plugins.actionprogress.detect;

import com.github.calebwhiting.runelite.api.InventoryManager;
import com.github.calebwhiting.runelite.data.*;
import com.github.calebwhiting.runelite.plugins.actionprogress.Action;
import com.github.calebwhiting.runelite.plugins.actionprogress.ActionUtils;
import com.github.calebwhiting.runelite.plugins.actionprogress.Product;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.ScriptEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

import java.util.Arrays;
import java.util.Objects;

import static com.github.calebwhiting.runelite.plugins.actionprogress.Action.*;

/**
 * Detects actions initiated from the chatbox crafting interface (Eg: Fletching, Glassblowing, Leather-work)
 */
@Slf4j
@Singleton
public class ChatboxDetector extends ActionDetector
{

	/**
	 * Indicates how many items are to be created in the crafting dialogue.
	 */
	private static final int VAR_MAKE_AMOUNT = 200;

	/**
	 * Indicates the selected product in the crafting dialogue.
	 */
	private static final int VAR_SELECTED_INDEX = 2673;

	private static final int WIDGET_MAKE_PARENT = 270;

	private static final int WIDGET_MAKE_QUESTION = 5;
	private static final int WIDGET_MAKE_SLOT_START = 14;
	private static final int WIDGET_MAKE_SLOT_COUNT = 9;
	private static final int WIDGET_MAKE_SLOT_ITEM = 38;

	private static final int WIDGET_ID_CHATBOX_FIRST_MAKE_BUTTON = 17694734;

	private static final int MAKE_X_SETUP = 2046;
	private static final int MAKE_X_BUTTON_CLICK = 2050;
	private static final int MAKE_X_BUTTON_KEY = 2051;
	private static final int MAKE_X_BUTTON_TRIGGERED = 2052;

	private static final Product[] MULTI_MATERIAL_PRODUCTS = {
			// @formatter:off
            new Product(CRAFT_LEATHER, ItemID.DRAGONHIDE_BODY, new Ingredient(ItemID.DRAGON_LEATHER, 3)),
            new Product(CRAFT_LEATHER, ItemID.DRAGONHIDE_CHAPS, new Ingredient(ItemID.DRAGON_LEATHER, 2)),
            new Product(CRAFT_LEATHER, ItemID.BLUE_DRAGONHIDE_BODY, new Ingredient(ItemID.DRAGON_LEATHER_BLUE, 3)),
            new Product(CRAFT_LEATHER, ItemID.BLUE_DRAGONHIDE_CHAPS, new Ingredient(ItemID.DRAGON_LEATHER_BLUE, 2)),
            new Product(CRAFT_LEATHER, ItemID.RED_DRAGONHIDE_BODY, new Ingredient(ItemID.DRAGON_LEATHER_RED, 3)),
            new Product(CRAFT_LEATHER, ItemID.RED_DRAGONHIDE_CHAPS, new Ingredient(ItemID.DRAGON_LEATHER_RED, 2)),
            new Product(CRAFT_LEATHER, ItemID.BLACK_DRAGONHIDE_BODY, new Ingredient(ItemID.DRAGON_LEATHER_BLACK, 3)),
            new Product(CRAFT_LEATHER, ItemID.BLACK_DRAGONHIDE_CHAPS, new Ingredient(ItemID.DRAGON_LEATHER_BLACK, 2)),
            new Product(CRAFT_LEATHER, ItemID.SNAKESKIN_BANDANA, new Ingredient(ItemID.VILLAGE_SNAKE_SKIN, 5)),
            new Product(CRAFT_LEATHER, ItemID.SNAKESKIN_BODY, new Ingredient(ItemID.VILLAGE_SNAKE_SKIN, 15)),
            new Product(CRAFT_LEATHER, ItemID.SNAKESKIN_BOOTS, new Ingredient(ItemID.VILLAGE_SNAKE_SKIN, 6)),
            new Product(CRAFT_LEATHER, ItemID.SNAKESKIN_CHAPS, new Ingredient(ItemID.VILLAGE_SNAKE_SKIN, 12)),
            new Product(CRAFT_LEATHER, ItemID.SNAKESKIN_VAMBRACES, new Ingredient(ItemID.VILLAGE_SNAKE_SKIN, 8)),
            new Product(CRAFT_LEATHER, ItemID.XERIC_HAT, new Ingredient(ItemID.XERIC_FABRIC, 3)),
            new Product(CRAFT_LEATHER, ItemID.XERIC_TOP, new Ingredient(ItemID.XERIC_FABRIC, 5)),
            new Product(CRAFT_LEATHER, ItemID.XERIC_ROBE, new Ingredient(ItemID.XERIC_FABRIC, 4)),
            new Product(CRAFT_LEATHER, ItemID.XERIC_ROBE, new Ingredient(ItemID.XERIC_FABRIC, 4)),
            new Product(CRAFT_LEATHER, ItemID.LEATHER_GLOVES, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_LEATHER, ItemID.LEATHER_BOOTS, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_LEATHER, ItemID.LEATHER_COWL, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_LEATHER, ItemID.LEATHER_VAMBRACES, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_LEATHER, ItemID.LEATHER_ARMOUR, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_LEATHER, ItemID.LEATHER_CHAPS, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_LEATHER, ItemID.COIF, new Ingredient(ItemID.LEATHER)),
            new Product(CRAFT_HARD_LEATHER, ItemID.HARDLEATHER_BODY, new Ingredient(ItemID.HARD_LEATHER, 1)),
            new Product(CRAFT_BATTLESTAVES, ItemID.AIR_BATTLESTAFF, new Ingredient(ItemID.AIR_ORB), new Ingredient(ItemID.BATTLESTAFF)),
            new Product(CRAFT_BATTLESTAVES, ItemID.FIRE_BATTLESTAFF, new Ingredient(ItemID.FIRE_ORB), new Ingredient(ItemID.BATTLESTAFF)),
            new Product(CRAFT_BATTLESTAVES, ItemID.EARTH_BATTLESTAFF, new Ingredient(ItemID.EARTH_ORB), new Ingredient(ItemID.BATTLESTAFF)),
            new Product(CRAFT_BATTLESTAVES, ItemID.WATER_BATTLESTAFF, new Ingredient(ItemID.WATER_ORB), new Ingredient(ItemID.BATTLESTAFF)),
            new Product(SMELTING, ItemID.BRONZE_BAR, new Ingredient(ItemID.TIN_ORE), new Ingredient(ItemID.COPPER_ORE)),
            new Product(SMELTING, ItemID.IRON_BAR, new Ingredient(ItemID.IRON_ORE)),
            new Product(SMELTING, ItemID.SILVER_BAR, new Ingredient(ItemID.SILVER_ORE)),
            new Product(SMELTING, ItemID.STEEL_BAR, new Ingredient(ItemID.IRON_ORE), new Ingredient(ItemID.COAL, 2)),
            new Product(SMELTING, ItemID.GOLD_BAR, new Ingredient(ItemID.GOLD_ORE)),
            new Product(SMELTING, ItemID.MITHRIL_BAR, new Ingredient(ItemID.MITHRIL_ORE), new Ingredient(ItemID.COAL, 4)),
            new Product(SMELTING, ItemID.ADAMANTITE_BAR, new Ingredient(ItemID.ADAMANTITE_ORE), new Ingredient(ItemID.COAL, 6)),
            new Product(SMELTING, ItemID.RUNITE_BAR, new Ingredient(ItemID.RUNITE_ORE), new Ingredient(ItemID.COAL, 8)),
			new Product(SMELTING_CANNONBALLS, ItemID.MCANNONBALL, new Ingredient[]{new Ingredient(ItemID.STEEL_BAR)}, new Ingredient(ItemID.AMMO_MOULD)),
			new Product(SMELTING_CANNONBALLS, ItemID.MCANNONBALL, new Ingredient[]{new Ingredient(ItemID.STEEL_BAR)}, new Ingredient(ItemID.DOUBLE_AMMO_MOULD)),
            new Product(CRAFT_CUT_GEMS, ItemID.OPAL, true, new Ingredient(ItemID.UNCUT_OPAL)),
            new Product(CRAFT_CUT_GEMS, ItemID.JADE, true, new Ingredient(ItemID.UNCUT_JADE)),
            new Product(CRAFT_CUT_GEMS, ItemID.RED_TOPAZ, true, new Ingredient(ItemID.UNCUT_RED_TOPAZ)),
            new Product(CRAFT_CUT_GEMS, ItemID.SAPPHIRE, true, new Ingredient(ItemID.UNCUT_SAPPHIRE)),
            new Product(CRAFT_CUT_GEMS, ItemID.EMERALD, true, new Ingredient(ItemID.UNCUT_EMERALD)),
            new Product(CRAFT_CUT_GEMS, ItemID.RUBY, true, new Ingredient(ItemID.UNCUT_RUBY)),
            new Product(CRAFT_CUT_GEMS, ItemID.DIAMOND, true, new Ingredient(ItemID.UNCUT_DIAMOND)),
            new Product(CRAFT_CUT_GEMS, ItemID.DRAGONSTONE, true, new Ingredient(ItemID.UNCUT_DRAGONSTONE)),
            new Product(CRAFT_CUT_GEMS, ItemID.ONYX, true, new Ingredient(ItemID.UNCUT_ONYX)),
            new Product(CRAFT_CUT_GEMS, ItemID.ZENYTE, true, new Ingredient(ItemID.UNCUT_ZENYTE)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.HUNTING_STRUNG_RABBIT_FOOT, new Ingredient(ItemID.HUNTING_RABBIT_FOOT), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRINGSTAR, new Ingredient(ItemID.NOSTRINGSTAR), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRINGSNAKE, new Ingredient(ItemID.NOSTRINGSNAKE), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_OPAL_AMULET, new Ingredient(ItemID.UNSTRUNG_OPAL_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_JADE_AMULET, new Ingredient(ItemID.UNSTRUNG_JADE_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_SAPPHIRE_AMULET, new Ingredient(ItemID.UNSTRUNG_SAPPHIRE_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_TOPAZ_AMULET, new Ingredient(ItemID.UNSTRUNG_TOPAZ_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_EMERALD_AMULET, new Ingredient(ItemID.UNSTRUNG_EMERALD_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_RUBY_AMULET, new Ingredient(ItemID.UNSTRUNG_RUBY_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_GOLD_AMULET, new Ingredient(ItemID.UNSTRUNG_GOLD_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_DIAMOND_AMULET, new Ingredient(ItemID.UNSTRUNG_DIAMOND_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_DRAGONSTONE_AMULET, new Ingredient(ItemID.UNSTRUNG_DRAGONSTONE_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.STRUNG_ONYX_AMULET, new Ingredient(ItemID.UNSTRUNG_ONYX_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_STRING_JEWELLERY, ItemID.ZENYTE_AMULET, new Ingredient(ItemID.UNSTRUNG_ZENYTE_AMULET), new Ingredient(ItemID.BALL_OF_WOOL)),
            new Product(CRAFT_MOLTEN_GLASS, ItemID.MOLTEN_GLASS, new Ingredient(ItemID.BUCKET_SAND), new Ingredient(ItemID.SODA_ASH)),
            new Product(CRAFT_BLOW_GLASS, ItemID.BEER_GLASS, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.CANDLE_LANTERN_EMPTY, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.OIL_LAMP_EMPTY, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.VIAL_EMPTY, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.FISHBOWL_EMPTY, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.STAFFORB, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.LENS, new Ingredient(ItemID.MOLTEN_GLASS)),
            new Product(CRAFT_BLOW_GLASS, ItemID.DORGESH_LIGHTBULB_NOFILAMENT, new Ingredient(ItemID.MOLTEN_GLASS)),
			new Product(CRAFT_LOOM, ItemID.BASKET_EMPTY, new Ingredient(ItemID.WILLOW_BRANCH, 6)),
			new Product(CRAFT_LOOM, ItemID.SACK_EMPTY, new Ingredient(ItemID.JUTE_FIBRE, 4)),
			new Product(CRAFT_LOOM_DRIFT_NET, ItemID.FOSSIL_DRIFT_NET, new Ingredient(ItemID.JUTE_FIBRE, 2)),
			new Product(CRAFT_LOOM, ItemID.REGICIDE_CLOTH, new Ingredient(ItemID.BALL_OF_WOOL, 4)),
			new Product(CRAFT_SHIELD, ItemID.LEATHER_SHIELD, new Ingredient(ItemID.DRAGON_LEATHER, 2), new Ingredient (ItemID.MAPLE_SHIELD), new Ingredient(ItemID.NAILS_BRONZE, 15)),
			new Product(CRAFT_SHIELD, ItemID.SNAKESKIN_SHIELD, new Ingredient(ItemID.VILLAGE_SNAKE_SKIN, 2), new Ingredient (ItemID.WILLOW_SHIELD), new Ingredient(ItemID.NAILS_IRON, 15)),
			new Product(CRAFT_SHIELD, ItemID.GREEN_DHIDE_SHIELD, new Ingredient(ItemID.DRAGON_LEATHER, 2), new Ingredient (ItemID.MAPLE_SHIELD), new Ingredient(ItemID.NAILS, 15)),
			new Product(CRAFT_SHIELD, ItemID.BLUE_DHIDE_SHIELD, new Ingredient(ItemID.DRAGON_LEATHER_BLUE, 2), new Ingredient (ItemID.YEW_SHIELD), new Ingredient(ItemID.NAILS_MITHRIL, 15)),
			new Product(CRAFT_SHIELD, ItemID.RED_DHIDE_SHIELD, new Ingredient(ItemID.DRAGON_LEATHER_RED, 2), new Ingredient (ItemID.MAGIC_SHIELD), new Ingredient(ItemID.NAILS_ADAMANT, 15)),
			new Product(CRAFT_SHIELD, ItemID.BLACK_DHIDE_SHIELD, new Ingredient(ItemID.DRAGON_LEATHER_BLACK, 2), new Ingredient (ItemID.REDWOOD_SHIELD), new Ingredient(ItemID.NAILS_RUNE, 15)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_LONGBOW, new Ingredient(ItemID.LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_OAK_LONGBOW, new Ingredient(ItemID.OAK_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_WILLOW_LONGBOW, new Ingredient(ItemID.WILLOW_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_MAPLE_LONGBOW, new Ingredient(ItemID.MAPLE_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_YEW_LONGBOW, new Ingredient(ItemID.YEW_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_MAGIC_LONGBOW, new Ingredient(ItemID.MAGIC_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_SHORTBOW, new Ingredient(ItemID.LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_OAK_SHORTBOW, new Ingredient(ItemID.OAK_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_WILLOW_SHORTBOW, new Ingredient(ItemID.WILLOW_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_MAPLE_SHORTBOW, new Ingredient(ItemID.MAPLE_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_YEW_SHORTBOW, new Ingredient(ItemID.YEW_LOGS)),
            new Product(FLETCH_CUT_BOW, ItemID.UNSTRUNG_MAGIC_SHORTBOW, new Ingredient(ItemID.MAGIC_LOGS)),
            new Product(FLETCH_STRING_BOW, ItemID.LONGBOW, new Ingredient(ItemID.UNSTRUNG_LONGBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.OAK_LONGBOW, new Ingredient(ItemID.UNSTRUNG_OAK_LONGBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.WILLOW_LONGBOW, new Ingredient(ItemID.UNSTRUNG_WILLOW_LONGBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.MAPLE_LONGBOW, new Ingredient(ItemID.UNSTRUNG_MAPLE_LONGBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.YEW_LONGBOW, new Ingredient(ItemID.UNSTRUNG_YEW_LONGBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.MAGIC_LONGBOW, new Ingredient(ItemID.UNSTRUNG_MAGIC_LONGBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.SHORTBOW, new Ingredient(ItemID.UNSTRUNG_SHORTBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.OAK_SHORTBOW, new Ingredient(ItemID.UNSTRUNG_OAK_SHORTBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.WILLOW_SHORTBOW, new Ingredient(ItemID.UNSTRUNG_WILLOW_SHORTBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.MAPLE_SHORTBOW, new Ingredient(ItemID.UNSTRUNG_MAPLE_SHORTBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.YEW_SHORTBOW, new Ingredient(ItemID.UNSTRUNG_YEW_SHORTBOW), new Ingredient(ItemID.BOW_STRING)),
            new Product(FLETCH_STRING_BOW, ItemID.MAGIC_SHORTBOW, new Ingredient(ItemID.UNSTRUNG_MAGIC_SHORTBOW), new Ingredient(ItemID.BOW_STRING)),
			new Product(FLETCH_SPINNING, ItemID.BOW_STRING, new Ingredient(ItemID.FLAX)),
			new Product(FLETCH_SHIELD, ItemID.OAK_SHIELD, new Ingredient(ItemID.OAK_LOGS, 2)),
			new Product(FLETCH_SHIELD, ItemID.WILLOW_SHIELD, new Ingredient(ItemID.WILLOW_LOGS, 2)),
			new Product(FLETCH_SHIELD, ItemID.MAPLE_SHIELD, new Ingredient(ItemID.MAPLE_LOGS, 2)),
			new Product(FLETCH_SHIELD, ItemID.YEW_SHIELD, new Ingredient(ItemID.YEW_LOGS, 2)),
			new Product(FLETCH_SHIELD, ItemID.MAGIC_SHIELD, new Ingredient(ItemID.MAGIC_LOGS, 2)),
			new Product(FLETCH_SHIELD, ItemID.REDWOOD_SHIELD, new Ingredient(ItemID.REDWOOD_LOGS, 2)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_WOOD, new Ingredient(ItemID.LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_OAK, new Ingredient(ItemID.OAK_LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_WILLOW, new Ingredient(ItemID.WILLOW_LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_TEAK, new Ingredient(ItemID.TEAK_LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_MAPLE, new Ingredient(ItemID.MAPLE_LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_MAHOGANY, new Ingredient(ItemID.MAHOGANY_LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_YEW, new Ingredient(ItemID.YEW_LOGS)),
			new Product(FLETCH_CUT_CROSSBOW, ItemID.XBOWS_CROSSBOW_STOCK_MAGIC, new Ingredient(ItemID.MAGIC_LOGS)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_BRONZE, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_WOOD), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_BRONZE)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_BLURITE, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_OAK), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_BLURITE)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_IRON, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_WILLOW), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_IRON)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_STEEL, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_TEAK), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_STEEL)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_MITHRIL, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_MAPLE), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_MITHRIL)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_ADAMANTITE, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_MAHOGANY), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_ADAMANTITE)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_RUNITE, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_YEW), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_RUNITE)),
			new Product(FLETCH_ATTACH_CROSSBOW, ItemID.XBOWS_CROSSBOW_UNSTRUNG_DRAGON, new Ingredient(ItemID.XBOWS_CROSSBOW_STOCK_MAGIC), new Ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_DRAGON)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_BRONZE, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_BRONZE), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_BLURITE, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_BLURITE), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_IRON, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_IRON), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_STEEL, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_STEEL), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_MITHRIL, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_MITHRIL), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_ADAMANTITE,new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_ADAMANTITE), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_RUNITE, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_RUNITE), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_STRING_CROSSBOW, ItemID.XBOWS_CROSSBOW_DRAGON, new Ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_DRAGON), new Ingredient(ItemID.XBOWS_CROSSBOW_STRING)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_DIAMOND, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.XBOWS_BOLT_TIPS_DIAMOND, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_DRAGONSTONE, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.XBOWS_BOLT_TIPS_DRAGONSTONE, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_EMERALD, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.XBOWS_BOLT_TIPS_EMERALD, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_JADE, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.XBOWS_BOLT_TIPS_JADE, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_ONYX, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.XBOWS_BOLT_TIPS_ONYX, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_OPAL, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.OPAL_BOLTTIPS, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_PEARL, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.PEARL_BOLTTIPS, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_RUBY, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.XBOWS_BOLT_TIPS_RUBY, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_SAPPHIRE, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.FLETCHING_BOLT_TIP_SAPPHIRE, 10)),
			new Product(FLETCH_ATTACH_TIPS, ItemID.DRAGON_BOLTS_UNENCHANTED_TOPAZ, new Ingredient(ItemID.DRAGON_BOLTS, 10), new Ingredient(ItemID.FLETCHING_BOLT_TIP_REDTOPAZ, 10)),
			new Product(FLETCH_DART, ItemID.BRONZE_DART, new Ingredient(ItemID.BRONZE_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.IRON_DART, new Ingredient(ItemID.IRON_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.STEEL_DART, new Ingredient(ItemID.STEEL_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.MITHRIL_DART, new Ingredient(ItemID.MITHRIL_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.ADAMANT_DART, new Ingredient(ItemID.ADAMANT_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.RUNE_DART, new Ingredient(ItemID.RUNE_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.AMETHYST_DART, new Ingredient(ItemID.AMETHYST_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FLETCH_DART, ItemID.DRAGON_DART, new Ingredient(ItemID.DRAGON_DART_TIP, 10), new Ingredient(ItemID.FEATHER, 10)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.ACHEY_TREE_LOGS, new Ingredient(ItemID.ACHEY_TREE_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.LOGS, new Ingredient(ItemID.LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.OAK_LOGS, new Ingredient(ItemID.OAK_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.WILLOW_LOGS, new Ingredient(ItemID.WILLOW_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.TEAK_LOGS, new Ingredient(ItemID.TEAK_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.ARCTIC_PINE_LOG, new Ingredient(ItemID.ARCTIC_PINE_LOG)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.MAPLE_LOGS, new Ingredient(ItemID.MAPLE_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.MAHOGANY_LOGS, new Ingredient(ItemID.MAHOGANY_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.YEW_LOGS, new Ingredient(ItemID.YEW_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.BLISTERWOOD_LOGS, new Ingredient(ItemID.BLISTERWOOD_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.MAGIC_LOGS, new Ingredient(ItemID.MAGIC_LOGS)),
			new Product(FIREMAKING_CAMPFIRE, ItemID.REDWOOD_LOGS, new Ingredient(ItemID.REDWOOD_LOGS)),
			new Product(FLETCH_ATTACH, ItemID.OPAL_BOLTS, new Ingredient(ItemID.BOLT,10) , new Ingredient(ItemID.FLETCHING_BOLT_TIP_OPAL,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_BLURITE_TIPPED_JADE, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_BLURITE,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_JADE,10)),
			new Product(FLETCH_ATTACH, ItemID.PEARL_BOLT, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_IRON,10) , new Ingredient(ItemID.PEARL_BOLTTIPS,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_STEEL_TIPPED_REDTOPAZ, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_STEEL,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_REDTOPAZ,10)),
			new Product(FLETCH_ATTACH, ItemID.BARBED_BOLTS, new Ingredient(ItemID.BOLT, 1) , new Ingredient(ItemID.BARBED_BOLTTIPS,1)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_SAPPHIRE, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_SAPPHIRE,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_EMERALD, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_EMERALD,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_RUBY, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_RUBY,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_DIAMOND, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_DIAMOND,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_DRAGONSTONE, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_DRAGONSTONE,10)),
			new Product(FLETCH_ATTACH, ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_ONYX, new Ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE,10) , new Ingredient(ItemID.XBOWS_BOLT_TIPS_ONYX,10)),
			new Product(FARM_ULTRA_COMPOST, ItemID.BUCKET_ULTRACOMPOST, new Ingredient(ItemID.FOSSIL_VOLCANIC_ASH,2), new Ingredient(ItemID.BUCKET_SUPERCOMPOST)),
			new Product(CHURNING_CREAM, ItemID.POT_OF_CREAM, new Ingredient(ItemID.BUCKET_MILK)),
			new Product(CHURNING_BUTTER_WITH_MILK, ItemID.POT_OF_BUTTER, new Ingredient(ItemID.BUCKET_MILK)),
			new Product(CHURNING_BUTTER_WITH_CREAM, ItemID.POT_OF_BUTTER, new Ingredient(ItemID.POT_OF_CREAM)),
			new Product(CHURNING_CHEESE_WITH_MILK, ItemID.CHEESE, new Ingredient(ItemID.BUCKET_MILK)),
			new Product(CHURNING_CHEESE_WITH_CREAM, ItemID.CHEESE, new Ingredient(ItemID.POT_OF_CREAM)),
			new Product(CHURNING_CHEESE_WITH_BUTTER, ItemID.CHEESE, new Ingredient(ItemID.POT_OF_BUTTER)),
			new Product(CHURNING_CHEESE_WITH_GARLIC, ItemID.CHEESE, new Ingredient(ItemID.POT_OF_NOT_GARLIC_BUTTER)),
            // @formatter:on
	};

	private final int[] widgetProductIds = new int[WIDGET_MAKE_SLOT_COUNT];

	@Inject private Client client;

	@Inject private InventoryManager inventoryManager;

	@Inject private ActionUtils actionUtils;

	private int selectedIndex = -1;

	private String question;

	@Subscribe
	public void onVarbitChanged(VarbitChanged evt)
	{
		if (evt.getValue() == VAR_SELECTED_INDEX) {
			this.selectedIndex = this.client.getVarpValue(evt.getValue());
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired evt)
	{
		if (evt.getScriptId() == MAKE_X_BUTTON_KEY ||
			evt.getScriptId() == MAKE_X_BUTTON_CLICK) {
			ScriptEvent se = evt.getScriptEvent();
			Widget source = se == null ? null : se.getSource();
			if (source != null) {
				this.selectedIndex = (source.getId() - WIDGET_ID_CHATBOX_FIRST_MAKE_BUTTON);
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired evt)
	{
		if (evt.getScriptId() == MAKE_X_SETUP) {
			log.debug("[proc_itembutton_draw] updating products");
			this.updateProducts();
		} else if (evt.getScriptId() == MAKE_X_BUTTON_TRIGGERED) {
			this.onQuestionAnswered();
		}
	}

	protected void unhandled(int itemId)
	{
		log.warn("[*] Unhandled chatbox action");
		log.warn(" |-> Question: {}", this.question);
		log.warn(" |-> Item ID: {}", itemId);
	}

	@Override
	public void setup()
	{
		/*
		 * Cooking
		 */
		this.registerAction(COOKING_TOP_PIZZA, ItemID.INCOMPLETE_PIZZA, ItemID.UNCOOKED_PIZZA, ItemID.PINEAPPLE_PIZZA, ItemID.ANCHOVIE_PIZZA,
				ItemID.MEAT_PIZZA
		);
		this.registerAction(COOKING_MIX_GRAPES, ItemID.JUG_UNFERMENTED_WINE, ItemID.JUG_UNFERMENTED_ZAMORAK_WINE);
		this.registerAction(COOKING_MIX_DOUGH, ItemID.BREAD_DOUGH, ItemID.PASTRY_DOUGH, ItemID.UNCOOKED_PITTA_BREAD, ItemID.PIZZA_BASE);
		/*
		 * Fletching
		 */
		this.registerAction(FLETCH_ATTACH, Fletching.UNENCHANTED_BOLTS_AND_ARROWS);
		this.registerAction(FLETCH_ATTACH_3T, ItemID.HUNTINGBEAST_SPIKE, ItemID.MOONLIGHT_ANTLER, ItemID.SUNLIGHT_ANTLER);
		this.registerAction(FLETCH_ATTACH, ItemID.HEADLESS_ARROW, ItemID.OGRE_ARROW, ItemID.SLAYER_BROAD_BOLT_AMETHYST, ItemID.AMETHYST_ARROW);
		this.registerAction(FLETCH_JAVELIN, Fletching.JAVELINS);
		this.registerAction(FLETCH_CUT_ARROW_SHAFT, ItemID.ARROW_SHAFT, ItemID.WINT_BRUMA_KINDLING, ItemID.OGRE_ARROW_SHAFT);
		this.registerAction(FLETCH_CUT_TIPS, Fletching.BOLT_TIPS);
		/*
		 *  Crafting
		 */
		this.registerAction(CRAFT_AMETHYST_HEADS_AND_TIPS, Crafting.AMETHYST_HEADS_AND_TIPS);
		/*
		 * Herblore
		 */
		this.registerAction(HERB_MIX_TAR, ItemID.SALAMANDER_TAR_GREEN, ItemID.SALAMANDER_TAR_ORANGE, ItemID.SALAMANDER_TAR_RED, ItemID.SALAMANDER_TAR_BLACK);
		this.registerAction(HERB_MIX_POTIONS_3T, ItemID.CUP_GUTHIX_REST_3);
		for (Recipe recipe : Herblore.UNFINISHED_POTIONS) {
			this.registerAction(HERB_MIX_UNFINISHED, recipe.getProductId());
		}
		for (Recipe recipe : Herblore.POTIONS) {
			this.registerAction(HERB_MIX_POTIONS, recipe.getIsSelectingIngredientAsProduct() ? recipe.getRequirements()[0].getItemId() : recipe.getProductId()); //TODO Find way to display product when getIsSelectingIngredientAsProduct = true
		}
		for (int leaveItem : Woodcutting.LEAVES){
			for (int foodItem : Woodcutting.RATION_FOOD) {
				this.registerAction(MAKING_FORESTERS_RATION, ItemID.FORESTRY_RATION, leaveItem, foodItem);
			}
		}
		/*
		 * Magic
		 */
		//this.registerAction(MAGIC_ENCHANT_BOLTS, Fletching.ENCHANTED_BOLTS);
	}

	private void onQuestionAnswered()
	{
		int currentProductId = this.widgetProductIds[this.selectedIndex];
		int amount = this.getActionCount(currentProductId);
		String question = this.question == null ? "?" : this.question;
		switch (question) {
			case "How many would you like to cook?":
			case "What would you like to cook?":
				this.actionManager.setAction(COOKING, amount, currentProductId);
				break;
			case "How would you like to cut the pineapple?":
				if (currentProductId == ItemID.PINEAPPLE_RING) {
					amount = Math.min(amount, this.actionUtils.getActionsUntilFull(4, 1));
				}
				this.actionManager.setAction(COOKING_CUT_FRUIT, amount, currentProductId);
				break;
			case "How many would you like to charge?":
				Magic.ChargeOrbSpell spell = Magic.ChargeOrbSpell.byProduct(currentProductId);
				Objects.requireNonNull(spell, "No charge orb spell found for product: " + currentProductId);
				this.actionManager.setAction(
						Action.MAGIC_CHARGE_ORB,
						Math.min(amount, spell.getSpell().getAvailableCasts(this.client, inventoryManager)),
						currentProductId
				);
				break;
			case "How many sets of bolts to enchant?":
				int enchantCrossbolBoltAmount = Magic.EnchantCrossbowBoltSpell.getAvailableCasts(client, this.inventoryManager, currentProductId);
				this.actionManager.setAction(
						Action.MAGIC_ENCHANT_BOLTS,
						Math.min(amount, enchantCrossbolBoltAmount),
						currentProductId
				);
				break;
			case "What would you like to smelt?": // Smelting bars
				Product smithingProduct = Recipe.forProduct(MULTI_MATERIAL_PRODUCTS, currentProductId, this.inventoryManager);
				if (smithingProduct != null) {
					if (amount > 0) {
						this.actionManager.setAction(
								smithingProduct.getAction(),
								amount,
								smithingProduct.getIsSelectingIngredientAsProduct() ? smithingProduct.getProductId() : currentProductId
						);
					}
				}
				break;
			case "How many would you like to string?": // Fletching/Stringing
			case "What would you like to string?": // Fletching/Stringing
			case "What would you like to make?": // Various
			case "How many batches would you like?":
			case "How many bars would you like to smith?": // Cannonballs
			case "How many gems would you like to cut?": // Cutting gems
			case "How many do you wish to make?": // Various
			case "How many sets of 15 do you wish to complete?": // Arrows
			case "How many sets of 15 do you wish to feather?": // Headless arrows
			case "?":
			default:
				Product recipe = Recipe.forProduct(MULTI_MATERIAL_PRODUCTS, currentProductId, this.inventoryManager);
				if (recipe != null) {
					amount = Math.min(amount, recipe.getMakeProductCount(this.inventoryManager));
					if (amount > 0) {
						this.actionManager.setAction(
								recipe.getAction(),
								amount,
								recipe.getIsSelectingIngredientAsProduct() ? recipe.getProductId() : currentProductId
						);
					}
				} else {
					this.setActionByItemId(currentProductId, amount);
				}
				break;
		}
	}

	private void updateProducts()
	{
		for (int slotIndex = 0; slotIndex < WIDGET_MAKE_SLOT_COUNT; slotIndex++) {
			Widget slotWidget = this.client.getWidget(WIDGET_MAKE_PARENT, WIDGET_MAKE_SLOT_START + slotIndex);
			Widget container = slotWidget == null ? null : slotWidget.getChild(WIDGET_MAKE_SLOT_ITEM);
			int id = container == null ? -1 : container.getItemId();
			if (id != -1 && id != ItemID.RD_HOURGLASS && id != ItemID.HW07_HOURGLASS) {
				this.widgetProductIds[slotIndex] = id;
			}
		}
		Widget questionWidget = this.client.getWidget(WIDGET_MAKE_PARENT, WIDGET_MAKE_QUESTION);
		if (questionWidget != null) {
			this.question = questionWidget.getText();
		}
		log.debug("updated products: {}", Arrays.toString(this.widgetProductIds));
	}

	private int getActionCount(int productId)
	{
		int n = this.client.getVarcIntValue(VAR_MAKE_AMOUNT);
		for (Smithing.Bar bar : Smithing.Bar.values()) {
			if (productId == bar.getItemId()) {
				return Math.min(n, bar.countAvailableOres(this.client));
			}
		}
		for (Cooking.Cookable entry : Cooking.Cookable.values()) {
			IDs raw = entry.getRaw(), cooked = entry.getCooked();
			if (cooked.contains(productId)) {
				int rawFish = this.inventoryManager.getItemCount(raw::contains);
				return Math.min(n, rawFish);
			}
		}
		return n;
	}

}