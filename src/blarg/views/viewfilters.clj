(ns blarg.views.viewfilters
  (:use [blarg.datetime]
        [blarg.routes.helpers])
  (:require [clj-jtwig.functions :refer [deftwigfn]]
            [clj-time.core]
            [clj-time.format]
            [blarg.util :refer [md->html]]))

(deftwigfn "md_to_html" [s]
  (md->html s))

(deftwigfn "post_url" [post]
  (->> post
       (clojure.walk/keywordize-keys)
       (get-post-url)))

(deftwigfn "to_relative" [date]
  (->relative-timestamp date))

(deftwigfn "to_month_day" [date]
  (->month-day-str date))

(deftwigfn "to_fulltime" [date]
  (clj-time.local/format-local-time date :rfc822))
