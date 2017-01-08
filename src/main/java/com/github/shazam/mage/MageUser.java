package com.github.shazam.mage;

import com.github.abilityapi.user.BaseUser;
import org.bukkit.entity.Player;

public class MageUser extends BaseUser {

    protected MageType mageType;

    public MageUser(Player player) {
        super(player);
    }

}
