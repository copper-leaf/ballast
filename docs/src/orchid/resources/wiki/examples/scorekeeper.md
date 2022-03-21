---
extraJs:
  - 'assets/example/distributions/compose-web.js'
---

# {{ page.title }}

The ScoreKeeper is a more complex version of a simple counter. It allows one to add/remove names from a list, and 
change the score of each player individually. 

How to use:

- Use the form field to enter one or more player names. Names must be unique. Players can be removed from the game by
  clicking the "X" button on their card.
- Click on a player card to select or deselect that player. Hit the numbered buttons below the list to increase/decrease 
  the score of all selected players' scores by that amount. 
- Scores are set temporarily to help you see how much you are adding in a single "move". After 5 seconds, the temporary
  scores will be "committed" and their total values updated accordingly. Alternatively, you may click on an individual
  player's score to commit it immediately.
- Player scores will be saved to your browser's LocalStorage with every change, and restored when reloading this page
  (TODO: implement a [SavedState module][#3] to have this done automatically)

<div id="scorekeeper"></div>
<br><br>

_Pro Tip: Open your {{ 'Ballast Debugger' | anchor }} with this page open to see all Ballast activity in real-time, or
just read the browser's Console logs._

[#3]: https://github.com/copper-leaf/ballast/issues/3
