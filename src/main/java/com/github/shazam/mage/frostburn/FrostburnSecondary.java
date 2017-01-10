package com.github.shazam.mage.frostburn;

import com.github.abilityapi.ability.Ability;
import com.github.abilityapi.ability.AbilityManager;
import com.github.abilityapi.ability.AbilityProvider;
import com.github.abilityapi.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.Optional;

import static com.github.shazam.util.EntityUtil.getBoundingBox;
import static com.github.shazam.util.VectorUtil.rotate;

/**
 * MageType: Frostburn.
 *
 * This type of mage has the ability to slow other characters and deal damage through deadly cold flames.
 *
 * PRIMARY - ??
 * SECONDARY - ?? : A large rock attack, carried by icy fire, targetted at an opponent. Causes movement to slow.
 * ULTIMATE - ??
 */
public class FrostburnSecondary extends Ability {

    private final Material material = Material.PACKED_ICE;
    private final Particle particle = Particle.REDSTONE;
    private final Particle particleExplode = Particle.CRIT_MAGIC;

    private final double locMovementMult = 0.8; // location movement multiplier

    private final double entityCollideDamage = 3.5;
    private final double entityKnockbackVelocity = 0.6;
    private final double entityKnockUpVelocity = 0.65;
    private final Vector entityKnockUp = new Vector(0, entityKnockUpVelocity, 0);

    private final AbilityManager manager;
    private final Location location;
    private Vector vector;

    private FallingBlock fallingBlock;
    private int ticks;
    private double degrees = Math.random() * 360; // particle rotation

    private boolean ignorePlayer = true;

    public FrostburnSecondary(AbilityManager manager, AbilityProvider provider, User user) {
        super(provider, user);
        this.manager = manager;
        this.location = getPlayer().getLocation();
        this.vector = location.getDirection().multiply(locMovementMult);

        location.add(vector.multiply(1.5));
    }

    @Override
    public void start() {
        final World world = location.getWorld();
        fallingBlock = world.spawnFallingBlock(location, new MaterialData(material));
        fallingBlock.setVelocity(vector);
        fallingBlock.setGravity(false);
        fallingBlock.setDropItem(false);
    }

    @Override
    public void update() {
        ticks++;

        if (hasCollidedWithBlock(fallingBlock)) {
            manager.stop(this);
            return;
        }

        final Optional<Entity> optional = getCollisionWithEntity(fallingBlock);
        if (optional.isPresent()) {
            final LivingEntity target = (LivingEntity) optional.get();
            if (target instanceof Player && target.getName().equals("Bluesocks")) {
                vector = fallingBlock.getVelocity().multiply(-1);
                fallingBlock.setVelocity(vector);
                ignorePlayer = false;
                return;
            }
            target.damage(entityCollideDamage);

            // apply velocity
            final Vector knockback = location.getDirection().multiply(entityKnockbackVelocity);
            target.setVelocity(target.getVelocity().add(knockback).add(entityKnockUp));

            manager.stop(this);
            return;
        }

        spawnUpdateParticles(location);
    }

    @Override
    public void stop() {
        explode(fallingBlock.getLocation());
        fallingBlock.remove();
    }

    @Override
    public boolean isExecuting() {
        return ticks < 20;
    }

    @EventHandler
    public void onFallingBlockSolidify(EntityChangeBlockEvent event) {
        if (event.getEntity().equals(fallingBlock)) {
            event.setCancelled(true);
            manager.stop(this);
        }
    }

    private void explode(Location location) {
        for (int i = 0; i < 20; i++) {
            final World world = location.getWorld();
            // random deviation
            final double xDiv = location.getX() + ((Math.random() * 2) - 1);
            final double yDiv = location.getY() + ((Math.random() * 2) - 1);
            final double zDiv = location.getZ() + ((Math.random() * 2) - 1);
            final Location position = new Location(world, xDiv, yDiv, zDiv);
            final double xDivVect = ((Math.random() * 0.5) - 0.25);
            final double yDivVect = ((Math.random() * 0.5) - 0.25);
            final double zDivVect = ((Math.random() * 0.5) - 0.25);

            world.spawnParticle(particleExplode, position, 0, xDivVect, yDivVect, zDivVect, 1);
        }
    }

    private void spawnUpdateParticles(Location location) {
        final World world = location.getWorld();
        for (int i = 0; i < 2; i++) {
            final double radius = 1.3;
            final double x = Math.cos(degrees += 0.15) * radius; // TODO: circle radius, field?
            final double y = Math.sin(degrees) * radius;
            final double z = 1.3;
            Location particleLoc = fallingBlock.getLocation().clone().add(rotate(new Vector(x, y, z), location.getYaw(), location.getPitch()));
            world.spawnParticle(particle, particleLoc, 0, 0.001, 1, 1);
        }
        for (int i = 0; i < 2; i++) {
            final double radius = 1.3;
            final double x = Math.cos((degrees += 0.15) + 180) * radius; // TODO: circle radius, field?
            final double y = Math.sin(degrees + 180) * radius;
            final double z = 1.3;
            Location particleLoc = fallingBlock.getLocation().clone().add(rotate(new Vector(x, y, z), location.getYaw(), location.getPitch()));
            world.spawnParticle(particle, particleLoc, 0, 44f/256f, 131f/256f, 175f/256f);
        }
    }

    // check if the block has collided via the velocity
    private boolean hasCollidedWithBlock(FallingBlock block) {
        final double range = 0.8;
        final Vector current = block.getVelocity();
        return !(vector.getX() - range < current.getX() && current.getX() < vector.getX() + range &&
               vector.getY() - range < current.getY() && current.getY() < vector.getY() + range &&
               vector.getZ() - range < current.getZ() && current.getZ() < vector.getZ() + range);
    }

    private Optional<Entity> getCollisionWithEntity(FallingBlock block) {
        return block.getNearbyEntities(3, 3, 3).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !ignorePlayer || !entity.equals(getPlayer()))
                .filter(entity -> getBoundingBox(entity).inRadius(block.getLocation().toVector(), 0.5))
                .findFirst();
    }

}
