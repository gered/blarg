(ns blarg.config
  (:use [blarg.util])
  (:require [clojure.java.io :as io])
  (:import [java.io PushbackReader]))

(def site-config (atom nil))

(defn load-config []
  (load-clj "site.config"))

(defn get-config
  "returns the entire site configuration"
  []
  (if (nil? @site-config)
    (reset! site-config (load-config))
    @site-config))

(defn config-val [key]
  (get (get-config) key))

(defn config-val-in [ks]
  (get-in (get-config) ks))
