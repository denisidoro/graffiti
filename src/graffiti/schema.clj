(ns graffiti.schema
  (:require [quark.collection.ns :as ns]
            [clojure.spec.alpha :as s]
            [provisdom.spectomic.core :as spectomic]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.keyword :as keyword]
            [quark.collection.map :as map]))

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

(defn ^:private nilable?
  [x]
  (let [description (s/describe x)]
    (and (coll? description)
         (boolean (map/find-first #(= % 'nilable) description)))))

(defn ^:private with-nillable-modifier
  [nilable? s]
  (if nilable?
    s
    (list 'non-null s)))

(defn ident->ref-type
  [ident object-map]
  (let [ref-kw    (keyword/from-ident ident)
        ref-type  (get object-map ref-kw)
        ref-null? (nilable? ref-kw)]
    (with-nillable-modifier ref-null? ref-type)))

(defn datomic-schema->lacinia-type
  [k
   object-map
   {:db/keys [ident cardinality valueType]}]
  (let [primitive (datomic-value-type->lacinia-primitive valueType)
        many?     (= cardinality :db.cardinality/many)
        ref?      (= :db.type/ref valueType)
        ref-type  (when ref? (ident->ref-type ident object-map))
        null?     (nilable? k)
        value     (if ref?
                    ref-type
                    (with-nillable-modifier null? primitive))]
    (cond
      (and many? (not null?)) (list 'non-null (list 'list value))
      many? (list 'list value)
      :else value)))

(defn lacinia-type
  [object-map k]
  (->> [k]
       spectomic/datomic-schema
       first
       (datomic-schema->lacinia-type k object-map)))

(defn lacinia-fields
  [object-map k]
  (->> k
       s/describe
       last
       (map (fn [k] [k {:type (lacinia-type object-map k)}]))
       (into {})
       ns/unnamespaced))

(defn resolver-args
  [object-map input]
  (->> input
       (map (fn [k] [k {:type (lacinia-type object-map k)}]))
       (into {}) ns/unnamespaced))

(defn lacinia-query
  [object-map
   {:keys [input type]}]
  {:type    type
   :resolve (keyword/from-type+input type input)
   :args    (resolver-args object-map input)})

