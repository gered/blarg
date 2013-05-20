(ns blarg.models.users
  (:use [blarg.models.db]
        [blarg.datetime])
  (:require [com.ashafa.clutch :as couch]
            [noir.util.crypt :as crypt]))

(defn get-user 
  ([id]
    (couch/with-db users
      (couch/get-document id)))
  ([id pass]
    (if-let [user (get-user id)]
      (when (crypt/compare pass (:password user))
        user))))

(defn add-user [id pass email]
  (let [user {:_id id
              :password (crypt/encrypt pass)
              :email email
              :created_at (get-timestamp)}]
    (couch/with-db users
      (couch/put-document user))))

(defn delete-user [id]
  (couch/with-db users
    (if-let [user (get-user id)]
      (couch/delete-document user))))
