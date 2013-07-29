(ns blarg.views.viewfilters
  (:use [blarg.datetime]
        [blarg.routes.helpers])
  (:require [selmer.filters :refer [add-filter!]]
            [markdown.core :as md]
            [clj-time.core]
            [clj-time.format]))

(add-filter!
  :md-to-html
  (fn [s]
    (md/md-to-html-string s)))

(add-filter!
  :post-url
  (fn [post]
    (get-post-url post)))

(add-filter!
  :to_relative
  (fn [date]
    (->relative-timestamp date)))

(add-filter!
  :to_month-day
  (fn [date]
    (->month-day-str date)))

(add-filter!
  :to_fulltime
  (fn [date]
    (clj-time.local/format-local-time date :rfc822)))
