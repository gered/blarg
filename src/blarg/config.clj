(ns blarg.config
  (:require [clojure.java.io :as io])
  (:import [java.io PushbackReader]))

(def site-config (atom nil))

(defn load-config []
  (with-open [r (PushbackReader. (io/reader (io/resource "site.config")))]
    (read r)))

(defn get-config
  "returns the entire site configuration"
  []
  (if (nil? @site-config)
    (reset! site-config (load-config))
    @site-config))

(defn get-db-config
  "returns just the database portion of the site configuration"
  []
  (:database (get-config)))
