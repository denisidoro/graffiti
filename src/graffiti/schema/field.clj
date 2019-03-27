(ns graffiti.schema.field
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

(defn ^:private lacinia-type
  [object-map k]
  (->> [k]
       spectomic/datomic-schema
       first
       (datomic-schema->lacinia-type k object-map)))

(defn lacinia-fields
  [{:lacinia/keys  [inverted-objects]
    :graffiti/keys [graphql-conformer]}
   k]
  (->> k
       s/describe
       last
       (map (fn [k] [(graphql-conformer k) {:type (lacinia-type inverted-objects k)}]))
       (into {})
       ns/unnamespaced))

(defn resolver-args
  [{:graffiti/keys [graphql-conformer] :as options}
   input]
  (->> input
       (map (fn [k] [(graphql-conformer k) {:type (lacinia-type options k)}]))
       (into {}) ns/unnamespaced))

(defn lacinia-query
  [options
   {:keys [input type]}]
  (map/assoc-if
    {:type    type
     :resolve (keyword/from-type+input type input)}
    :args (and (some-> input seq)
               (resolver-args options input))))

(defn lacinia-mutation
  [options
   {:keys [type mutation]}]
  (let [{::pc/keys [params]} mutation]
    (map/assoc-if
      {:type    type
       :resolve (keyword/from-type+input type params)}
      :args (and (some-> params seq)
                 (resolver-args options params)))))

