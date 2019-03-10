(ns graffiti.game-test
  (:require [clojure.spec.alpha :as s]
            [graffiti.core :as graffiti]
            [graffiti.query :as query]
            [com.wsscode.pathom.connect :as pc]
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

(pc/defresolver game
  [env {:game/keys [id]}]
  {::pc/input  #{:game/id}
   ::pc/output [:game/id :game/name {:game/designers [:designer/id]}]}
  (db/get-game id))

(pc/defresolver designer
  [env {:designer/keys [id]}]
  {::pc/input  #{:designer/id}
   ::pc/output [:designer/id :designer/name :designer/games]}
  (db/get-designer id))

;; schemas

(def objects
  {:Game     :game/entity
   :Designer :designer/entity})

(def queries
  {:game
   {:type     :Game
    :resolver game}})

;; setup

(def mesh
  (graffiti/compile
    {:lacinia/objects        objects
     :lacinia/queries        queries
     :pathom/extra-resolvers [designer]}))

;; query

(t/is
  (= (query/graphql mesh "{ game(id: \"1234\") { id name designers { name games { name }}}}")
     {:data {:game {:id        "1234"
                    :name      "Zertz"
                    :designers [{:name  "John"
                                 :games [{:name "Zertz"}]}]}}}))

(t/is
  (= (query/eql mesh [{[:game/id "1234"]
                       [:game/id
                        :game/name {:game/designers [:designer/id
                                                     :designer/name {:designer/games [:game/name]}]}]}])
     {[:game/id "1234"] #:game{:id        "1234"
                               :name      "Zertz"
                               :designers [#:designer{:id    "4567"
                                                      :name  "John"
                                                      :games [#:game{:name "Zertz"}]}]}}))
