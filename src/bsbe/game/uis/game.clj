(ns bsbe.game.uis.game
  (:use [bsbe.game.uis.core]
        [bsbe.game.command]))

(defn process-inputs
  "apply the changes to the state for each input received"
  [state inputs]
  (if (empty? inputs)
    state ; no more inputs to process
    (let [input (first inputs) ; process inputs one at a time
          partial-input (:partial-input state)
          new-state ; next state made based on the input
          (case input
            ; backspace: take out the last input
            \backspace (update-in state [:partial-input] (comp vec butlast))
            ; enter: move partial-input to input so it can be processed
            \return (-> state
                        (assoc-in [:input] (apply str partial-input))
                        (assoc-in [:partial-input] []))
            ; otherwise: add input to partial-input
            (update-in state [:partial-input] conj input))]
      ; process rest of the inputs
      (recur new-state (rest inputs)))))

(defn process-state
  "transforms the game state based on previous state and inputs"
  [state inputs]
  (let [state (process-inputs state inputs)
        input-available (not (empty? (:input state)))
        processed-state (if input-available
                          (process-command state)
                          state)
        animated-state (animate-message processed-state)]
    animated-state))

