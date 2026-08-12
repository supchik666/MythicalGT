package com.mythicalgt;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.mythicalgt.materials.MythicalMaterials;

/**
 * A dedicated creative tab listing every GTCEu tool this addon adds, instead of leaving them
 * mixed into GTCEu's own shared "Tool" tab alongside every other material's tools. Doesn't
 * register any new items itself - just enumerates {@link MythicalMaterials#ALL_MATERIALS} against
 * every registered {@link GTToolType}, the same lookup {@code ToolHelper.get} uses everywhere else
 * in GTCEu, and lists whichever combinations actually produced an item.
 */
public final class MythicalCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MythicalGT.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TOOLS = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(MythicalCreativeTabs::icon)
                    .title(Component.translatable("itemGroup.mythical_gt.main"))
                    .displayItems((params, output) -> {
                        for (Material material : MythicalMaterials.ALL_MATERIALS) {
                            for (GTToolType toolType : GTToolType.getTypes().values()) {
                                ItemStack stack = ToolHelper.get(toolType, material);
                                if (!stack.isEmpty()) {
                                    output.accept(stack);
                                }
                            }
                        }
                    })
                    .build());

    private MythicalCreativeTabs() {}

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private static ItemStack icon() {
        for (Material material : MythicalMaterials.ALL_MATERIALS) {
            ItemStack stack = ToolHelper.get(GTToolType.SAW, material);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
