(ns graffiti.book-test
  (:require [clojure.spec.alpha :as s]
            [graffiti.core :as g]
            [graffiti.db :as db]
            [clojure.test :as t]))

;; specs

(s/def :book/id string?)
(s/def :book/title string?)
(s/def :book/entity (s/keys :opt [:book/id :book/title]))

;; resolvers

(g/defresolver book
  [ctx {:book/keys [id]}]
  {:input  #{:book/id}
   :output [:book/id :book/title]}
  (db/get-book id))

;; setup

(def ^:const options
  {:lacinia/objects
   {:Book :book/entity}

   :lacinia/queries
   {:book {:resolver book
           :type     :Book}}})

(def mesh (g/compile options))

;; query

(t/deftest graphql-query
  (t/is
    (= (g/graphql mesh "{ book(id: \"1234\") { id title }}")
       {:data {:book {:id    "1234"
                      :title "The Great Gatsby"}}})))

(t/deftest eql-query
  (t/is
    (= (g/eql mesh [{[:book/id "1234"]
                     [:book/id
                      :book/title]}])
       {[:book/id "1234"] #:book{:id    "1234"
                                 :title "The Great Gatsby"}})))
