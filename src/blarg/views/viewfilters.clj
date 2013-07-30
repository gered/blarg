(ns blarg.views.viewfilters
  (:use [blarg.datetime]
        [blarg.routes.helpers])
  (:require [selmer.filters :refer [add-filter!]]
            [markdown.core :as md]
            [clj-time.core]
            [clj-time.format]))

(add-filter!
  :md-to-html
  #(md/md-to-html-string %))

(add-filter!
  :post-url
  #(get-post-url %))

(add-filter!
  :to_relative
  #(->relative-timestamp %))

(add-filter!
  :to_month-day
  #(->month-day-str %))

(add-filter!
  :to_fulltime
  #(clj-time.local/format-local-time % :rfc822))

(add-filter!
  :is-empty
  #(empty? %))