package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class PrayerWarningTextOverlay extends Overlay
{
    // A tick value far enough in the past that it never counts as "recent"
    private static final int NEVER = -100000;
    private static final double TICK_SECONDS = 0.6;

    private enum Tier
    {
        NONE, LOW, REALLY_LOW, OUT
    }

    private final Client client;
    private final PrayerWarningTextConfig config;

    private int lastCombatTick = NEVER;
    private Tier lastTier = Tier.NONE;
    private int tierSinceTick = NEVER;
    private boolean wasPraying = false;

    @Inject
    private PrayerWarningTextOverlay(Client client, PrayerWarningTextConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    /** Called by the plugin when you deal or take a hit. */
    void markCombat()
    {
        lastCombatTick = client.getTickCount();
    }

    /** Called by the plugin on logout or world hop. */
    void reset()
    {
        lastCombatTick = NEVER;
        lastTier = Tier.NONE;
        tierSinceTick = NEVER;
        wasPraying = false;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return null;
        }

        int points = client.getBoostedSkillLevel(Skill.PRAYER);
        int now = client.getTickCount();

        // Remember when the current tier began, so each tier's hide delay has a starting point
        Tier tier = getTier(points);
        if (tier != lastTier)
        {
            lastTier = tier;
            tierSinceTick = now;
        }

        // Turning a prayer on restarts the hide timer so the warning comes back
        boolean praying = isAnyPrayerActive();
        if (config.reappearOnPrayer() && praying && !wasPraying)
        {
            tierSinceTick = now;
        }
        wasPraying = praying;

        // While praying or in combat the warning stays on, and the hide delay restarts when that ends
        boolean keepOn = config.stayOnWhilePraying() && (praying || inCombat(now));
        if (keepOn)
        {
            tierSinceTick = now;
        }

        // Fighting without a prayer on: keep the warning off
        if (config.hideInCombatWithoutPrayer() && !praying && inCombat(now))
        {
            return null;
        }

        String text;
        Color color;

        switch (tier)
        {
            case OUT:
                if (config.hideOutOfCombat() && !keepOn && !shouldKeepOutText(now))
                {
                    return null;
                }
                text = config.outText();
                color = config.outColor();
                break;

            case REALLY_LOW:
                if (config.onlyWhilePraying() && !isAnyPrayerActive())
                {
                    return null;
                }
                if (config.reallyLowHideEnabled() && !keepOn && delayPassed(now, config.reallyLowHideSeconds()))
                {
                    return null;
                }
                text = config.text();
                color = config.reallyLowColor();
                break;

            case LOW:
                if (config.onlyWhilePraying() && !isAnyPrayerActive())
                {
                    return null;
                }
                if (config.lowHideEnabled() && !keepOn && delayPassed(now, config.lowHideSeconds()))
                {
                    return null;
                }
                text = config.text();
                color = config.lowColor();
                break;

            default:
                return null;
        }

        if (text == null || text.isEmpty())
        {
            return null;
        }
        text = text.replace("{points}", String.valueOf(points));

        Point location = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + config.heightOffset());
        if (location != null)
        {
            OverlayUtil.renderTextLocation(graphics, location, text, color);
        }

        return null;
    }

    /** The most severe matching warning wins: out of prayer, then really low, then low. */
    private Tier getTier(int points)
    {
        if (config.outEnabled() && points <= config.outThreshold())
        {
            return Tier.OUT;
        }
        if (config.reallyLowEnabled() && points <= config.reallyLowThreshold())
        {
            return Tier.REALLY_LOW;
        }
        if (config.lowEnabled() && points <= config.lowThreshold())
        {
            return Tier.LOW;
        }
        return Tier.NONE;
    }

    /** True once the current tier has been showing for longer than the given number of seconds. */
    private boolean delayPassed(int now, int seconds)
    {
        return now - tierSinceTick > toTicks(seconds);
    }

    /**
     * In combat: keep showing the out-of-prayer text.
     * Not in combat: keep showing it for the configured delay, counted from the later of
     * "prayer ran out" and "combat ended", then hide it.
     */
    private boolean shouldKeepOutText(int now)
    {
        int combatTicks = toTicks(config.combatSeconds());
        int delayTicks = toTicks(config.outOfCombatSeconds());

        int sinceCombat = now - lastCombatTick;
        if (sinceCombat < 0)
        {
            // Tick counter went backwards (e.g. after relogging); forget the old combat
            lastCombatTick = NEVER;
            sinceCombat = now - NEVER;
        }

        if (sinceCombat <= combatTicks)
        {
            return true;
        }

        int combatEnded = lastCombatTick + combatTicks;
        int countdownStart = Math.max(tierSinceTick, combatEnded);
        return now - countdownStart <= delayTicks;
    }

    /** True if you dealt or took a hit within the combat timeout. */
    private boolean inCombat(int now)
    {
        int sinceCombat = now - lastCombatTick;
        return sinceCombat >= 0 && sinceCombat <= toTicks(config.combatSeconds());
    }

    private static int toTicks(int seconds)
    {
        return (int) Math.ceil(seconds / TICK_SECONDS);
    }

    private boolean isAnyPrayerActive()
    {
        for (Prayer prayer : Prayer.values())
        {
            if (client.isPrayerActive(prayer))
            {
                return true;
            }
        }
        return false;
    }
}