(ns graffiti.keyword-test
  (:require [clojure.test :as t]
            [matcher-combinators.test]
            [graffiti.keyword :as keyword]))

(t/deftest graphql-and-eql
 (t/are [input output]
   (match? {:graphql output
            :eql     input}
           {:graphql (keyword/graphql input)
            :eql     (keyword/eql output)})
    :foo                   :foo
    :foo-bar               :fooBar
    :foo.bar               :foo_bar
    :foo.bar-baz           :foo_barBaz
    :foo.bar.baz           :foo_bar_baz
    :foo-bar.baz           :fooBar_baz
    :foo-bar.lorem-ipsum   :fooBar_loremIpsum))
