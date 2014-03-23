(defproject blarg "0.1"
  :description "webapp for http://blarg.ca/"
  :url "http://blarg.ca/"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [lib-noir "0.8.1"]
                 [compojure "1.1.6"]
                 [ring-server "0.3.1"]
                 [com.taoensso/timbre "3.0.0"]
                 [com.taoensso/tower "2.0.1"]
                 [cheshire "5.2.0"]
                 [selmer "0.2.4"]
                 [environ "0.4.0"]
                 [markdown-clj "0.9.26"]
                 [com.ashafa/clutch "0.4.0-RC1"]
                 [slugger "1.0.1"]
                 [clj-time "0.6.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-rss "0.1.3"]]
  :repl-options {:init-ns blarg.repl}
  :plugins [[lein-ring "0.8.10"]
            [lein-environ "0.4.0"]]
  :ring {:handler blarg.handler/war-handler
         :init    blarg.handler/init
         :destroy blarg.handler/destroy}
  :profiles
  {:production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.1"]]
         :source-paths ["dev"]}}
  :min-lein-version "2.0.0")
