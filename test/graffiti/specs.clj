(ns graffiti.specs
  (:require [clojure.spec.alpha :as s]
            [graffiti.spec :as gs]))

(declare game)

(gs/defentity book
  {:book/id    string?
   :book/title string?})

(gs/defentity designer
  {:designer/id        string?
   :designer/full-name string?
   :designer/games     (s/coll-of game)})

(gs/defentity game
  {:game/id        string?
   :game/name      string?
   :game/designers (s/coll-of designer)})

(gs/defentity message
  {:message/id   string?
   :message/text string?})
