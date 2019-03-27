(defproject denisidoro/graffiti "0.12.0"

  :description "An opinionated, declarative GraphQL implementation in Clojure"
  :url "https://github.com/denisidoro/graffiti"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[denisidoro/quark "0.6.0"]
                 [com.wsscode/pathom "2.2.12"]
                 [com.walmartlabs/lacinia "0.32.0"]
                 [provisdom/spectomic "0.7.9"]]

  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            ;[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.3"]
            [lein-auto "0.1.3"]
            [lein-changelog "0.3.2"]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]
                                  [nubank/matcher-combinators "0.7.0"]
                                  [nubank/selvage "1.0.0-BETA"]]}}

  :deploy-repositories [["releases" :clojars]]

  :test-paths ["test/"])
