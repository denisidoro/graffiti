(ns graffiti.db
  (:require [quark.collection.ns :as ns]
            [quark.collection.map :as map]))

(defn indexed
  [coll]
  (reduce #(assoc %1 (-> %2 ns/unnamespaced :id) %2) {} coll))

(def db
  (map/map-vals
    indexed
    {:books
     #{#:book{:id    "1234"
              :title "The Great Gatsby"}}

     :games
     #{#:game{:id        "1234"
              :name      "Uncharted"
              :designers [{:designer/id "4567"}]}}

     :designers
     #{#:designer{:id        "4567"
                  :full-name "John"
                  :games     [{:game/id "1234"}]}}}))

(defn get-entity
  ([entity id]
   (get-entity entity nil id))
  ([entity database id]
   (get-in (or (some-> database deref) db) [entity id])))

(def get-book (partial get-entity :books))
(def get-game (partial get-entity :games))
(def get-designer (partial get-entity :designers))

(defn new-database
  []
  (atom db))
