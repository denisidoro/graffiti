(ns graffiti.setup
  (:require [graffiti.resolver :as resolver]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.keyword :as keyword]
            [graffiti.mutation :as mutation]
            [clojure.set :as set]))

(defn ^:private gen-resolver
  [parser
   {:keys [input output type]}]
  [(keyword/from-type+input type input)
   (resolver/pathom input output parser)])

;; TODO
(defn ^:private gen-mutation
  [parser
   {:keys [mutation type]}]
  nil
  (let [params (::pc/params mutation)]
    [(keyword/from-type+input type params)
     (mutation/pathom mutation parser)]))

(defn gen-resolvers
  [{:lacinia/keys [queries]}
   parser]
  (->> queries
       (map (fn [[k v]] (gen-resolver parser v)))
       (into {})))

(defn gen-mutations
  [{:lacinia/keys [mutations]}
   parser]
  (->> mutations
       (map (fn [[k v]] (gen-mutation parser v)))
       (into {})))

(defn gen-resolvers+mutations
  [options parser]
  (merge (gen-resolvers options parser)
         (gen-mutations options parser)))

(defn enhance-options
  [{:lacinia/keys [objects] :as options}]
  (merge {:graffiti/eql-conformer     keyword/eql
          :graffiti/graphql-conformer keyword/graphql
          :lacinia/inverted-objects   (set/map-invert objects)}
         options))
