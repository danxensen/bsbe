(ns bsbe.core
  (:gen-class)
  (:use [bsbe.game.core :only [run-game]]
        [bsbe.gui.core :only [run-gui]]))

(defn -main
  "starts the game"
  [& args]
  ; create the buffers shared by the main game process and the gui
  (let [shared-state (atom nil)
        shared-inputs (atom nil)]
    ; start the gui and the game with access to the buffers
    (run-gui shared-state shared-inputs "Blue Skies, Blue Eyes" 800 600)
    ; start game in separate thread; when it finishes shutdown thread agents
    @(future (run-game shared-state shared-inputs))
    (shutdown-agents)))
