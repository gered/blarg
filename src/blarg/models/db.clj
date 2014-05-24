(ns blarg.models.db
  (:use [blarg.util])
  (:require [blarg.config :refer [config-val]]
            [com.ashafa.clutch :as couch]))

(defn db-url [db]
  (let [c    (config-val :database)
        url  (:url c)
        user (:user c)
        pass (:pass c)]
    (if (and user pass)
      (assoc (cemerick.url/url url db)
        :username user
        :password pass)
      (cemerick.url/url url db))))

(defmacro with-db [url & body]
  `(couch/with-db
     ~url
     ~@body))

(defn get-users-db []
  (db-url "blarg_users"))
(defn get-posts-db []
  (db-url "blarg_posts"))
(defn get-files-db []
  (db-url "blarg_files"))

(defmacro with-users-db [& body]
  `(with-db (get-users-db) ~@body))
(defmacro with-posts-db [& body]
  `(with-db (get-posts-db) ~@body))
(defmacro with-files-db [& body]
  `(with-db (get-files-db) ~@body))

(defmacro ->view-keys
  "returns a sequence of only the keys returned by running a view"
  [& body]
  `(if-let [result# ~@body]
     (map (fn [x#] (:key x#)) result#)))

(defmacro ->view-values
  "returns a sequence of only the values returned by running a view"
  [& body]
  `(if-let [result# ~@body]
     (map (fn [x#] (:value x#)) result#)))

(defmacro ->first-view-value
  "returns only the first value from the sequence returned by running a view"
  [& body]
  `(first (->view-values ~@body)))

(defn touch-design-doc
  "verifies that a design doc is present in the given db, creating it if
   it is not there"
  [db design-doc-id doc-js-path]
  (couch/with-db db
    (let [doc (couch/get-document design-doc-id)]
      (if (nil? doc)
        (couch/put-document (load-json doc-js-path))))))

(defn touch-databases
  "verifies that the required databases are present, creating them if they
   are not there (including the views)."
  []
  #_(couch/get-database (get-users-db))
  #_(when (couch/get-database (get-files-db))
    (touch-design-doc (get-files-db) "_design/files" "couchdb/design_docs/files.js"))
  #_(when (couch/get-database (get-posts-db))
    (touch-design-doc (get-posts-db) "_design/posts" "couchdb/design_docs/posts.js")))
