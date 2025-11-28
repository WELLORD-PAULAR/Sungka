## Game Rules (updated)

This README includes the updated gameplay rules and UI notes so players know the current behavior implemented in the app.

- Click one of your pits to sow shells. Houses are not playable.
- If a pit you own contains a power-up, clicking it will attempt to activate that power-up instead of sowing.
- Power-up codes: D(DoubleCapture), B(BonusTurn), R(Reverse), M(Magnet), S(StealShells), P(PitShield), A(AddShells), W(SwapHouses), K(SkipOpp), L(LuckyDrop).
- Activating a power-up does NOT consume your turn.
- Each player may activate up to 2 power-ups per turn. When a player reaches this limit, ALL power-ups on that player's side are immediately wiped (cleared) and the player must select a sowing pit.
- When you capture an opponent pit that contains a power-up, the captured power-up is immediately activated for you if you have remaining activations; otherwise it will be stored on your side if space exists or discarded.
- PitShield protects a pit for 2 turns from capture/theft/refill; a protected pit is shown with a red border and protection is removed when it blocks a capture.
- At the end of a player's turn, power-ups on their side are cleared and then refilled up to the cap (default 3), skipping any protected pit.
- The UI shows active power-ups and remaining per-turn counters near the top controls.
- The AI animates moves, uses lookahead search, and performs lightweight online learning to adapt during a match.

For more details open the in-game Help (`Show Help`).
