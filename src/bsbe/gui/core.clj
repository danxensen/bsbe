(ns bsbe.gui.core
  (:import [com.badlogic.gdx Game]
           [com.badlogic.gdx.backends.lwjgl LwjglApplication]))

(defn continue?
  "determines if the gui should continue running"
  [state]
  ; if the game is finished, process should leave a nil state
  (not (nil? state)))

(defn run-gui
  "runs the gui application"
  [shared-state shared-inputs window-title window-width window-height]
  ; create the game application
  (let [game (bsbe.gui.Game. shared-state shared-inputs)
        app (LwjglApplication. game window-title window-width window-height true)]
    ; close the app when state processing should no longer continue
    ; using a watch because i can't find a good way to hook this up
    (add-watch shared-state nil
               (fn [key ref old new]
                 (when-not (continue? new)
                   (.stop app))))))
