(ns graffiti.playground
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t]
            [graffiti.schema :as schema]
            [provisdom.spectomic.core :as spectomic]
            [quark.collection.map :as map]))

(defn spec-ex
  [k]
  {:describe (s/describe k)
   :form     (s/form k)
   :get-spec (s/get-spec k)
   :lacinia  (schema/lacinia-type {:car/entity :Car} k)
   :datomic  (-> [k] spectomic/datomic-schema first)})

(s/def :car/id (s/nilable string?))
(s/def :car/brand string?)
(s/def :car/entity (s/keys :opt [:car/id :car/brand]))

(s/def :maker/cars (s/coll-of :car/entity))
(s/def :maker/green-cars (s/coll-of (s/nilable :car/entity)))
(s/def :maker/blue-cars (s/nilable (s/coll-of :car/entity)))
(s/def :maker/red-cars (s/nilable (s/coll-of (s/nilable :car/entity))))

(spec-ex :car/id)
(spec-ex :car/brand)
(spec-ex :maker/cars)
(spec-ex :maker/green-cars)
(spec-ex :maker/blue-cars)
(spec-ex :maker/red-cars)

(spec-ex :game/designers)

