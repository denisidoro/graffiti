(ns graffiti.setup
  (:require [quark.collection.map :as map]
            [graffiti.resolver :as resolver]
            [clojure.set :as set]
            [graffiti.schema :as schema]
            [graffiti.keyword :as keyword]))

(defn ^:private gen-objects
  [object-map]
  (->> object-map
       set/map-invert
       (map/map-vals (fn [k] {:fields (schema/lacinia-fields object-map k)}))))

(defn ^:private gen-queries
  [object-map queries]
  (map/map-vals #(schema/lacinia-query object-map %) queries))

(defn ^:private gen-resolver
  [parser
   resolver-name
   {:keys [input type]}]
  [(keyword/from-type+input type input)
   (resolver/pathom resolver-name input parser)])

(defn gen-resolvers
  [query-map parser]
  (->> query-map
       (map (fn [[k v]] (gen-resolver parser k v)))
       (into {})))

(defn gen-raw-schema
  [{:lacinia/keys [objects queries]}]
  (let [inverted-object-map (set/map-invert objects)]
    {:objects (gen-objects inverted-object-map)
     :queries (gen-queries inverted-object-map queries)}))
