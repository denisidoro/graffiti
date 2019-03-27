(ns graffiti.schema.spec
  (:require [clojure.spec.alpha :as s]
            [quark.collection.map :as map]))

(def registry
  (atom {}))

(defmacro sdef
  ([k spec-form]
   `(sdef ~k ~spec-form {}))
  ([k spec-form metadata]
   (let [meta  (dissoc metadata :schema)
         meta+ (when (seq (keys meta)) meta)]
     `(do (s/def ~k ~spec-form)
          (swap! registry map/assoc-if ~k ~meta+)))))
