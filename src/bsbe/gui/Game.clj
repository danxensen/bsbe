(ns bsbe.gui.Game
  (:gen-class :name bsbe.gui.Game
              :extends com.badlogic.gdx.Game
              :state state
              :init init-game
              :constructors {[clojure.lang.Atom clojure.lang.Atom] []})
  (:import [com.badlogic.gdx Game]))

(defn -init-game
  "inits a Game instance"
  [shared-state shared-inputs]
  ; pass nothing to Game constructor; shared data to state
  [[] {:state shared-state :inputs shared-inputs}])

(defn -create [^Game this]
  ; set up screen, input listener
  )

