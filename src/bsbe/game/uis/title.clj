(ns bsbe.game.uis.title)

(defn process-state
  "processes the title game state"
  [state inputs]
  (if (empty? inputs)
    state
    (let [input (first inputs)]
      (case input
        ; start the game when the user presses enter
        \return (assoc-in state [:current-ui] :game)
        (recur state (rest inputs))))))
