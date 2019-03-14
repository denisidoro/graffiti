(ns graffiti.book-test
  (:require [graffiti.core :as g]
            [graffiti.db :as db]
            [graffiti.specs :as specs]
            [matcher-combinators.test]
            [clojure.test :as t]))

;; resolvers

(g/defresolver book
  [ctx {:book/keys [id]}]
  {:input  #{:book/id}
   :output [:book/id :book/title]}
  (db/get-book id))

;; setup

(def ^:const options
  {:lacinia/objects
   {:Book specs/book}

   :lacinia/queries
   {:book {:input #{:book/id}
           :type  :Book}}

   :pathom/resolvers
   [book]})

(def mesh (g/compile options))

;; query

(t/deftest graphql-query
  (t/is
    (match? (g/graphql mesh "{ book(id: \"1234\") { id title }}")
            {:data {:book {:id    "1234"
                           :title "The Great Gatsby"}}})))

(t/deftest eql-query
  (t/is
    (match? (g/eql mesh [{[:book/id "1234"]
                          [:book/id
                           :book/title]}])
            {[:book/id "1234"] #:book{:id    "1234"
                                      :title "The Great Gatsby"}})))
