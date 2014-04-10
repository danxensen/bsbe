(defproject bsbe "0.1.0-SNAPSHOT"
  :description "blue skies, blue eyes"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.badlogic.gdx/gdx "0.9.9-SNAPSHOT"]
                 [com.badlogic.gdx/gdx-backend-lwjgl "0.9.9-SNAPSHOT"]]
  :repositories [["libgdx"
                  "http://libgdx.badlogicgames.com/nightlies/maven/"]]
  :aot [bsbe.gui.Game]
  :main ^:skip-aot bsbe.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
