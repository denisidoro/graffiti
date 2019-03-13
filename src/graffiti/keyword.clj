(ns graffiti.keyword
  (:require [camel-snake-kebab.core :as snake]
            [clojure.spec.alpha :as s]
            [com.wsscode.pathom.connect :as pc]
            [quark.collection.map :as map]
            [clojure.string :as str]))

(defn from-ident
  [ident]
  (let [get-spec (s/get-spec ident)]
    (if (keyword? get-spec)
      get-spec
      (->> ident s/describe flatten reverse (map/find-first keyword?)))))

(defn from-resolver
  [resolver]
  (keyword (::pc/sym resolver)))

(defn from-type+input
  [type input]
  (keyword (name type) (-> (str input)
                           (str/replace #"[#\{\}:]" "")
                           (str/replace #"/" "_"))))

(defn sanitize
  [k]
  (let [k-ns   (namespace k)
        k-name (name k)]
    (keyword (snake/->kebab-case k-ns) k-name)))

(defn fix-todo
  [mesh k]
  (let [k-ns   (keyword (namespace k))
        k-name (name k)
        k-ns+  (-> mesh
                   (get-in [:graffiti/options :lacinia/objects k-ns])
                   namespace)]
    (keyword k-ns+ k-name)))

