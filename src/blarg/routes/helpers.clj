(ns blarg.routes.helpers
  (:require [clj-time.core]))

(defn get-post-url [post]
  (str 
    "/"
    (clj-time.core/year (:created_at post)) "/"
    (clj-time.core/month (:created_at post)) "/"
    (clj-time.core/day (:created_at post)) "/"
    (:slug post)))

(defn ->html-title [coll]
  (str " &raquo; " (clojure.string/join " &raquo; " coll)))
