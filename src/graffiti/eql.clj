(ns graffiti.eql
  (:require [clojure.walk :as walk]
            [graffiti.keyword :as keyword]))

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
  [options q]
  (->> q
       (walk/postwalk
         (fn [{:keys [selections] :as x}]
           (cond
             (qualified-keyword? x)
             (keyword/fix-todo options x)
             :else
             (if selections
               selections
               x))))
       remove-nils))

(defn as-tree
  [{:graffiti/keys [graphql-conformer]} x]
  (walk/postwalk
    (fn [y]
      (if (keyword? y)
        (-> y name graphql-conformer)
        y)) x))
