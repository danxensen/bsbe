(ns bsbe.gui.Game
  (:gen-class :name bsbe.gui.Game
              :extends com.badlogic.gdx.Game
              :state state
              :init init-game
              :constructors {[clojure.lang.Atom clojure.lang.Atom] []})
  (:import [com.badlogic.gdx Game Gdx Screen InputProcessor Input$Keys]
           [com.badlogic.gdx.graphics GL20 Color]
           [com.badlogic.gdx.graphics.g2d BitmapFont]
           [com.badlogic.gdx.scenes.scene2d Stage]
           [com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle]
           [com.badlogic.gdx.backends.lwjgl LwjglFiles]))

(def style
  (delay ; delay because creating LabelStyle at compile time fails
   (Label$LabelStyle.
    (BitmapFont. (.internal (LwjglFiles.) "unifont-32.fnt") false)
    (Color. 1 1 1 1))))

(defn clear-screen
  "clears the screen"
  []
  (.glClearColor (Gdx/gl) 0 0 0 0)
  (.glClear (Gdx/gl) GL20/GL_COLOR_BUFFER_BIT))

(defn render
  "renders the game state"
  [stage state]
  (let [partial-input (Label. (apply str (:partial-input state)) @style)
        input (Label. (:input state) @style)]
    (.setY input 500)
    (.addActor @stage partial-input)
    (.addActor @stage input)))

(defn make-screen
  "creates the screen for displaying stuff"
  [state]
  (let [stage (atom nil)]
    (proxy [Screen] []
      (resize [w h])
      (show [])
      (hide [])
      (render [delta]
        (clear-screen)
        (reset! stage (Stage.))
        ; render the game state
        (render stage @state)
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
    (keyDown [keycode]
      (when (= keycode Input$Keys/ENTER)
        (swap! inputs #(conj % \newline)))
      true)
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
  [[] {:state shared-state :inputs shared-inputs}])

(defn -create [^Game this]
  ; set up screen, input listener
  (.setScreen this (make-screen (:state (.state this))))
  (.setInputProcessor Gdx/input (make-input-listener (:inputs (.state this)))))
