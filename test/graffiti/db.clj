(ns graffiti.db
  (:require [quark.collection.ns :as ns]))

(defn indexed
  [coll]
  (reduce #(assoc %1 (-> %2 ns/unnamespaced :id) %2) {} coll))

(def books
  (indexed
    #{#:book{:id   "1234"
             :title "The Great Gatsby"}}))

(def games
  (indexed
    #{#:game{:id        "1234"
             :name      "Uncharted"
             :designers [{:designer/id "4567"}]}}))

(def designers
  (indexed
    #{#:designer{:id    "4567"
                 :name  "John"
                 :games [{:game/id "1234"}]}}))

(def get-book (partial get books))
(def get-game (partial get games))
(def get-designer (partial get designers))

