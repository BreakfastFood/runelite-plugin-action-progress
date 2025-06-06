package com.github.calebwhiting.runelite.data;

import net.runelite.api.gameval.ItemID;

public interface Woodcutting
{
	int[] LEAVES = {
			ItemID.LEAVES, ItemID.LEAVES_OAK, ItemID.LEAVES_WILLOW, ItemID.LEAVES_MAPLE,
			ItemID.LEAVES_YEW, ItemID.LEAVES_MAGIC
	};
	int[] RATION_FOOD = {
			ItemID.SARDINE, ItemID.SALMON, ItemID.TROUT, ItemID.COD, ItemID.HERRING, ItemID.PIKE, ItemID.MACKEREL,
			ItemID.RAW_TUNA, ItemID.BASS, ItemID.SWORDFISH, ItemID.LOBSTER, ItemID.SHARK, ItemID.LAVA_EEL, ItemID.MANTARAY, ItemID.MONKFISH,
			ItemID.DARK_CRAB, ItemID.ANGLERFISH, ItemID.COOKED_MEAT, ItemID.COOKED_CHICKEN
	};
}
