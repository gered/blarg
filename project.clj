(defproject blarg "0.1"
  :description "webapp for http://blarg.ca/"
  :url "http://blarg.ca/"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [lib-noir "0.5.4"]
                 [compojure "1.1.5"]
                 [ring-server "0.2.8"]
                 [clabango "0.5"]
                 [com.taoensso/timbre "1.6.0"]
                 [com.postspectacular/rotor "0.1.0"]
                 [com.taoensso/tower "1.5.1"]
                 [markdown-clj "0.9.19"]
                 [com.ashafa/clutch "0.4.0-RC1"]
                 [slugger "1.0.1"]
                 [clj-time "0.5.0"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [cheshire "5.1.2"]
                 [clj-rss "0.1.3"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler blarg.handler/war-handler
         :init    blarg.handler/init
         :destroy blarg.handler/destroy}
  :profiles
  {:production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.3"]
                        [ring/ring-devel "1.1.8"]]}}
  :min-lein-version "2.0.0")