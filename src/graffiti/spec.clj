(ns graffiti.spec
  (:require [clojure.spec.alpha :as s]
            [graffiti.core :as g]
            [graffiti.db :as db]
            [clojure.test :as t]
            [graffiti.schema.spec :as ss]
            [quark.collection.map :as map]))

(defn spec-primitive
  [spec]
  (let [last-elem (-> spec flatten last)
        change?   (symbol? last-elem)
        new-elem  (when change? (keyword (-> last-elem str keyword name) "__entity"))]
    (if change?
      (concat (drop-last spec) (list new-elem))
      spec)))

(defn with-values-as-map
  [schema]
  (map/map-vals #(if-not (map? %) {:schema %} %) schema))

(defmacro defentity
  [ns schema]
  (let [schema+ (with-values-as-map schema)
        ns-name (-> ns keyword name)
        kw      (keyword ns-name "__entity")
        ns-sym  (symbol ns-name)
        specs   (map (fn [[k {:keys [schema] :as options}]] (list `ss/sdef k (spec-primitive schema) options)) schema+)
        fields  (-> schema+ keys vec)]
    `(do ~@specs
         (s/def ~kw (s/keys :opt ~fields))
         (def ~ns-sym ~kw))))

