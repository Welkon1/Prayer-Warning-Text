# Prayer Warning Text

Shows a short warning above your character as your prayer runs low.

| Warning | Default threshold | Default color | Default text |
|---|---|---|---|
| Low prayer | 20 or below | White | LOW PRAYER |
| Really low | 10 or below | Orange | LOW PRAYER |
| Out of prayer | 0 | Red | NO PRAYER |

If more than one warning applies, only the most severe is shown.

## When to hide

The **When to hide** setting controls how warnings go away. In every mode, turning a prayer on brings the warning back and restarts its hide delay.

| Mode | Behavior |
|---|---|
| Stay on while praying or in combat (default) | Warnings stay on while a prayer is active or you're in combat, then hide after the delay. |
| Hide after delay only | Warnings hide after the delay regardless of prayer or combat. |
| Only while praying | Low and really low warnings show only while a prayer is active. |
| Hide in combat without prayer | All warnings hide while you fight with no prayer active. Otherwise it behaves like the default. |

## Notes

- Combat means you dealt or took a hit within the combat timeout (10 seconds by default).
- With percentages on, thresholds are a percent of your Prayer level rather than prayer points.
- The out-of-prayer hide delay has a 10 second minimum.
- Percentages, custom text size and blink are off by default.

## Showing your prayer points

Turn on **Show prayer points** to add your current points to the end of the text, like `LOW PRAYER 12`.

To put the number somewhere else, type `{points}` in the warning text. For example, `{points} PRAYER LEFT` shows as `12 PRAYER LEFT`. This works in both the warning text and the out-of-prayer text, and the number is always your real prayer points, even with percentages on.

The plugin only draws text. It doesn't click, type or change anything in the game.