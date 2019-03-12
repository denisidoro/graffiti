(ns graffiti.keyword
  (:require [camel-snake-kebab.core :as snake]
            [clojure.spec.alpha :as s]
            [com.wsscode.pathom.connect :as pc]
            [quark.collection.map :as map]))

(defn from-ident
  [ident]
  (let [get-spec (s/get-spec ident)]
    (if (keyword? get-spec)
      get-spec
      (->> ident s/describe flatten reverse (map/find-first keyword?)))))

(defn from-resolver
  [resolver]
  (keyword (::pc/sym resolver)))

(defn sanitize
  [k]
  (let [k-ns   (namespace k)
        k-name (name k)]
    (keyword (snake/->kebab-case k-ns) k-name)))

