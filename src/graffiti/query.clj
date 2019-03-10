(ns graffiti.query
  (:require [com.walmartlabs.lacinia :as lacinia]
            [graffiti.interceptors :as interceptors]
            [clojure.core.async :as async]))

(defn graphql
  [{:lacinia/keys [schema]}
   query-string]
  (-> (lacinia/execute schema query-string nil nil)
      interceptors/simplify))

(defn eql
  [{:pathom/keys [parser]}
   query]
  (async/<!! (parser {} query)))
