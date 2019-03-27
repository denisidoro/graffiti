(ns graffiti.game-test
  (:require [graffiti.core :as g]
            [graffiti.specs :as specs]
            [graffiti.db :as db]
            [matcher-combinators.test]
            [clojure.test :as t]
            [selvage.test.flow :refer [*world* defflow]]))

;; helpers

(defn create-system!
  [options world]
  (assoc world :database (db/new-database)
               :lacinia/mesh (g/compile options)))

(defn graphql
  [world & args]
  (apply g/graphql (:lacinia/mesh world) args))

(defn eql
  [world & args]
  (apply g/eql (:lacinia/mesh world) args))

;; resolvers

(g/defresolver game-resolver
  [ctx {:game/keys [id]}]
  {:input  #{:game/id}
   :output [:game/id :game/name {:game/designers [:designer/id]}]}
  (db/get-game id))

(g/defmutation send-message
  [ctx {:keys [message/text]}]
  {:params [:message/text]
   :output [:message/id :message/text]}
  {:message/id   123
   :message/text text})

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
    :Designer specs/designer
    :Message  specs/message}

   :lacinia/queries
   {:game     {:type   :Game
               :input  #{:game/id}
               :params #{}}                                 ; TODO: handle :params
    :mainGame {:type   :Game
               :output :game/main}}

   :lacinia/mutations
   {:sendMessage {:type     :Message
                  :mutation send-message}}

   :pathom/resolvers
   [game-resolver designer-resolver maingame-resolver]})

;; query

(defflow my-first-test

  (partial create-system! options)

  (t/testing "simple GraphQl query"
    (t/is
      (match? (graphql *world* "query ($id: String!) { game(id: $id) { id name designers { id fullName games { name }}}}" {:id "1234"})
              {:data {:game {:id        "1234"
                             :name      "Uncharted"
                             :designers [{:id       "4567"
                                          :fullName "John"
                                          :games    [{:name "Uncharted"}]}]}}})))

  (t/testing "simple EQL query"
    (t/is
      (match? (eql *world* [{[:game/id "1234"]
                             [:game/id
                              :game/name {:game/designers [:designer/id
                                                           :designer/full-name
                                                           {:designer/games [:game/name]}]}]}])
              {[:game/id "1234"] #:game{:id        "1234"
                                        :name      "Uncharted"
                                        :designers [#:designer{:id        "4567"
                                                               :full-name "John"
                                                               :games     [#:game{:name "Uncharted"}]}]}})))

  (t/testing "resolver without input"
    (t/is
      (match? (graphql *world* "{ mainGame { name }}")
              {:data {:mainGame {:name "Uncharted"}}})))

  (t/testing "simple GraphQL mutation"
    (t/is
      (match? (graphql *world* "mutation { sendMessage(text: \"hello\") { text }}")
              {:data {:sendMessage {:text "hello"}}}))))
