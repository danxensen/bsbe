(ns bsbe.game.util
  (:use [bsbe.game.command]))

; (defentity thing (make-commands) state)
(defmacro defentity
  "defines an entity"
  ([name commands]
     `(defentity ~name ~commands {}))
  ([name commands state]
     `(defn ~name []
        {:id (keyword '~name) :commands ~commands :state ~state})))

; (defarea place "description" [entities] (make-commands) state)
(defmacro defarea
  "defines an area"
  ([name description entities commands]
     `(defarea ~name ~description ~entities ~commands {}))
  ([name description entities commands state]
     ; evaluate the entity fns to create the entity list
     (let [entities (zipmap (map keyword entities) (map list entities))]
       `(defn ~name []
          {:id (keyword '~name) :description ~description :entities ~entities :commands ~commands :state ~state}))))
