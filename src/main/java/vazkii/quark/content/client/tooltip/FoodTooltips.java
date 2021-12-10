//package vazkii.quark.content.client.tooltip;
//
//import java.util.List;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.datafixers.util.Either;
//import com.mojang.datafixers.util.Pair;
//
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiComponent;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.FormattedText;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.world.effect.MobEffectCategory;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.food.FoodProperties;
//import net.minecraft.world.inventory.tooltip.TooltipComponent;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.event.RenderTooltipEvent;
//import net.minecraftforge.client.gui.ForgeIngameGui;
//import vazkii.quark.content.client.module.ImprovedTooltipsModule;
//
//public class FoodTooltips {
//
//	@OnlyIn(Dist.CLIENT)
//	public static void makeTooltip(RenderTooltipEvent.GatherComponents event, boolean showFood, boolean showSaturation) {
//		if(event.getItemStack().isEdible()) {
//			FoodProperties food = event.getItemStack().getItem().getFoodProperties();
//			if (food != null) {
//				int pips = food.getNutrition();
//				if(pips == 0)
//					return;
//				
//				int len = (int) Math.ceil((double) pips / ImprovedTooltipsModule.foodDivisor);
//
//				int saturationSimplified = 0;
//				float saturation = food.getSaturationModifier();
//				if(saturation < 1) {
//					if(saturation > 0.7)
//						saturationSimplified = 1;
//					else if(saturation > 0.5)
//						saturationSimplified = 2;
//					else if(saturation > 0.2)
//						saturationSimplified = 3;
//					else saturationSimplified = 4;
//				}
//
//				Component saturationText = new TranslatableComponent("quark.misc.saturation" + saturationSimplified).withStyle(ChatFormatting.GRAY);
//				List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
//
//				if (tooltip.isEmpty()) {
//					if(showSaturation)
//						tooltip.add(Either.left(saturationText));
//				}
//				else {
//					int i = 1;
//					if(showSaturation)
//						tooltip.add(i, Either.left(saturationText));
//				}
//			}
//		}
//	}
//
//	@OnlyIn(Dist.CLIENT)
//	public static void renderTooltip(RenderTooltipEvent.GatherComponents event) {
//		
//		if(event.getStack().isEdible()) {
//			FoodProperties food = event.getStack().getItem().getFoodProperties();
//			if (food != null) {
//				RenderSystem.color3f(1F, 1F, 1F);
//				Minecraft mc = Minecraft.getInstance();
//				PoseStack matrix = event.getMatrixStack();
//				
//				int pips = food.getNutrition();
//				if(pips == 0)
//					return;
//
//				boolean poison = false;
//				for (Pair<MobEffectInstance, Float> effect : food.getEffects()) {
//					if (effect.getFirst() != null && effect.getFirst().getEffect() != null && effect.getFirst().getEffect().getCategory() == MobEffectCategory.HARMFUL) {
//						poison = true;
//						break;
//					}
//				}
//
//				int count = (int) Math.ceil((double) pips / ImprovedTooltipsModule.foodDivisor);
//				boolean fract = pips % 2 != 0;
//				int renderCount = count;
//				int y = TooltipUtils.shiftTextByLines(event.getLines(), event.getY() + 10);
//				
//				boolean compress = count > ImprovedTooltipsModule.foodCompressionThreshold;
//				if(compress) {
//					renderCount = 1;
//					if(fract)
//						count--;
//				}
//
//				matrix.pushPose();
//				matrix.translate(0, 0, 500);
//				mc.getTextureManager().bind(ForgeIngameGui.GUI_ICONS_LOCATION);
//
//				for (int i = 0; i < renderCount; i++) {
//					int x = event.getX() + i * 9 - 1;
//
//					int u = 16;
//					if (poison)
//						u += 117;
//					int v = 27;
//
//					GuiComponent.blit(matrix, x, y, u, v, 9, 9, 256, 256);
//
//					u = 52;
//					if (fract && i == 0)
//						u += 9;
//					if (poison)
//						u += 36;
//
//					GuiComponent.blit(matrix, x, y, u, v, 9, 9, 256, 256);
//				}
//				
//				if(compress)
//					mc.font.drawShadow(matrix, "x" + (count + (fract ? ".5" : "")), event.getX() + 10, y + 1, 0xFF666666);
//				matrix.popPose();
//			}
//		}
//	}
//
//}
