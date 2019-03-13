(ns graffiti.resolver
  (:require [com.walmartlabs.lacinia.executor :as ex]
            [quark.collection.ns :as ns]
            [graffiti.eql :as eql]
            [graffiti.query :as query]
            [com.wsscode.pathom.connect :as pc]
            [clojure.set :as set]))

(defn ident
  [input args]
  (let [input' (first input)]
    [input' (get args (ns/unnamespaced input'))]))

(defn pathom
  [input parser]
  (fn [context args value]
    (->> (query/eql
           {:pathom/parser parser}
           [{(ident input args)
             (-> context ex/selections-tree eql/from-selection-tree)}])
         vals
         (map eql/as-tree)
         (reduce merge))))

(defn conform-config
  [config]
  (set/rename-keys
    config
    {:input  ::pc/input
     :output ::pc/output}))
