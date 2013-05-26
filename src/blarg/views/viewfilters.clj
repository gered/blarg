(ns blarg.views.viewfilters
  (:use [blarg.datetime]
        [blarg.routes.helpers])
  (:require [clabango.filters :refer [deftemplatefilter]]
            [markdown.core :as md]
            [clj-time.core]
            [clj-time.format]))

(deftemplatefilter "is_false" [node body arg]
  (if body
    false
    true))

(deftemplatefilter "default" [node body arg]
  (if body
    body
    arg))

(deftemplatefilter "md-to-html" [node body arg]
  (md/md-to-html-string body))

(deftemplatefilter "post-url" [node body arg]
  (get-post-url body))

(deftemplatefilter "to_relative" [node body arg]
  (->relative-timestamp body))

(deftemplatefilter "to_month-day" [node body arg]
  (->month-day-str body))

(deftemplatefilter "to_fulltime" [node body arg]
  (clj-time.local/format-local-time body :rfc822))
