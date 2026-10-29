package com.example;

import com.example.PrayerWarningTextConfig.AlertMode;
import com.example.PrayerWarningTextConfig.VisibilityMode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.client.Notifier;
import net.runelite.client.config.FlashNotification;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import java.awt.TrayIcon;
import net.runelite.client.config.RequestFocusType;
import net.runelite.client.ui.ClientUI;
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
    private final Notifier notifier;
    private final ClientUI clientUI;
    private final PrayerWarningFlashOverlay flashOverlay;

    private int lastCombatTick = NEVER;
    private Tier lastTier = Tier.NONE;
    private int tierSinceTick = NEVER;
    private boolean wasPraying = false;
    private Tier alertTier = Tier.NONE;
    private boolean alertTierKnown = false;

    @Inject
    private PrayerWarningTextOverlay(Client client, PrayerWarningTextConfig config, Notifier notifier,
                                     ClientUI clientUI, PrayerWarningFlashOverlay flashOverlay)
    {
        this.client = client;
        this.config = config;
        this.notifier = notifier;
        this.clientUI = clientUI;
        this.flashOverlay = flashOverlay;
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
        alertTier = Tier.NONE;
        alertTierKnown = false;
        flashOverlay.stop();
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

        // With percentages on, thresholds compare against a percent of your Prayer level
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        int thresholdValue = (config.usePercentages() && maxPrayer > 0) ? (points * 100) / maxPrayer : points;

        // Remember when the current tier began, so each tier's hide delay has a starting point
        Tier tier = getTier(thresholdValue);
        if (tier != lastTier)
        {
            lastTier = tier;
            tierSinceTick = now;
        }

        VisibilityMode mode = config.visibilityMode();

        // Turning a prayer on always restarts the hide timer, so the warning comes back
        boolean praying = isAnyPrayerActive();
        if (praying && !wasPraying)
        {
            tierSinceTick = now;
        }
        wasPraying = praying;

        // Every mode except "Hide after delay only" keeps the warning on while praying or in combat
        boolean keepOn = mode != VisibilityMode.DELAY_ONLY && (praying || inCombat(now));
        if (keepOn)
        {
            tierSinceTick = now;
        }

        // Fighting without a prayer on: keep the warning off
        if (mode == VisibilityMode.HIDE_IN_COMBAT && !praying && inCombat(now))
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
                if (mode == VisibilityMode.ONLY_WHILE_PRAYING && !praying)
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
                if (mode == VisibilityMode.ONLY_WHILE_PRAYING && !praying)
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

        // Keeps custom text with {points} working, and adds the number if the toggle is on
        boolean hasPlaceholder = text.contains("{points}");
        text = text.replace("{points}", String.valueOf(points));
        if (config.showPoints() && !hasPlaceholder)
        {
            text = text + " " + points;
        }

        // Optional blink for the out of prayer warning: on for half a second, off for half a second
        if (tier == Tier.OUT && config.blinkOutOfPrayer() && (System.currentTimeMillis() / 500) % 2 == 1)
        {
            return null;
        }

        // Only change the font if the player turned on a size or bold option
        Font originalFont = graphics.getFont();
        if (config.customTextSize() || config.boldText())
        {
            float size = config.customTextSize() ? config.textSize() : originalFont.getSize2D();
            int style = config.boldText() ? Font.BOLD : originalFont.getStyle();
            graphics.setFont(originalFont.deriveFont(style, size));
        }

        Point location = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + config.heightOffset());
        if (location != null)
        {
            OverlayUtil.renderTextLocation(graphics, location, text, color);
        }

        graphics.setFont(originalFont);

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

    /** Called by the plugin every game tick. Fires alerts the first time you reach a warning. */
    void onGameTick()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        int points = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        int value = (config.usePercentages() && maxPrayer > 0) ? (points * 100) / maxPrayer : points;
        Tier tier = getTier(value);

        Tier previous = alertTier;
        alertTier = tier;

        // Ignore the state you were already in when you logged in
        boolean first = !alertTierKnown;
        alertTierKnown = true;
        if (first || tier == previous)
        {
            return;
        }

        if (tier.ordinal() > previous.ordinal())
        {
            fireAlert(tier);
        }
        else
        {
            // Prayer recovered, so the reason for the flash is gone
            flashOverlay.stop();
        }
    }

    private void fireAlert(Tier tier)
    {
        AlertMode mode;
        String message;

        switch (tier)
        {
            case LOW:
                mode = config.lowAlert();
                message = "Low prayer";
                break;
            case REALLY_LOW:
                mode = config.reallyLowAlert();
                message = "Really low prayer";
                break;
            case OUT:
                mode = config.outAlert();
                message = "Out of prayer";
                break;
            default:
                return;
        }

        boolean flash = mode == AlertMode.FLASH || mode == AlertMode.FLASH_AND_SOUND;
        boolean sound = mode == AlertMode.SOUND || mode == AlertMode.FLASH_AND_SOUND;

        // A worse warning replaces any earlier flash
        flashOverlay.stop();

        if (clientUI.isFocused() && !config.alertWhenFocused())
        {
            return;
        }

        if (flash)
        {
            flashOverlay.start();
        }

        if (sound)
        {
            Notification soundOnly = new Notification(
                    true,                          // enabled
                    true,                          // initialized
                    true,                          // override RuneLite's global notification settings
                    true,                         // tray: no Windows notification
                    TrayIcon.MessageType.INFO,     // tray icon type (unused)
                    RequestFocusType.OFF,          // don't pull the window to the front
                    NotificationSound.NATIVE,      // the system beep
                    null,                          // sound file name (custom sounds only)
                    100,                           // volume (custom sounds only)
                    0,                             // timeout (tray only)
                    false,                         // no chat message
                    FlashNotification.DISABLED,    // RuneLite's own flash isn't used
                    Color.RED,                     // flash color (unused)
                    true);                         // send even when the game is focused
            notifier.notify(soundOnly, message);
        }
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