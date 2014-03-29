(ns blarg.util
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.stacktrace :refer [print-stack-trace]]
            [markdown.core :as md]
            [noir.io]
            [cheshire.core :refer :all])
  (:import [java.io PushbackReader]))

(defn load-clj
  "loads a file from the resources directory, parses and returns it as clojure"
  [file]
  (with-open [r (PushbackReader. (io/reader (io/resource file)))]
    (read r)))

(defn load-json
  "loads a file from the resources directory, parses it as JSON and returns it
   as an equivalent clojure object"
  [file]
  (parse-stream (io/reader (io/resource file)) true))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (->>
    (noir.io/slurp-resource filename)
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

(defn ensure-prefix [s prefix]
  (if-not (.startsWith s prefix)
    (str prefix s)
    s))

(defn ensure-suffix [s suffix]
  (if-not (.endsWith s suffix)
    (str s suffix)
    s))

(defn ensure-prefix-suffix [s affix]
  (ensure-prefix (ensure-suffix s affix) affix))

(defn get-throwable-stack-trace [throwable]
  (if throwable
    (with-out-str
      (print-stack-trace throwable))))

(defn log-formatter [{:keys [level throwable message timestamp hostname ns]} & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  (format "%s %s %s [%s] - %s%s"
          timestamp hostname (-> level name str/upper-case) ns (or message "")
          (or (get-throwable-stack-trace throwable) "")))