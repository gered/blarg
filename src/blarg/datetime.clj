(ns blarg.datetime
  (:require [clj-time.core]
            [clj-time.coerce]
            [clj-time.format]
            [clj-time.local]))

(defn get-timestamp
  "Converts a date object to a local timestamp string. The current
   date is used if none is provided."
  ([] (get-timestamp (clj-time.local/local-now)))
  ([date]
    (clj-time.local/format-local-time date :date-time)))

(defn parse-timestamp
  "Converts a local timestamp string to a date object."
  [s]
  (clj-time.local/to-local-date-time s))

(defn same-date?
  "Compares to dates to see if they are the same. The dates passed in are
   coerced to LocalDate objects before being compared."
  ([a b-day b-month b-year]
    (same-date? a (clj-time.core/local-date b-year b-month b-day)))
  ([a b]
    (let [local-a (clj-time.coerce/to-local-date a)
          local-b (clj-time.coerce/to-local-date b)]
      (= 0 (.compareTo local-a local-b)))))

(defn string->date
  "Given a map (or a sequence of maps), converts string timestamps contained in
   each of the provided fields to LocalDate objects."
  [coll fields]
  (cond
    (map? coll)
    (->> fields
         (apply
           #(if-let [timestamp (get coll %)]
              {% (parse-timestamp timestamp)}))
         (merge coll))
    
    (seq? coll)
    (map #(string->date % fields) coll)))

(defn date->string
  "Does the reverse of string->date (converts LocalDate's back to strings)"
  [coll fields]
  (cond
    (map? coll)
    (->> fields
         (apply
           #(if-let [date (get coll %)]
              {% (get-timestamp date)}))
         (merge coll))
    
    (seq? coll)
    (map #(date->string % fields) coll)))

(defn ->relative-timestamp
  "Returns a readable string representing the time between the current date
   and the provided one"
  [date]
  (if (string? date)
    (->relative-timestamp (clj-time.local/to-local-date-time date))
    (let [span (clj-time.core/interval date (clj-time.core/now))
          years (clj-time.core/in-years span)
          weeks (clj-time.core/in-weeks span)
          days (clj-time.core/in-days span)
          hours (clj-time.core/in-hours span)
          minutes (clj-time.core/in-minutes span)]
      (cond
        (> years 0)   (clj-time.format/unparse (clj-time.format/formatter "MMM d, yyyy") date)
        (> weeks 0)   (clj-time.format/unparse (clj-time.format/formatter "MMM d") date)
        (> days 1)    (str days " days ago")
        (= days 1)    "1 day ago"
        (> hours 1)   (str hours " hours ago")
        (= hours 1)   "1 hour ago"
        (> minutes 1) (str minutes " minutes ago")
        (= minutes 1) "1 minute ago"
        :else         "just now"))))

(defn ->month-day-str
  "Returns a date string in the format 'MMM d' from the given date object or 
   string timestamp"
  [date]
  (if (string? date)
    (->month-day-str (clj-time.local/to-local-date-time date))
    (clj-time.format/unparse (clj-time.format/formatter "MMM d") date)))

(defn ->nicer-month-year-str
  "Given a date in 'MM-yyyy' format (numeric month), returns the same date in 
   'MMM yyyy' format (name of month)"
  [s]
  (if-let [date (clj-time.format/parse (clj-time.format/formatter "MM-yyyy") s)]
    (clj-time.format/unparse (clj-time.format/formatter "MMM yyyy") date)))


