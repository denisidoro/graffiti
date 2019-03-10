(ns graffiti.schema
  (:require [quark.collection.ns :as ns]
            [clojure.spec.alpha :as s]
            [provisdom.spectomic.core :as spectomic]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.keyword :as keyword]))

(def datomic-value-type->lacinia-primitive
  {:db.type/string  'String
   :db.type/boolean 'Boolean
   :db.type/double  'Float
   :java.lang.Long  'Float
   :db.type/float   'Float
   :db.type/instant 'String
   :db.type/uuid    'ID
   :db.type/bigdec  'Float
   :db.type/bigint  'Int
   :db.type/uri     'String
   :db.type/keyword 'String})

(defn datomic-schema->lacinia-type
  [object-map
   {:db/keys [ident cardinality valueType]}]
  (let [primitive (datomic-value-type->lacinia-primitive valueType)
        many?     (= cardinality :db.cardinality/many)
        ref?      (= :db.type/ref valueType)
        ref-kw    (when ref? (->> ident keyword/from-ident object-map))]
    (cond
      (and ref? many?) (list 'list ref-kw)
      ref? ref-kw
      many? (list 'list primitive)
      :else primitive)))

(defn lacinia-type
  [object-map k]
  (->> [k]
       spectomic/datomic-schema
       first
       (datomic-schema->lacinia-type object-map)))

(defn lacinia-fields
  [object-map k]
  (->> k
       s/describe
       last
       (map (fn [k] [k {:type (lacinia-type object-map k)}]))
       (into {})
       ns/unnamespaced))

(defn resolver-args
  [object-map resolver]
  (->> resolver
       ::pc/input
       (map (fn [k] [k {:type (lacinia-type object-map k)}]))
       (into {}) ns/unnamespaced))

(defn lacinia-query
  [object-map
   resolver]
  {:type    (-> resolver :spec object-map)
   :resolve (keyword/from-resolver resolver)
   :args    (resolver-args object-map resolver)})

