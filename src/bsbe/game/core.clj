(ns bsbe.game.core)

(defn new-game
  "creates a new game state"
  []
  {:partial-input [] ; holds the input the player is still typing
   :input "" ; holds the input after player hits enter
   })

(defn get-inputs
  "gets the new inputs"
  [input-buffer]
  ; input-buffer is the shared-inputs atom,
  ; it will contain a collection of inputs to be processed this frame
  (let [inputs @input-buffer] ; grab the inputs
    (swap! input-buffer (fn [_] [])) ; clear the buffer
    inputs))

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
            \newline (-> state
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
        input-available (not (empty? (:input state)))]
    (if input-available
      (process-command state)
      state)))

(defn continue?
  "determines if the game should continue running"
  [state]
  ; if the game is finished, process should leave a nil state
  (not (nil? state)))

(defn take-a-break
  "sleeps thread until the next frame should be processed"
  []
  (Thread/sleep (/ 1000 60)))

(defn run-game
  "runs the core game loop"
  [shared-state shared-inputs]
  (loop [state (new-game)] ; start with a new game state
    (when (continue? state) ; check if we should continue processing
      (let [input (get-inputs shared-inputs) ; get inputs
            next-state (process-state state input)] ; process state
        (swap! shared-state (fn [_] next-state)) ; update shared-state
        (take-a-break)
        (recur next-state))))) ; recur loop with new state

