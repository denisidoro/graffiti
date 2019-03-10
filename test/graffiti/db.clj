(ns graffiti.db
  (:require [quark.collection.ns :as ns]))

(defn indexed
  [coll]
  (reduce #(assoc %1 (-> %2 ns/unnamespaced :id) %2) {} coll))

(def items
  (indexed
    #{#:item{:id   "1234"
             :name "Zertz"}}))

(def games
  (indexed
    #{#:game{:id        "1234"
             :name      "Zertz"
             :designers [{:designer/id "4567"}]}}))

(def designers
  (indexed
    #{#:designer{:id    "4567"
                 :name  "John"
                 :games [{:game/id "1234"}]}}))

(def get-item (partial get items))
(def get-game (partial get games))
(def get-designer (partial get designers))

