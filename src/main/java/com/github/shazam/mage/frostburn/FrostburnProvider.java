package com.github.shazam.mage.frostburn;

import com.github.abilityapi.ability.Ability;
import com.github.abilityapi.ability.AbilityManager;
import com.github.abilityapi.ability.AbilityProvider;
import com.github.abilityapi.sequence.Sequence;
import com.github.abilityapi.sequence.SequenceBlueprint;
import com.github.abilityapi.sequence.SequenceBuilder;
import com.github.abilityapi.sequence.action.Actions;
import com.github.abilityapi.user.User;

public class FrostburnProvider implements AbilityProvider {

    @Override
    public SequenceBlueprint getSequence() {
        return new SequenceBuilder()
                .action(Actions.INTERACT_LEFT)
                    //.condition((player, event) -> User.get(player).getInstance(MageUser.class).getMageType().equals(MageType.FROSTBURN))
                .build(this);
    }

    @Override
    public Ability createInstance(AbilityManager abilityManager, Sequence sequence, User user) {
        return new FrostburnSecondary(abilityManager, this, user);
    }

}
