(ns bsbe.game.core
  (:use [bsbe.game.command]
        [bsbe.game.dictionary]
        [bsbe.game.areas.hotelroom]))

(defn new-game
  "creates a new game state"
  []
  {:partial-input [] ; the input the player is still typing
   :input "" ; the input after player hits enter
   :message "" ; the message to display on the screen
   :animated-message "" ; the partially-animated portion of the message
   :animated-index 0 ; the animation index
   :state {} ; any global state values
   :areas {:hotelroom (hotelroom)} ; all the areas in the game
   :location :hotelroom ; the current area id
   :inventory {} ; player's inventory of entities
   :dictionary (make-dictionary) ; dictionary shared throughout game
   :unknown-command (fn [state]
                      (assoc-in state [:message] (str "You don't think you can " (:input state) ", maybe you didn't form that idea properly?")))
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
            \return (-> state
                        (assoc-in [:input] (apply str partial-input))
                        (assoc-in [:partial-input] []))
            ; otherwise: add input to partial-input
            (update-in state [:partial-input] conj input))]
      ; process rest of the inputs
      (recur new-state (rest inputs)))))

(defn animate-message
  "animates a message being written one character at a time"
  [state]
  ; must continually re-grab animated-message and -index from state
  ; because we make a few changes to state to update them
  (let [message (:message state)
        message-changed? (not= message (:current-message state))
        ; check if the animation needs to be restarted
        state (if message-changed?
                (-> state
                    (assoc-in [:animated-message] "")
                    (assoc-in [:current-message] message)
                    (assoc-in [:animated-index] 0))
                state)
        ; increment how much of the message is displayed
        index-low? (< (:animated-index state) (count (:current-message state)))
        state (if index-low?
                (update-in state [:animated-index] inc)
                state)
        ; take first x out of the message
        new-message (take (:animated-index state) (:current-message state))
        state (assoc-in state [:animated-message] new-message)]
    state))

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

