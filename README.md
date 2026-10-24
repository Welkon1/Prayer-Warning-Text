# Prayer Warning Text

Shows a short warning above your character when your prayer is getting low, so you notice before it runs out.

## Why

Most low prayer warnings give you one message that stays on screen until you fix it. I wanted warnings that get more urgent as prayer drops, and that go away on their own, so there's no text stuck over my head while I run around on purpose with no prayer.

## What it does

There are three warnings. Each has its own threshold, color and on/off switch.

| Warning | Default threshold | Default color |
|---|---|---|
| Low prayer | 20 or below | White |
| Really low | 10 or below | Orange |
| Out of prayer | 0 | Red |

The text stays on while you have a prayer active or you're in combat. Once both stop, it hides after a delay you choose. If you'd rather it never hide, you can turn the delays off.

A few extra options:

- Bring the warning back when you turn a prayer on.
- Hide all warnings while you're fighting with no prayer active.
- Only show the low and really low warnings while a prayer is on.
- Change the text, the height above your head, and how long you count as "in combat".

## Notes

- You count as in combat for 10 seconds (by default) after you deal or take a hit.
- The out-of-prayer hide delay can't go below 10 seconds.
- You can put `{points}` in the warning text to show your current prayer points, for example `LOW PRAYER: {points}`.
- The plugin only draws text. It doesn't click, type or change anything in the game.