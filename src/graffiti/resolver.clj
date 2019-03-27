(ns graffiti.resolver
  (:require [com.walmartlabs.lacinia.executor :as ex]
            [quark.collection.ns :as ns]
            [graffiti.eql :as eql]
            [graffiti.query :as query]
            [com.wsscode.pathom.connect :as pc]
            [clojure.set :as set]
            [clojure.walk :as walk]))

(defn ident
  [input output args]
  (if-let [input' (first input)]
    [input' (get args (ns/unnamespaced input'))]
    output))

(defn graphql-keywords
  [{:graffiti/keys [graphql-conformer]}
   m]
  (walk/postwalk
    (fn [x]
      (if (keyword? x)
        (graphql-conformer x)
        x))
    m))

(defn resolver-results
  [parser options eql-query]
  (->> (query/eql {:pathom/parser parser} eql-query)
       vals
       (map #(eql/as-tree options %))
       (reduce merge)
       (graphql-keywords options)))

(defn pathom
  [input output parser]
  (fn [{:graffiti/keys [mesh] :as context}
       args
       value]
    (let [options   (:graffiti/options mesh)
          ident     (ident input output args)
          fields    (->> context
                         ex/selections-tree
                         (eql/from-selection-tree options))
          eql-query [{ident fields}]]
      (resolver-results parser options eql-query))))

(defn conform-config
  [config]
  (set/rename-keys
    config
    {:input  ::pc/input
     :output ::pc/output}))
