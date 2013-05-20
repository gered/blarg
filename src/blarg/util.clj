(ns blarg.util
  (:require [noir.io :as io]
            [markdown.core :as md]))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (->>
    (io/slurp-resource filename)
    (md/md-to-html-string)))

(defn string->int
  ([s] (string->int s nil))
  ([s default]
    (let [match (re-find #"-?\d+" s)]
      (if match
        (Integer. match)
        default))))

(defn parse-int
  ([x] (parse-int x nil))
  ([x default]
    (cond
      (nil? x)    default
      (number? x) (int x)
      (string? x) (string->int x default)
      :else default)))

(defn make-in-range [n low high]
  (cond
    (< n low) low
    (> n high) high
    :else n))
