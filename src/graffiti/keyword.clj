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
  (keyword (name type) (-> (str (or (seq input) :none))
                           (str/replace #"[#\{\}:\(\)]" "")
                           (str/replace #"/" "_"))))

(defn snake-fn
  [f k]
  (if-not (keyword? k)
    (f k)
    (let [k-ns   (namespace k)
          k-name (name k)]
      (if k-ns
        (keyword (-> k-ns f name) (-> k-name f name))
        (f k)))))

(def camel-case
  (partial snake-fn snake/->camelCase))

(def kebab-case
  (partial snake-fn snake/->kebab-case))

(defn sanitize
  [k]
  (let [k-ns   (namespace k)
        k-name (name k)]
    (keyword (snake/->kebab-case k-ns) k-name)))

(defn graphql
  [k]
  (if (qualified-keyword? k)
    (keyword (-> k namespace keyword graphql name)
             (-> k name keyword graphql name))
    (->> (str/split (name k) #"\.")
         (map camel-case)
         (str/join "_")
         keyword)))

(defn eql
  [k]
  (if (qualified-keyword? k)
    (keyword (-> k namespace keyword eql name)
             (-> k name keyword eql name))
    (->> (str/split (name k) #"\_")
         (map kebab-case)
         (str/join ".")
         keyword)))

(defn fix-todo
  [mesh k]
  (let [k-ns   (keyword (namespace k))
        k-name (name k)
        k-ns+  (-> mesh
                   (get-in [:graffiti/options :lacinia/objects k-ns])
                   namespace)]
    (eql (keyword k-ns+ k-name))))

