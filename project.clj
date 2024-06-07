(defproject b-social/jason "0.1.6-SNAPSHOT"
  :description "Factory functions around jsonista, mostly for key conversion."
  :url "https://github.com/b-social/jason"

  :license {:name "The MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies
  [[metosin/jsonista "0.3.8"]
   [com.fasterxml.jackson.datatype/jackson-datatype-joda "2.15.2"]
   [com.fasterxml.jackson.datatype/jackson-datatype-jsr310 "2.15.2"]
   [camel-snake-kebab "0.4.3"]]

  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]
            [lein-eftest "0.5.8"]
            [lein-codox "0.10.7"]
            [lein-cljfmt "0.6.4"]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.1"]]

  :profiles {:shared {:dependencies
                      [[org.clojure/clojure "1.11.3"]
                       [clj-time "0.15.2"]
                       [eftest "0.5.8"]]}
             :dev    [:shared {:source-paths ["dev"]
                               :eftest       {:multithread? false}}]
             :test   [:shared {:eftest {:multithread? false}}]}

  :cloverage
  {:ns-exclude-regex [#"^user"]}

  :codox
  {:namespaces  [#"^jason\."]
   :metadata    {:doc/format :markdown}
   :output-path "docs"
   :doc-paths ["docs"]
   :source-uri  "https://github.com/b-social/jason/blob/{version}/{filepath}#L{line}"}

  :cljfmt {:indents ^:replace {#".*" [[:inner 0]]}}

  :deploy-repositories
  {"releases" {:url "https://repo.clojars.org" :creds :gpg}}

  :release-tasks
  [["shell" "git" "diff" "--exit-code"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["codox"]
   ["changelog" "release"]
   ["shell" "sed" "-E" "-i" "" "s/\"[0-9]+\\.[0-9]+\\.[0-9]+\"/\"${:version}\"/g" "README.md"]
   ["shell" "git" "add" "."]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["deploy"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["vcs" "push"]]

  :aliases {"test"      ["with-profile" "test" "eftest" ":all"]
            "precommit" ["do"
                         ["check"]
                         ["kibit" "--replace"]
                         ["cljfmt" "fix"]
                         ["with-profile" "test" "bikeshed"
                          "--max-line-length" "200"
                          "--verbose" "true"]
                         ["test"]]})
