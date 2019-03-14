(ns graffiti.setup
  (:require [quark.collection.map :as map]
            [graffiti.resolver :as resolver]
            [clojure.set :as set]
            [graffiti.schema :as schema]
            [graffiti.keyword :as keyword]))

(defn ^:private gen-objects
  [{:lacinia/keys [objects] :as options}]
  (->> objects
       (map/map-vals (fn [k] {:fields (schema/lacinia-fields options k)}))))

(defn ^:private gen-queries
  [{:lacinia/keys [queries] :as options}]
  (map/map-vals #(schema/lacinia-query options %) queries))

(defn ^:private gen-resolver
  [parser
   resolver-name
   {:keys [input output type]}]
  [(keyword/from-type+input type input)
   (resolver/pathom input output parser)])

(defn gen-resolvers
  [{:lacinia/keys [queries]}
   parser]
  (->> queries
       (map (fn [[k v]] (gen-resolver parser k v)))
       (into {})))

(defn gen-raw-schema
  [options]
  {:objects (gen-objects options)
   :queries (gen-queries options)})
