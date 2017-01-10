package com.github.shazam;

import com.github.abilityapi.AbilityAPI;
import com.github.shazam.mage.frostburn.FrostburnProvider;
import com.github.shazam.mage.hellfire.HellfirePrimaryProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ShazamPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final AbilityAPI abilityAPI = AbilityAPI.get();
        abilityAPI.getAbilityRegistry().register(new FrostburnProvider());
        abilityAPI.getAbilityRegistry().register(new HellfirePrimaryProvider());
    }

}
