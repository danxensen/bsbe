(ns bsbe.game)

(defn new-game
  "creates a new game state"
  []
  {})

(defn get-inputs
  "gets the new inputs"
  [input-buffer]
  ; input-buffer is the shared-inputs atom,
  ; it will contain a collection of inputs to be processed this frame
  (let [inputs @input-buffer] ; grab the inputs
    (swap! input-buffer (fn [_] [])) ; clear the buffer
    inputs))

(defn process-state
  "transforms the game state based on previous state and inputs"
  [state inputs]
  {})

(defn continue?
  "determines if the game should continue running"
  [state]
  ; if the game is finished, process should leave a nil state
  (not (nil? state)))

(defn run-game
  "runs the core game loop"
  [shared-state shared-inputs]
  (loop [state (new-game)] ; start with a new game state
    (when (continue? state) ; check if we should continue processing
      (let [input (get-inputs shared-inputs) ; get inputs
            next-state (process-state state input)] ; process state
        (swap! shared-state (fn [_] next-state)) ; update shared-state
        ; ! time process for target frames-per-second ??
        (recur next-state))))) ; recur loop with new state

