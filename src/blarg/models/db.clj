(ns blarg.models.db
  (:require [blarg.config :as config]
            [com.ashafa.clutch :as couch]))

(defn db-url [db]
  (let [c    (config/get-db-config)
        url  (:url c)
        user (:user c)
        pass (:pass c)]
    (if (and user pass)
      (assoc (cemerick.url/url url db)
        :username user
        :password pass)
      (cemerick.url/url url db))))

(def users (db-url "blarg_users"))
(def posts (db-url "blarg_posts"))
(def comments (db-url "blarg_comments"))

(defmacro ->view-values
  "returns a sequence of only the values returned by running a view"
  [& body]
  `(if-let [result# ~@body]
     (map (fn [x#] (:value x#)) result#)))

(defmacro ->first-view-value
  "returns only the first value from the sequence returned by running a view"
  [& body]
  `(first (->view-values ~@body)))