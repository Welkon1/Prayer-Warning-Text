package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Prayer Warning Text",
        description = "Color-coded overhead text for low prayer: three customizable tiers, with the out-of-prayer warning staying on in combat and clearing itself afterward",
        tags = {"prayer", "low prayer", "overhead", "warning", "text", "notification"}
)
public class PrayerWarningTextPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PrayerWarningTextOverlay overlay;

    @Override
    protected void startUp()
    {
        overlay.reset();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
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