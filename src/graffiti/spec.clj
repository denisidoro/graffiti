(ns graffiti.spec
  (:require [clojure.spec.alpha :as s]
            [graffiti.core :as g]
            [graffiti.db :as db]
            [clojure.test :as t]))

(defn spec-primitive
  [spec]
  (let [last-elem (-> spec flatten last)
        change?   (symbol? last-elem)
        new-elem  (when change? (keyword (-> last-elem str keyword name) "__entity"))]
    (if change?
      (concat (drop-last spec) (list new-elem))
      spec)))

(defmacro defentity
  [ns schema]
  (let [ns-name (-> ns keyword name)
        kw      (keyword ns-name "__entity")
        ns-sym  (symbol ns-name)
        specs   (map (fn [[k spec]] (list `s/def k (spec-primitive spec))) schema)
        fields  (-> schema keys vec)]
    `(do ~@specs
         (s/def ~kw (s/keys :opt ~fields))
         (def ~ns-sym ~kw))))
