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
     (let [entities (vec (map list entities))]
       `(defn ~name []
          {:id (keyword '~name) :description ~description :entities ~entities :commands ~commands :state ~state}))))

(defn current-area
  "gets the current area"
  [{:keys [location areas]}]
  (get location areas))

(defn entity-at?
  "checks if an entity is at a location"
  [{:keys [areas inventory]} id area]
  (if (= area :inventory)
    (contains? inventory id)
    (contains? (get areas area) id)))

(defn move-entity
  "moves an entity from one place to another"
  [state id from to]
  (let [; inventory is in a different place than areas, so handle separately
        from-inventory? (= from :inventory)
        to-inventory? (= to :inventory)
        ; retrieve the entity data so we don't lose it
        entity (if from-inventory?
                 (get-in state [:inventory] id)
                 (get-in state [:areas from :entities id]))
        ; remove the entity from the 'from' location
        entity-removed (if from-inventory?
                         (dissoc state :inventory id)
                         (dissoc state :areas from :entities id))
        ; add the entity to the 'to' location
        entity-moved (if to-inventory?
                       (assoc-in state [:inventory id] entity)
                       (assoc-in state [:areas to :entities id]))]
    entity-moved))
