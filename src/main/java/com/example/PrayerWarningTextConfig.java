package com.example;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("prayerwarningtext")
public interface PrayerWarningTextConfig extends Config
{
    @ConfigSection(
            name = "Text",
            description = "What the warning says and where it appears",
            position = 0
    )
    String textSection = "textSection";

    @ConfigSection(
            name = "Low prayer",
            description = "The first warning",
            position = 1
    )
    String lowSection = "lowSection";

    @ConfigSection(
            name = "Really low prayer",
            description = "A stronger warning when prayer keeps dropping",
            position = 2
    )
    String reallyLowSection = "reallyLowSection";

    @ConfigSection(
            name = "Out of prayer",
            description = "What happens when your prayer runs out",
            position = 3
    )
    String outSection = "outSection";

    @ConfigSection(
            name = "Combat",
            description = "How the plugin decides whether you are in combat",
            position = 4
    )
    String combatSection = "combatSection";

    // ---------- Text ----------

    @ConfigItem(
            keyName = "text",
            name = "Warning text",
            description = "Text shown for the low and really low warnings. Use {points} to show your current prayer points, e.g. LOW PRAYER: {points}",
            position = 0,
            section = textSection
    )
    default String text()
    {
        return "LOW PRAYER";
    }

    @ConfigItem(
            keyName = "outText",
            name = "Out of prayer text",
            description = "Text shown when your prayer has run out. Use {points} to show your current prayer points",
            position = 1,
            section = textSection
    )
    default String outText()
    {
        return "NO PRAYER";
    }

    @Range(min = -100, max = 150)
    @ConfigItem(
            keyName = "heightOffset",
            name = "Height offset",
            description = "Moves the text up (positive) or down (negative) from the top of your head. Raise it if the text overlaps your overhead prayer icon",
            position = 2,
            section = textSection
    )
    default int heightOffset()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "onlyWhilePraying",
            name = "Only while praying",
            description = "Only show the low and really low warnings while at least one prayer is active. Does not affect the out of prayer warning",
            position = 3,
            section = textSection
    )
    default boolean onlyWhilePraying()
    {
        return false;
    }

    @ConfigItem(
            keyName = "reappearOnPrayer",
            name = "Reappear when prayer activated",
            description = "When you turn a prayer on, the warning comes back and its hide timer starts over",
            position = 4,
            section = textSection
    )
    default boolean reappearOnPrayer()
    {
        return true;
    }

    // ---------- Low prayer ----------

    @ConfigItem(
            keyName = "lowEnabled",
            name = "Show low prayer warning",
            description = "Turn the low prayer warning on or off",
            position = 0,
            section = lowSection
    )
    default boolean lowEnabled()
    {
        return true;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            keyName = "lowThreshold",
            name = "At or below",
            description = "The warning appears when your prayer points are at or below this number",
            position = 1,
            section = lowSection
    )
    default int lowThreshold()
    {
        return 20;
    }

    @ConfigItem(
            keyName = "lowColor",
            name = "Color",
            description = "Color of the low prayer warning (an off-white by default)",
            position = 2,
            section = lowSection
    )
    default Color lowColor()
    {
        return new Color(238, 238, 238);
    }

    @ConfigItem(
            keyName = "lowHideEnabled",
            name = "Hide after delay",
            description = "Hide the low prayer warning a set time after your prayer drops into this range",
            position = 3,
            section = lowSection
    )
    default boolean lowHideEnabled()
    {
        return true;
    }

    @Range(min = 1, max = 300)
    @ConfigItem(
            keyName = "lowHideSeconds",
            name = "Hide delay (seconds)",
            description = "How many seconds the low prayer warning stays on",
            position = 4,
            section = lowSection
    )
    default int lowHideSeconds()
    {
        return 10;
    }

    // ---------- Really low prayer ----------

    @ConfigItem(
            keyName = "reallyLowEnabled",
            name = "Show really low warning",
            description = "Turn the really low prayer warning on or off",
            position = 0,
            section = reallyLowSection
    )
    default boolean reallyLowEnabled()
    {
        return true;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            keyName = "reallyLowThreshold",
            name = "At or below",
            description = "The text switches to the really low color when your prayer points are at or below this number",
            position = 1,
            section = reallyLowSection
    )
    default int reallyLowThreshold()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "reallyLowColor",
            name = "Color",
            description = "Color of the really low prayer warning",
            position = 2,
            section = reallyLowSection
    )
    default Color reallyLowColor()
    {
        return new Color(255, 140, 0);
    }

    @ConfigItem(
            keyName = "reallyLowHideEnabled",
            name = "Hide after delay",
            description = "Hide the really low warning a set time after your prayer drops into this range",
            position = 3,
            section = reallyLowSection
    )
    default boolean reallyLowHideEnabled()
    {
        return true;
    }

    @Range(min = 1, max = 300)
    @ConfigItem(
            keyName = "reallyLowHideSeconds",
            name = "Hide delay (seconds)",
            description = "How many seconds the really low warning stays on",
            position = 4,
            section = reallyLowSection
    )
    default int reallyLowHideSeconds()
    {
        return 10;
    }

    // ---------- Out of prayer ----------

    @ConfigItem(
            keyName = "outEnabled",
            name = "Show out of prayer warning",
            description = "Turn the out of prayer warning on or off",
            position = 0,
            section = outSection
    )
    default boolean outEnabled()
    {
        return true;
    }

    @Range(min = 0, max = 99)
    @ConfigItem(
            keyName = "outThreshold",
            name = "At or below",
            description = "Counts as out of prayer when your prayer points are at or below this number. The default of 0 means completely empty",
            position = 1,
            section = outSection
    )
    default int outThreshold()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "outColor",
            name = "Color",
            description = "Color of the out of prayer warning",
            position = 2,
            section = outSection
    )
    default Color outColor()
    {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "hideOutOfCombat",
            name = "Hide when out of combat",
            description = "While in combat the out of prayer text stays on. When you are not in combat it disappears after the delay below, so it is not over your head while you run around with no prayer",
            position = 3,
            section = outSection
    )
    default boolean hideOutOfCombat()
    {
        return true;
    }

    @Range(min = 1, max = 300)
    @ConfigItem(
            keyName = "outOfCombatSeconds",
            name = "Hide delay (seconds)",
            description = "How many seconds the out of prayer text stays on after your prayer runs out or your combat ends",
            position = 4,
            section = outSection
    )
    default int outOfCombatSeconds()
    {
        return 10;
    }

    // ---------- Combat ----------

    @Range(min = 1, max = 60)
    @ConfigItem(
            keyName = "combatSeconds",
            name = "Combat timeout (seconds)",
            description = "You count as in combat for this many seconds after you deal or take a hit",
            position = 0,
            section = combatSection
    )
    default int combatSeconds()
    {
        return 10;
    }
}