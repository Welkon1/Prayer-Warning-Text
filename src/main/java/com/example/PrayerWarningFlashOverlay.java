package com.example;

import com.example.PrayerWarningTextConfig.FlashStyle;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws the screen flash for the alerts. Only one flash is active at a time,
 * so a newer alert replaces an older one.
 */
@Singleton
public class PrayerWarningFlashOverlay extends Overlay
{
    private final Client client;
    private final PrayerWarningTextConfig config;

    private volatile boolean active = false;
    private volatile long startMillis = 0;

    @Inject
    private PrayerWarningFlashOverlay(Client client, PrayerWarningTextConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    /** Starts (or restarts) the flash. */
    void start()
    {
        startMillis = System.currentTimeMillis();
        active = true;
    }

    /** Stops the flash right away. */
    void stop()
    {
        active = false;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!active)
        {
            return null;
        }

        long elapsed = System.currentTimeMillis() - startMillis;
        if (elapsed >= config.flashSeconds() * 1000L)
        {
            active = false;
            return null;
        }

        // Each blink is on for the first half of its cycle and off for the second half
        double cycleMillis = 1000.0 / config.flashSpeed();
        if (elapsed % cycleMillis >= cycleMillis / 2)
        {
            return null;
        }

        int width = client.getCanvasWidth();
        int height = client.getCanvasHeight();
        graphics.setColor(config.flashColor());

        if (config.flashStyle() == FlashStyle.BORDER)
        {
            int border = Math.min(config.borderWidth(), Math.min(width, height) / 2);
            graphics.fillRect(0, 0, width, border);
            graphics.fillRect(0, height - border, width, border);
            graphics.fillRect(0, border, border, height - border * 2);
            graphics.fillRect(width - border, border, border, height - border * 2);
        }
        else
        {
            graphics.fillRect(0, 0, width, height);
        }

        return null;
    }
}