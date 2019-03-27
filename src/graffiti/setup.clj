(ns graffiti.setup
  (:require [quark.collection.map :as map]
            [graffiti.resolver :as resolver]
            [graffiti.schema :as schema]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.keyword :as keyword]
            [graffiti.mutation :as mutation]))

(defn ^:private gen-objects
  [{:lacinia/keys [objects] :as options}]
  (->> objects
       (map/map-vals (fn [k] {:fields (schema/lacinia-fields options k)}))))

(defn ^:private gen-queries
  [{:lacinia/keys [queries] :as options}]
  (map/map-vals #(schema/lacinia-query options %) queries))

(defn ^:private gen-mutations-schema
  [{:lacinia/keys [mutations] :as options}]
  (map/map-vals #(schema/lacinia-mutation options %) mutations))

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

(defn gen-raw-schema
  [options]
  {:objects   (gen-objects options)
   :queries   (gen-queries options)
   :mutations (gen-mutations-schema options)})
