package com.mythicalgt;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mythicalgt.materials.MythicalMaterials;

/**
 * Adds GTCEu Alloy Smelter recipes and a full GT tool line for every Mythic Metals CE Port metal.
 * See {@link MythicalMaterials} for the actual material/tool wiring - this class only owns the
 * mod entry point and the GTCEu material-lifecycle event subscriptions.
 */
@Mod(MythicalGT.MOD_ID)
public class MythicalGT {

    public static final String MOD_ID = "mythical_gt";
    public static GTRegistrate REGISTRATE = GTRegistrate.create(MythicalGT.MOD_ID);

    public MythicalGT() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::addMaterialRegistries);
        modEventBus.addListener(this::addMaterials);
        modEventBus.addListener(this::modifyMaterials);

        MythicalCreativeTabs.register(modEventBus);

        REGISTRATE.registerRegistrate();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(MythicalGT.MOD_ID);
    }

    private void addMaterials(MaterialEvent event) {
        MythicalMaterials.init();
    }

    private void modifyMaterials(PostMaterialEvent event) {
        MythicalMaterials.modify();
    }
}
