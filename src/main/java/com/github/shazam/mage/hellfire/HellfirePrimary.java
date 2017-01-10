package com.github.shazam.mage.hellfire;

import com.github.abilityapi.ability.Ability;
import com.github.abilityapi.ability.AbilityManager;
import com.github.abilityapi.ability.AbilityProvider;
import com.github.abilityapi.sequence.Sequence;
import com.github.abilityapi.user.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.HashMap;

import static com.github.shazam.util.EntityUtil.getBoundingBox;

public class HellfirePrimary extends Ability {

    private final AbilityProvider provider;
    private final AbilityManager manager;
    private final Sequence sequence;
    private final User user;
    private final Player player;
    private int ticks;

    private ArrayList<ArmorStand> hellfire;
    private HashMap<Location, Block> hellfireBlocks;

    private boolean hasUnsneaked;

    public HellfirePrimary(AbilityManager manager, AbilityProvider provider, Sequence sequence, User user) {
        super(provider, user);
        this.provider = provider;
        this.manager = manager;
        this.sequence = sequence;
        this.user = user;
        this.player = user.getPlayer();

        hellfire = new ArrayList<ArmorStand>();
        hellfireBlocks = new HashMap<Location, Block>();
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        if(!hasUnsneaked && !player.isSneaking()) {
            hasUnsneaked = true;
        }

        if (ticks++ % 5 == 0 && ticks < 20 * 3 && !hasUnsneaked) {
            ArmorStand fire = spawnFire();
            hellfire.add(fire);
            fire.setVelocity(player.getLocation().getDirection().multiply(1.5));

            fire.getWorld().playSound(fire.getLocation(), Sound.ITEM_FIRECHARGE_USE, .25f, 1f);
        }

        cancelArmorStandIgnition();

        spawnFireParticles();

        getCollisionWithEntity();

    }

    @Override
    public void stop() {removeFire();}

    @Override
    public boolean isExecuting() {
        return ticks < 20 * 8;
    }

    public ArmorStand spawnFire() {
        final World world = player.getWorld();
        final Location location = player.getLocation().add(0,1,0);

        ArmorStand fire = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        fire.setArms(false);
        fire.setBasePlate(false);
        fire.setVisible(false);
        fire.setInvulnerable(true);

        return fire;
    }

    public void spawnFireParticles() {
        for (ArmorStand fire : hellfire) {
            final World world = fire.getWorld();
            final Location location = fire.getLocation();

            if (fire.isOnGround() && fire.getVelocity().getBlockX() < .5) {
                world.spawnParticle(Particle.LAVA, location, 0, .5f, 0, .5f);
                Material blockMaterial = fire.getLocation().getBlock().getType();
                if (!blockMaterial.equals(Material.FIRE) && !blockMaterial.equals(Material.WATER) && !blockMaterial.equals(Material.LAVA) && !blockMaterial.equals(Material.STATIONARY_WATER) && !blockMaterial.equals(Material.STATIONARY_LAVA)) {
                    hellfireBlocks.put(fire.getLocation(), fire.getLocation().getBlock());
                    fire.getLocation().getBlock().setType(Material.FIRE);
                }
            } else {
                world.spawnParticle(Particle.FLAME, location.add(0, 0.45f, 0), 0, 0f, 0f, 0f);
            }
            world.spawnParticle(Particle.SMOKE_NORMAL, location.add(0, 0.5f, 0), 1, .2f, .1f, .2f, 0.1);
        }
    }

    public void cancelArmorStandIgnition() {
        for (ArmorStand fire : hellfire) {
            fire.setFireTicks(0);
        }
    }

    public void removeFire() {
        if (hellfire.get(0) != null) {
            final World world = hellfire.get(0).getWorld();
            final Location soundLocation = hellfire.get(0).getLocation();

            for (ArmorStand fire : hellfire) {
                fire.remove();
            }

            for (Location location : hellfireBlocks.keySet()) {
                location.getBlock().setType(Material.AIR);

                location.getBlock().setType(hellfireBlocks.get(location).getType());
                location.getBlock().setData(hellfireBlocks.get(location).getData());
            }
            hellfireBlocks.clear();

            world.playSound(soundLocation, Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
        }
    }

    public void damageEntity(LivingEntity target) {
        target.setFireTicks(20 * 3);
    }

    private void getCollisionWithEntity() {
        for (ArmorStand fire : hellfire) {
            for (Entity entity : fire.getNearbyEntities(1, 1, 1)) {
                if (entity instanceof LivingEntity && !entity.equals(player) && !entity.getType().equals(EntityType.ARMOR_STAND) && getBoundingBox(entity).inRadius(fire.getLocation().toVector(), 1)) {
                    damageEntity((LivingEntity) entity);
                }
            }
        }
    }

}
