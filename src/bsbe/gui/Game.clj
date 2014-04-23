(ns bsbe.gui.Game
  (:gen-class :name bsbe.gui.Game
              :extends com.badlogic.gdx.Game
              :state state
              :init init-game
              :constructors {[clojure.lang.Atom clojure.lang.Atom] []})
  (:import [com.badlogic.gdx Game Gdx Screen InputProcessor]
           [com.badlogic.gdx.graphics GL20]
           [com.badlogic.gdx.scenes.scene2d Stage])
  (:require [bsbe.gui.uis [title :as title]
                          [game :as game]]))

(defn clear-screen
  "clears the screen"
  []
  (.glClearColor (Gdx/gl) 0 0 0 0)
  (.glClear (Gdx/gl) GL20/GL_COLOR_BUFFER_BIT))

(defn render
  "renders the current ui"
  [stage {:keys [current-ui] :as state} uis]
  ; get the current ui and render it
  ((get uis current-ui) stage state))

(defn make-screen
  "creates the screen for displaying stuff"
  [state uis]
  (let [stage (atom nil)]
    (proxy [Screen] []
      (resize [w h])
      (show [])
      (hide [])
      (render [delta]
        (clear-screen)
        (reset! stage (Stage.))
        ; render the game state
        (render stage @state uis)
        (doto @stage
          (.act delta)
          (.draw)))
      (pause [])
      (dispose [])
      (resume []))))

(defn make-input-listener
  "makes a listener for input events"
  [inputs]
  (proxy [InputProcessor] []
    (keyDown [keycode] true)
    (keyUp [keycode] true)
    (keyTyped [character]
      (swap! inputs #(conj % character))
      true)
    (touchDown [x y pointer button] true)
    (touchUp [x y pointer button] true)
    (touchDragged [x y pointer] true)
    (mouseMoved [x y] true)
    (scrolled [amount] true)))

(defn -init-game
  "inits a Game instance"
  [shared-state shared-inputs]
  ; pass nothing to Game constructor; shared data to state
  [[] {:state shared-state
       :inputs shared-inputs
       :uis {:title title/render
             :game game/render}}])

(defn -create [^Game this]
  ; set up screen, input listener
  (let [state (.state this)]
    (.setScreen this (make-screen (:state state) (:uis state)))
    (.setInputProcessor Gdx/input (make-input-listener (:inputs state)))))
