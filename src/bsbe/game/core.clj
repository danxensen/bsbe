(ns bsbe.game.core
  (:use [bsbe.game.command]
        [bsbe.game.dictionary]
        [bsbe.game.areas.hotelroom])
  (:require [bsbe.game.uis [title :as title]
                           [game :as game]]))

(defn new-game
  "creates a new game state"
  []
  {:current-ui :title ; the current ui to use
   :uis {:title title/process-state
         :game game/process-state} ; all the uis in the game
   :partial-input [] ; the input the player is still typing
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

(defn process-state
  "processes the state and inputs"
  [{:keys [current-ui uis] :as state} inputs]
  ; get the current ui and execute it
  ((get uis current-ui) state inputs))

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

