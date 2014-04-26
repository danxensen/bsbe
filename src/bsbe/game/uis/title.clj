(ns bsbe.game.uis.title)

(defn process-state
  "processes the title game state"
  [state inputs]
  (if (empty? inputs)
    state
    (let [input (first inputs)]
      (case input
        ; start the game when the user presses enter and execute look command
        \return (let [state (assoc-in state [:current-ui] :game)
                      ;state (assoc-in state [:input] "look")
                      ]
                  state)
        (recur state (rest inputs))))))
