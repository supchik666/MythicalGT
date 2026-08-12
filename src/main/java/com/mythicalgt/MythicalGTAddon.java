package com.mythicalgt;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

@GTAddon
public class MythicalGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return MythicalGT.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return MythicalGT.MOD_ID;
    }
}
