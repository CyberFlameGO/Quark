package vazkii.quark.world.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.DimensionConfig;
import vazkii.quark.base.lib.LibEntityIDs;
import vazkii.quark.base.module.Feature;
import vazkii.quark.base.potion.PotionMod;
import vazkii.quark.world.client.render.RenderStoneling;
import vazkii.quark.world.entity.EntityStoneling;
import vazkii.quark.world.item.ItemDiamondHeart;

public class Stonelings extends Feature {

	public static int maxYLevel, weight;
	public static DimensionConfig dimensions;
	public static boolean enableDiamondHeart;
	
	public static Item diamond_heart;
	
	@Override
	public void setupConfig() {
		maxYLevel = loadPropInt("Max Y Level", "", 24);
		weight = loadPropInt("Spawning Weight", "Higher = more stonelings", 80);
		enableDiamondHeart = loadPropBool("Enable Diamond Heart", "", true);
		
		dimensions = new DimensionConfig(configCategory);
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		if(enableDiamondHeart)
			diamond_heart = new ItemDiamondHeart();
		
		String stonelingName = "quark:stoneling";
		EntityRegistry.registerModEntity(new ResourceLocation(stonelingName), EntityStoneling.class, stonelingName, LibEntityIDs.STONELING, Quark.instance, 80, 3, true, 0xA1A1A1, 0x505050);
		
		EntityRegistry.addSpawn(EntityStoneling.class, weight, 1, 1, EnumCreatureType.MONSTER, DepthMobs.getBiomesWithMob(EntityZombie.class));
	}
	
	@Override
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityStoneling.class, RenderStoneling.FACTORY);
	}
	
	@Override
	public boolean requiresMinecraftRestartToEnable() {
		return true;
	}

}
