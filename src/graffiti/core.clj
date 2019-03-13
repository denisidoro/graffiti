(ns graffiti.core
  (:refer-clojure :exclude [compile])
  (:require [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [graffiti.setup :as setup]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [com.walmartlabs.lacinia :as lacinia]
            [graffiti.interceptors :as interceptors]
            [clojure.core.async :as async]
            [graffiti.resolver :as resolver]))

(defn compile
  [{:lacinia/keys [queries]
    :pathom/keys  [resolvers]
    :as           options}]
  (let [
        ; pathom-resolvers   (concat (->> queries vals (map :resolver)) extra-resolvers)
        pathom-readers     [p/map-reader
                            pc/parallel-reader
                            pc/open-ident-reader
                            p/env-placeholder-reader]
        pathom-plugins     [(pc/connect-plugin {::pc/register resolvers})
                            p/error-handler-plugin
                            p/trace-plugin]
        pathom-parser      (p/parallel-parser
                             {::p/env     {::p/reader               pathom-readers
                                           ::p/placeholder-prefixes #{">"}}
                              ::p/mutate  pc/mutate-async
                              ::p/plugins pathom-plugins})
        lacinia-raw-schema (setup/gen-raw-schema options)
        lacinia-resolvers  (setup/gen-resolvers queries pathom-parser)
        lacinia-schema     (-> lacinia-raw-schema
                               (util/attach-resolvers lacinia-resolvers)
                               lacinia.schema/compile)]
    {:pathom/parser      pathom-parser
     :lacinia/raw-schema lacinia-raw-schema
     :lacinia/schema     lacinia-schema}))

(defmacro defresolver
  [sym arglist config & body]
  (let [config' (resolver/conform-config config)]
    `(pc/defresolver ~sym ~arglist ~config' ~@body)))

(defn graphql
  [{:lacinia/keys [schema]}
   query-string]
  (-> (lacinia/execute schema query-string nil nil)
      interceptors/simplify))

(defn eql
  [{:pathom/keys [parser]}
   query]
  (async/<!! (parser {} query)))
