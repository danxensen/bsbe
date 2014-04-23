(ns bsbe.gui.uis.game
  (:import [com.badlogic.gdx.scenes.scene2d.ui Label])
  (:use [bsbe.gui.uis.core]))

(defn render
  "renders the game state"
  [stage state]
  (let [partial-input (Label. (apply str ">" (:partial-input state)) @style)
        message (:animated-message state)]
    (.addActor @stage partial-input)
    (format-message message stage)))
