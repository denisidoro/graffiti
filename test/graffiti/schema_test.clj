(ns graffiti.schema-test
  (:require [clojure.test :as t]
            [matcher-combinators.test]
            [graffiti.specs :as specs]
            [graffiti.setup :as setup]
            [graffiti.schema.core :as schema]))

(def options
  {:lacinia/objects
   {:Book specs/book}

   :lacinia/queries
   {:book {:input #{:book/id}
           :type  :Book}}})

(t/deftest second-test
  (t/is (match? {:objects {:Book {:fields {:id {:type '(non-null String)}, :title {:type '(non-null String)}}}},
                 :queries {:book {:type :Book, :resolve :Book/book_id, :args {:id {:type '(non-null String)}}}}}
                (-> options setup/enhance-options schema/spec->graphql))))

