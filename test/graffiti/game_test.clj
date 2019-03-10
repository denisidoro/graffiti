(ns graffiti.game-test
  (:require [clojure.spec.alpha :as s]
            [graffiti.core :as g]
            [graffiti.db :as db]
            [clojure.test :as t]))

;; specs

(s/def :game/id string?)
(s/def :game/name string?)
(s/def :game/designers (s/coll-of :designer/entity))
(s/def :game/entity (s/keys :opt [:game/id :game/name :game/designers]))

(s/def :designer/id string?)
(s/def :designer/name string?)
(s/def :designer/name string?)
(s/def :designer/games (s/coll-of :game/entity))
(s/def :designer/entity (s/keys :opt [:designer/id :designer/name :designer/games]))

;; resolvers

(g/defresolver game
  [ctx {:game/keys [id]}]
  {:input  #{:game/id}
   :output [:game/id :game/name {:game/designers [:designer/id]}]
   :spec   :game/entity}
  (db/get-game id))

(g/defresolver designer
  [ctx {:designer/keys [id]}]
  {:input  #{:designer/id}
   :output [:designer/id :designer/name :designer/games]}
  (db/get-designer id))

;; setup

(def ^:const options
  {:lacinia/objects
   {:Game     :game/entity
    :Designer :designer/entity}

   :lacinia/queries
   {:game game}

   :pathom/extra-resolvers
   [designer]})

(def mesh (g/compile options))

;; query

(t/is
  (= (g/graphql mesh "{ game(id: \"1234\") { id name designers { name games { name }}}}")
     {:data {:game {:id        "1234"
                    :name      "Uncharted"
                    :designers [{:name  "John"
                                 :games [{:name "Uncharted"}]}]}}}))

(t/is
  (= (g/eql mesh [{[:game/id "1234"]
                   [:game/id
                    :game/name {:game/designers [:designer/id
                                                 :designer/name {:designer/games [:game/name]}]}]}])
     {[:game/id "1234"] #:game{:id        "1234"
                               :name      "Uncharted"
                               :designers [#:designer{:id    "4567"
                                                      :name  "John"
                                                      :games [#:game{:name "Uncharted"}]}]}}))
