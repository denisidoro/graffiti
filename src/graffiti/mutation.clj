(ns graffiti.mutation
  (:require [com.wsscode.pathom.connect :as pc]
            [clojure.set :as set]
            [graffiti.query :as query]
            [graffiti.eql :as eql]
            [graffiti.resolver :as resolver]
            [graffiti.keyword :as keyword]
            [quark.collection.map :as map]))

(defn conform-config
  [config]
  (set/rename-keys
    config
    {:sym    ::pc/sym
     :params ::pc/params
     :output ::pc/output}))

(defn ^:private construct-eql
  [mutation vars]
  [(list (::pc/sym mutation) vars)])

(defn ^:private contruct-vars
  [conformer params args]
  (->> params
       (map (fn [p]
              (let [graphql-p (-> p conformer name keyword)]
                [p (second (map/find-first (fn [[k v]] (= k graphql-p)) args))])))
       (into {})))

;; TODO
(defn pathom
  [mutation parser]
  (fn [{:graffiti/keys [mesh] :as context}
       args
       value]
    (let [options   (:graffiti/options mesh)
          conformer (:graffiti/graphql-conformer options)
          params    (::pc/params mutation)
          vars      (contruct-vars conformer params args)
          eql-query (construct-eql mutation vars)]
      (resolver/resolver-results parser options eql-query))))
