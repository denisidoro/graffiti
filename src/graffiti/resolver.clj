(ns graffiti.resolver
  (:require [com.walmartlabs.lacinia.executor :as ex]
            [quark.collection.ns :as ns]
            [graffiti.eql :as eql]
            [graffiti.query :as query]
            [com.wsscode.pathom.connect :as pc]
            [clojure.set :as set]
            [clojure.walk :as walk]
            [graffiti.keyword :as keyword]))

(defn ident
  [input args]
  (let [input' (first input)]
    [input' (get args (ns/unnamespaced input'))]))

(defn graphql-keywords
  [m]
  (walk/postwalk
    (fn [x]
      (if (keyword? x)
        (keyword/graphql x)
        x))
    m))

(defn pathom
  [resolver-name input parser]
  (fn [{:graffiti/keys [mesh] :as context}
       args
       value]
    (->> (query/eql
           {:pathom/parser parser}
           [{(ident input args)
             (-> context ex/selections-tree (eql/from-selection-tree mesh))}])
         vals
         (map eql/as-tree)
         (reduce merge)
         graphql-keywords)))

(defn conform-config
  [config]
  (set/rename-keys
    config
    {:input  ::pc/input
     :output ::pc/output}))
