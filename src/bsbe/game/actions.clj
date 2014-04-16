(ns bsbe.game.actions)

; actions are functions used in command bodies
; they are used to easily effect changes on the game state
; they return functions of type state -> state

(defn message
  "appends to the next message in the game"
  [text]
  (fn [state] (assoc-in state [:message] text)))

(defn flag
  "sets a flag or other data variable in the game"
  [key value]
  (fn [state] (assoc-in state [:flags key] value)))
