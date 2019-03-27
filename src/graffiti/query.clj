(ns graffiti.query
  (:require [com.walmartlabs.lacinia :as lacinia]
            [graffiti.interceptors :as interceptors]
            [clojure.core.async :as async]))

(defn graphql
  [{:graffiti/keys [mesh] :as context} query variables]
  (-> (lacinia/execute (:lacinia/schema mesh) query variables context)
      interceptors/simplify))

(defn eql
  [{:pathom/keys [parser]}
   query]
  (async/<!! (parser {} query)))
