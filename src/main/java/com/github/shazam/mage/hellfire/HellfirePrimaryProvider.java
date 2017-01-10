package com.github.shazam.mage.hellfire;

import com.github.abilityapi.ability.Ability;
import com.github.abilityapi.ability.AbilityManager;
import com.github.abilityapi.ability.AbilityProvider;
import com.github.abilityapi.sequence.Sequence;
import com.github.abilityapi.sequence.SequenceBlueprint;
import com.github.abilityapi.sequence.SequenceBuilder;
import com.github.abilityapi.sequence.action.Actions;
import com.github.abilityapi.user.User;

public class HellfirePrimaryProvider implements AbilityProvider {
    @Override
    public SequenceBlueprint getSequence() {
        return new SequenceBuilder()
                .action(Actions.START_SNEAK)
                    //.condition((player, event) -> User.get(player).getInstance(MageUser.class).getMageType().equals(MageType.HELLFIRE))
                .build(this);
    }

    @Override
    public Ability createInstance(AbilityManager abilityManager, Sequence sequence, User user) {
        return new HellfirePrimary(abilityManager, this, sequence, user);
    }
}
