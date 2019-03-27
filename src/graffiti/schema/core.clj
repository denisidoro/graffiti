(ns graffiti.schema.core
  (:require [quark.collection.map :as map]
            [graffiti.schema.field :as schema.field]))

(defn ^:private gen-objects
  [{:lacinia/keys [objects] :as options}]
  (->> objects
       (map/map-vals (fn [k] {:fields (schema.field/lacinia-fields options k)}))))

(defn ^:private gen-queries
  [{:lacinia/keys [queries] :as options}]
  (map/map-vals #(schema.field/lacinia-query options %) queries))

(defn ^:private gen-mutations-schema
  [{:lacinia/keys [mutations] :as options}]
  (map/map-vals #(schema.field/lacinia-mutation options %) mutations))

(defn spec->graphql
  [options]
  {:objects   (gen-objects options)
   :queries   (gen-queries options)
   :mutations (gen-mutations-schema options)})
