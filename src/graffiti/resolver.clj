(ns graffiti.resolver
  (:require [com.walmartlabs.lacinia.executor :as ex]
            [quark.collection.ns :as ns]
            [graffiti.eql :as eql]
            [graffiti.query :as query]
            [com.wsscode.pathom.connect :as pc]))

(defn ident
  [resolver args]
  (let [input (->> resolver ::pc/input first)]
    [input (get args (ns/unnamespaced input))]))

(defn pathom
  [resolver parser]
  (fn [context args value]
    (->> (query/eql
           {:pathom/parser parser}
           [{(ident resolver args)
             (-> context ex/selections-tree eql/from-selection-tree)}])
         vals
         (map eql/as-tree)
         (reduce merge))))
