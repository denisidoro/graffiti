(ns graffiti.specs
  (:require [clojure.spec.alpha :as s]
            [graffiti.spec :as gs]))

(declare game)

(s/def :book/id string?)
(s/def :book/title string?)
(s/def :book/entity (s/keys :opt [:book/id :book/title]))

(gs/defentity designer
  {:designer/id    string?
   :designer/name  string?
   :designer/games (s/coll-of game)})

(gs/defentity game
  {:game/id        string?
   :game/name      string?
   :game/designers (s/coll-of designer)})
