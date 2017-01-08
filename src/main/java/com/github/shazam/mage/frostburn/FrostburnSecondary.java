package com.github.shazam.mage.frostburn;

import com.github.abilityapi.ability.Ability;
import com.github.abilityapi.ability.AbilityManager;
import com.github.abilityapi.ability.AbilityProvider;
import com.github.abilityapi.user.User;
import com.github.shazam.util.EntityUtil;
import javafx.scene.transform.Rotate;
import org.bukkit.*;
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

    private final Material material = Material.COBBLESTONE;
    private final Particle particle = Particle.REDSTONE;
    private final Particle particleExplode = Particle.CRIT_MAGIC;

    private final int range = 10; // blocks
    private final double locMovementMult = 0.8; // location movement multiplier

    private final double entityCollideDamage = 3.5;
    private final double entityKnockbackVelocity = 0.6;
    private final double entityKnockUpVelocity = 0.65;
    private final Vector entityKnockUp = new Vector(0, entityKnockUpVelocity, 0);
    private final double blockCollideDistance = 0.55;

    private final AbilityManager manager;
    private final Location location;
    private final Vector vector;

    private FallingBlock fallingBlock;
    private int ticks;
    private double degrees = Math.random() * 360; // particle rotation

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
            manager.stop(this); // TODO: set things on fire, do some break particle sequence?
            explode(fallingBlock.getLocation());
            return;
        }

        final World world = location.getWorld();
        final Optional<Entity> optional = getCollisionWithEntity(fallingBlock);
        if (optional.isPresent()) {
            final LivingEntity target = (LivingEntity) optional.get();
            target.damage(entityCollideDamage);

            // apply velocity
            final Vector knockback = location.getDirection().multiply(entityKnockbackVelocity);
            target.setVelocity(target.getVelocity().add(knockback).add(entityKnockUp));

            manager.stop(this);
            explode(fallingBlock.getLocation());
            return;
        }

//        location.add(vector);
        spawnUpdateParticles(location);
    }

    @Override
    public void stop() {
        fallingBlock.remove(); // boom
    }

    @Override
    public boolean isExecuting() {
        return ticks < 20;
    }

    @EventHandler
    public void onFallingBlockSolidify(EntityChangeBlockEvent event) {
        if (event.getEntity().equals(fallingBlock)) {
            event.setCancelled(true);
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
//            final Vector velocity = new Vector(xDivVect, yDivVect, zDivVect);

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

    private Vector rotate(Vector initial, float yaw, float pitch) {
        final double yawR = yaw / 180.0 * Math.PI;
        final double pitchR = pitch / 180.0 * Math.PI;
        return rotateAboutY(rotateAboutX(initial, pitchR), -yawR);
    }

    private Vector rotateAboutX(Vector initial, double a) {
        final double y = Math.cos(a) * initial.getY() - Math.sin(a) * initial.getZ();
        final double z = Math.sin(a) * initial.getY() + Math.cos(a) * initial.getZ();
        return initial.setY(y).setZ(z);
}

    private Vector rotateAboutY(Vector initial, double a) {
        final double x = Math.cos(a) * initial.getX() + Math.sin(a) * initial.getZ();
        final double z = -Math.sin(a) * initial.getX() + Math.cos(a) * initial.getZ();
        return initial.setX(x).setZ(z);
    }

    private Vector rotateAboutZ(Vector initial, double a) {
        final double x = Math.cos(a) * initial.getX() - Math.sin(a) * initial.getY();
        final double y = Math.sin(a) * initial.getX() + Math.cos(a) * initial.getY();
        return initial.setX(x).setY(y);
    }

    // check if the block has collided via the velocity
    private boolean hasCollidedWithBlock(FallingBlock block) {
        final Vector vector = block.getVelocity();
        final double x = blockCollideDistance;
        return vector.getX() == x || vector.getY() == x || vector.getZ() == x;
    }

    private Optional<Entity> getCollisionWithEntity(FallingBlock block) {
        return block.getNearbyEntities(3, 3, 3).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !entity.equals(getPlayer()))
                .filter(entity -> getBoundingBox(entity).inRadius(block.getLocation().toVector(), 0.5))
                .findFirst();
    }

}
