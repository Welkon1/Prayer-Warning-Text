# Prayer Warning Text

Color-coded text over your character as your prayer runs low.

## Features

- **Low prayer warning:** text appears once your prayer drops to a threshold you choose (off-white by default).
- **Really low warning:** a second threshold switches the text to orange.
- **Out of prayer:** when your prayer runs out the text turns red. It stays on while you are in combat. When you are not in combat it clears itself after a delay you choose, so it is not sitting over your head while you run around with no prayer.
- **Hide delays:** the low and really low warnings can also hide themselves after a delay you choose.
- **Custom text:** use `{points}` to show your current prayer points, e.g. `LOW PRAYER: {points}`. The out-of-prayer message can be different.
- **Everything is adjustable:** every warning, threshold, color, timer and the text height can be changed or turned off in the plugin's settings.
- **Optional:** only show the low and really low warnings while a prayer is active.
- **Reappears when you pray:** turning a prayer on brings the warning back and restarts its hide timer.

## Notes

- "In combat" means you dealt or took a hit within the combat timeout (10 seconds by default).
- This plugin only draws text. It does not click, type, or change anything in the game.