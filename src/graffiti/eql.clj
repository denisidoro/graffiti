(ns graffiti.eql
  (:require [clojure.walk :as walk]
            [graffiti.keyword :as keyword]
            [quark.collection.ns :as ns]))

(defn ^:private remove-nils
  [q]
  (reduce
    (fn [m [k v]]
      (if-not v
        (conj m k)
        (conj m {k (remove-nils v)})))
    []
    q))

(defn from-selection-tree
  [q mesh]
  (->> q
       (walk/postwalk
         (fn [{:keys [selections] :as x}]
           (cond
             (qualified-keyword? x)
             (keyword/fix-todo mesh x)
             :else
             (if selections
               selections
               x))))
       remove-nils))

(defn as-tree
  [x]
  (ns/unnamespaced x))
