package dev.danablend.counterstrike.events;

import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WeaponFireEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player shooter;
    private Weapon gun;
    private Projectile bullet;


    public WeaponFireEvent(Player shooter, Weapon gun, Projectile bullet) {
        System.out.println("############## WeaponFireEvent");

        Utils.debug("WeaponFireEvent has been called...");
        this.shooter = shooter;
        this.gun = gun;
        this.bullet = bullet;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return shooter;
    }

    public Projectile getBullet() {
        return bullet;
    }

    public Weapon getGun() {
        return gun;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
