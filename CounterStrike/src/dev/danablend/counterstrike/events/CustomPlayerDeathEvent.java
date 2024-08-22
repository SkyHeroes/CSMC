package dev.danablend.counterstrike.events;

import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomPlayerDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player victim;
    private Player killer;
    private Projectile bullet;
    private Weapon gun;

    public CustomPlayerDeathEvent(Player victim, Player killer, Projectile bullet, Weapon gun) {
        Utils.debug("CustomPlayerDeathEvent has been called...");
        this.victim = victim;
        this.killer = killer;
        this.bullet = bullet;
        this.gun = gun;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getVictim() {
        return victim;
    }

    public Player getKiller() {
        return killer;
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
