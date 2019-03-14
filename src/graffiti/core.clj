(ns graffiti.core
  (:refer-clojure :exclude [compile])
  (:require [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [graffiti.setup :as setup]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [graffiti.resolver :as resolver]
            [graffiti.query :as query]
            [graffiti.keyword :as keyword]
            [clojure.set :as set]))

(defn compile
  [{:lacinia/keys [objects raw-schema-update-fn resolver-map-update-fn]
    :pathom/keys  [resolvers readers plugins parser]
    :or           {raw-schema-update-fn   identity
                   resolver-map-update-fn identity}
    :as           options}]
  (let [options+           (merge {:graffiti/eql-conformer     keyword/eql
                                           :graffiti/graphql-conformer keyword/graphql
                                           :lacinia/inverted-objects   (set/map-invert objects)}
                                          options)
        pathom-readers     (or readers
                               [p/map-reader
                                pc/parallel-reader
                                pc/open-ident-reader
                                p/env-placeholder-reader])
        pathom-plugins     (or plugins
                               [(pc/connect-plugin {::pc/register resolvers})
                                p/error-handler-plugin
                                p/trace-plugin])
        pathom-parser      (or parser
                               (p/parallel-parser
                                 {::p/env     {::p/reader               pathom-readers
                                               ::p/placeholder-prefixes #{">"}}
                                  ::p/mutate  pc/mutate-async
                                  ::p/plugins pathom-plugins}))
        lacinia-raw-schema (-> (setup/gen-raw-schema options+)
                                       raw-schema-update-fn)
        lacinia-resolvers  (-> (setup/gen-resolvers options pathom-parser)
                               resolver-map-update-fn)
        lacinia-schema     (-> lacinia-raw-schema
                               (util/attach-resolvers lacinia-resolvers)
                               lacinia.schema/compile)
        options+-          (dissoc options+ :lacinia/inverted-objects)]
    {:graffiti/options   options+-
     :pathom/parser      pathom-parser
     :lacinia/raw-schema lacinia-raw-schema
     :lacinia/schema     lacinia-schema}))

(defmacro defresolver
  [sym arglist config & body]
  (let [config+ (resolver/conform-config config)]
    `(pc/defresolver ~sym ~arglist ~config+ ~@body)))

(defn graphql
  [mesh query-str]
  (query/graphql mesh query-str))

(defn eql
  [mesh query]
  (query/eql mesh query))
