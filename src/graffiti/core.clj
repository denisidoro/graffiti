(ns graffiti.core
  (:refer-clojure :exclude [compile])
  (:require [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [graffiti.setup :as setup]
            [graffiti.schema.core :as schema]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.resolver :as resolver]
            [graffiti.mutation :as mutation]
            [graffiti.query :as query]))

(defn compile
  [{:lacinia/keys [mutations raw-schema-update-fn resolver-map-update-fn]
    :pathom/keys  [resolvers readers plugins parser]
    :or           {raw-schema-update-fn   identity
                   resolver-map-update-fn identity}
    :as           options}]
  (let [options+            (setup/enhance-options options)
        pathom-readers      (or readers
                                [p/map-reader
                                 pc/parallel-reader
                                 pc/open-ident-reader
                                 p/env-placeholder-reader])
        resolvers+mutations (concat resolvers
                                    (->> mutations vals (map :mutation)))
        pathom-plugins      (or plugins
                                [(pc/connect-plugin {::pc/register resolvers+mutations})
                                 p/error-handler-plugin
                                 p/trace-plugin])
        pathom-parser       (or parser
                                (p/parallel-parser
                                  {::p/env     {::p/reader               pathom-readers
                                                ::p/placeholder-prefixes #{">"}}
                                   ::p/mutate  pc/mutate-async
                                   ::p/plugins pathom-plugins}))
        lacinia-raw-schema  (-> (schema/spec->graphql options+)
                                raw-schema-update-fn)
        lacinia-resolvers   (-> (setup/gen-resolvers+mutations options pathom-parser)
                                resolver-map-update-fn)
        lacinia-schema      (-> lacinia-raw-schema
                                (util/attach-resolvers lacinia-resolvers)
                                lacinia.schema/compile)
        options+-           (dissoc options+ :lacinia/inverted-objects)]
    {:graffiti/options   options+-
     :pathom/parser      pathom-parser
     :lacinia/raw-schema lacinia-raw-schema
     :lacinia/schema     lacinia-schema}))

(defmacro defresolver
  [sym arglist config & body]
  (let [config+ (resolver/conform-config config)]
    `(pc/defresolver ~sym ~arglist ~config+ ~@body)))

(defmacro defmutation
  [sym arglist config & body]
  (let [config+ (mutation/conform-config config)]
    `(pc/defmutation ~sym ~arglist ~config+ ~@body)))

(defn graphql
  ([mesh query-str]
   (graphql mesh query-str {}))
  ([mesh query-str variables]
   (graphql mesh query-str variables {}))
  ([mesh query-str variables context]
   (query/graphql (assoc context :graffiti/mesh mesh) query-str variables)))

(defn eql
  [mesh query]
  (query/eql mesh query))
