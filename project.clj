(defproject blarg "0.7"
  :description "webapp for http://blarg.ca/"
  :url "http://blarg.ca/"
  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [lib-noir "0.8.1"]
   [compojure "1.1.6"]
   [ring-server "0.3.1"]
   [com.taoensso/timbre "3.0.0"]
   [cheshire "5.2.0"]
   [clj-jtwig-java6 "0.3.2"]
   [environ "0.4.0"]
   [markdown-clj "0.9.41"]
   [com.ashafa/clutch "0.4.0-RC1"]
   [slugger "1.0.1"]
   [clj-time "0.6.0"]
   [org.clojure/math.numeric-tower "0.0.4"]
   [clj-rss "0.1.3"]
   [clj-metasearch "0.1.1"]]
  :main main
  :plugins [[lein-ring "0.8.10"]
            [lein-environ "0.4.0"]]
  :ring {:handler blarg.handler/handle-app
         :init    blarg.handler/init
         :destroy blarg.handler/destroy}
  :profiles
  {:uberjar    {:aot :all}
   :production {:ring           {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}
                :resource-paths ["env-resources/prod"]}
   :repl       {:source-paths   ["dev"]}
   :dev        {:dependencies   [[ring-mock "0.1.5"]
                                 [ring/ring-devel "1.2.1"]]
                :resource-paths ["env-resources/dev"]}}
  :min-lein-version "2.0.0")
