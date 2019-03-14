(ns graffiti.game-test
  (:require [graffiti.core :as g]
            [graffiti.specs :as specs]
            [graffiti.db :as db]
            [matcher-combinators.test]
            [clojure.test :as t]))

;; resolvers

(g/defresolver game-resolver
  [ctx {:game/keys [id]}]
  {:input  #{:game/id}
   :output [:game/id :game/name {:game/designers [:designer/id]}]}
  (db/get-game id))

(g/defresolver maingame-resolver
  [ctx {:game/keys [id]}]
  {:output [{:game/main [:game/id :game/name {:game/designers [:designer/id]}]}]}
  {:game/main (db/get-game "1234")})

(g/defresolver designer-resolver
  [ctx {:designer/keys [id]}]
  {:input  #{:designer/id}
   :output [:designer/id :designer/full-name :designer/games]}
  (db/get-designer id))

;; setup

(def ^:const options
  {:lacinia/objects
   {:Game     specs/game
    :Designer specs/designer}

   :lacinia/queries
   {:game     {:type  :Game
               :input #{:game/id}}
    :maingame {:type   :Game
               :output :game/main}}

   :pathom/resolvers
   [game-resolver designer-resolver maingame-resolver]})

(def mesh (g/compile options))

;; query

(g/eql mesh [{:game/main [:game/id]}])

(t/deftest graphql-simple-query
  (t/is
    (match? (g/graphql mesh "{ game(id: \"1234\") { id name designers { id fullName games { name }}}}")
            {:data {:game {:id        "1234"
                           :name      "Uncharted"
                           :designers [{:id       "4567"
                                        :fullName "John"
                                        :games    [{:name "Uncharted"}]}]}}})))

(t/deftest eql-simple-query
  (t/is
    (match? (g/eql mesh [{[:game/id "1234"]
                          [:game/id
                           :game/name {:game/designers [:designer/id
                                                        :designer/full-name
                                                        {:designer/games [:game/name]}]}]}])
            {[:game/id "1234"] #:game{:id        "1234"
                                      :name      "Uncharted"
                                      :designers [#:designer{:id        "4567"
                                                             :full-name "John"
                                                             :games     [#:game{:name "Uncharted"}]}]}})))

(t/deftest resolver-without-input
  (t/is
    (match? (g/graphql mesh "{ maingame { name }}")
            {:data {:maingame {:name "Uncharted"}}})))

