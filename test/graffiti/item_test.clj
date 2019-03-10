(ns graffiti.item-test
  (:require [clojure.spec.alpha :as s]
            [graffiti.core :as graffiti]
            [graffiti.query :as query]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.db :as db]
            [clojure.test :as t]))

;; specs

(s/def :item/id string?)
(s/def :item/name string?)
(s/def :item/entity (s/keys :opt [:item/id :item/name]))

;; resolvers

(pc/defresolver item
  [env {:item/keys [id]}]
  {::pc/input  #{:item/id}
   ::pc/output [:item/id :item/name]}
  (db/get-item id))

;; schemas

(def objects
  {:Item :item/entity})

(def queries
  {:item
   {:type     :Item
    :resolver item}})

;; setup

(def mesh
  (graffiti/compile
    {:lacinia/objects objects
     :lacinia/queries queries}))

;; query

(t/is
  (= (query/graphql mesh "{ item(id: \"1234\") { id name }}")
     {:data {:item {:id   "1234"
                    :name "Zertz"}}}))

(t/is
  (= (query/eql mesh [{[:item/id "1234"]
                       [:item/id
                        :item/name]}])
     {[:item/id "1234"] #:item{:id   "1234"
                               :name "Zertz"}}))
