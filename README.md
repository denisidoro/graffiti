# graffiti

An opinionated, declarative GraphQL implementation in Clojure.
It is powered by [lacinia][lacinia], [pathom][pathom] and [clojure.spec][spec].

### Usage

```clojure
(require '[graffiti.core :as g])

;; specs
(s/def :book/id string?)
(s/def :book/title string?)
(s/def :book/entity (s/keys :opt [:book/id :book/title]))

;; resolvers
(g/defresolver book
  [ctx {:book/keys [id]}]
  {:input  #{:book/id}            ; from :book/id
   :output [:book/id :book/title] ; we can go to :book/id and :book/title
   :spec   :book/entity}
  (db/get-book ctx id))

;; setup definition
(def ^:const options
  {:lacinia/objects
   {:Book :book/entity}

   :lacinia/queries
   {:book book}})

;; compilation
(def mesh (g/compile options))

;; GraphQL query
(g/graphql mesh "{ book(id: \"1234\") { id title }}")
; => {:data {:book {:id    "1234"
;                   :title "The Great Gatsby"}}})

;; EQL query
(g/eql mesh [{[:book/id "1234"] [:book/id :book/title]}])
; => {[:book/id "1234"] #:book{:id    "1234"
;                              :title "The Great Gatsby"}}
```

### Why?

[lacinia][lacinia] is the de facto GraphQL library for Clojure.

However, it encourages you to define schemas in a non-idiomatic way:
```clojure
{:objects
 {:Book
  {:fields
   {:id    {:type '(non-null ID)}
    :title {:type '(non-null String)}}}}
 {:queries
  {:book
   {:type    :Book
    :args    {:id {:type 'ID}}
    :resolve :book}}}}
```
Considering that in your application you're most likely already defining schemas using [plumatic/schema][schema] or [clojure.spec][spec], redefining it is simply cumbersome.

[pathom] is a graph parser with steroids. It has all the bells and whistles but if you want to use it as a GraphQL server you have to change your mental model a bit and write some boilerplate.

In addition to that, [lacinia][lacinia] needs to know at development time the paths necessary to go from an input to an output, for complex graph structures. In [lacinia][lacinia]'s [documentation example][game.tutorial], you need to hardcode in the schema resolvers that allow you to go from a `:Game` to a `:Designer` and vice-versa. See [how much easier it is](https://github.com/denisidoro/graffiti/blob/master/test/graffiti/game_test.clj) to implement the example using [pathom][pathom] under the hood.

Finally, [lacinia][lacinia] doesn't encourage you to use namespaces keys, which are fundamental to avoid collisions specially in systems such as API gateways or backends for frontends.

This library attempts to be the best of both worlds.

[lacinia]: https://github.com/walmartlabs/lacinia
[pathom]: https://github.com/wilkerlucio/pathom
[spec]: https://clojure.org/guides/spec
[schema]: https://github.com/plumatic/schema
[game.tutorial]: https://lacinia.readthedocs.io/en/latest/tutorial/designer-data.html
