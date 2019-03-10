(ns graffiti.core
  (:refer-clojure :exclude [compile])
  (:require [com.walmartlabs.lacinia.util :as util]
            [quark.collection.map :as map]
            [graffiti.resolver :as resolver]
            [clojure.set :as set]
            [graffiti.schema :as schema]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [graffiti.keyword :as keyword]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]))

(defn ^:private gen-objects
  [object-map]
  (->> object-map
       set/map-invert
       (map/map-vals (fn [k] {:fields (schema/lacinia-fields object-map k)}))))

(defn ^:private gen-queries
  [object-map queries]
  (map/map-vals #(schema/lacinia-query object-map %) queries))

(defn ^:private gen-resolvers
  [query-map parser]
  (->> query-map
       vals
       (map :resolver)
       (map (fn [r] [(keyword/from-resolver r) (resolver/pathom r parser)]))
       (into {})))

(defn ^:private gen-raw-schema
  [{:lacinia/keys [objects queries]}]
  (let [object-map (set/map-invert objects)]
    {:objects (gen-objects object-map)
     :queries (gen-queries object-map queries)}))

(defn compile
  [{:lacinia/keys [queries]
    :pathom/keys  [extra-resolvers]
    :as           options}]
  (let [pathom-resolvers   (concat (->> queries vals (mapv :resolver)) extra-resolvers)
        pathom-readers     [p/map-reader
                            pc/parallel-reader
                            pc/open-ident-reader
                            p/env-placeholder-reader]
        pathom-plugins     [(pc/connect-plugin {::pc/register pathom-resolvers})
                            p/error-handler-plugin
                            p/trace-plugin]
        pathom-parser      (p/parallel-parser
                             {::p/env     {::p/reader               pathom-readers
                                           ::p/placeholder-prefixes #{">"}}
                              ::p/mutate  pc/mutate-async
                              ::p/plugins pathom-plugins})
        lacinia-raw-schema (gen-raw-schema options)
        lacinia-resolvers  (gen-resolvers queries pathom-parser)
        lacinia-schema     (-> lacinia-raw-schema
                               (util/attach-resolvers lacinia-resolvers)
                               lacinia.schema/compile)]
    {:pathom/parser  pathom-parser
     :lacinia/schema lacinia-schema}))

