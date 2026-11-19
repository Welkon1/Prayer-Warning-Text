package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.MenuAction;

@PluginDescriptor(
        name = "Prayer Warning Text",
        description = "Color-coded overhead text for low prayer: three customizable tiers, optional flash and sound alerts, and flexible rules for when the text hides",
        tags = {"prayer", "low prayer", "overhead", "warning", "text", "notification", "alert", "flash", "sound"}
)
public class PrayerWarningTextPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PrayerWarningTextOverlay overlay;

    @Inject
    private PrayerWarningFlashOverlay flashOverlay;

    @Override
    protected void startUp()
    {
        overlay.reset();
        overlayManager.add(overlay);
        overlayManager.add(flashOverlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlayManager.remove(flashOverlay);
        flashOverlay.stop();
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        Player local = client.getLocalPlayer();
        if (local == null)
        {
            return;
        }

        // Count a hit on you, or a hit you dealt, as being in combat
        if (event.getActor() == local || event.getHitsplat().isMine())
        {
            overlay.markCombat();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        // Trying to turn on a prayer or the quick-prayer orb shows the warning again at 0 prayer
        if (event.getMenuAction() == MenuAction.CC_OP
                && "Activate".equals(event.getMenuOption()))
        {
            overlay.markPrayerAttempt();
        }
        // Clicking Pickpocket starts a pickpocket attempt, which can end in stun damage
        if ("Pickpocket".equalsIgnoreCase(event.getMenuOption()))
        {
            overlay.markPickpocket();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE)
        {
            return;
        }

        String message = event.getMessage();
        String lower = message.toLowerCase();

        // Pickpocket messages also mark the attempt, in case the stun damage lands later
        if (lower.contains("pick") && lower.contains("pocket"))
        {
            overlay.markPickpocket();
        }

        // The Ancient Pyramid altar drains all prayer when it switches your spellbook
        if (message.contains("You feel a strange wisdom fill your mind")
                || message.contains("You feel a strange drain upon your memory"))
        {
            overlay.markSpellbookSwitch();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        overlay.onGameTick();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GameState state = event.getGameState();
        if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
        {
            overlay.reset();
        }
    }

    @Provides
    PrayerWarningTextConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PrayerWarningTextConfig.class);
    }
}