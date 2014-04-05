(ns blarg.models.users
  (:use [blarg.models.db]
        [blarg.datetime])
  (:require [com.ashafa.clutch :as couch]
            [noir.util.crypt :as crypt]))

(defn get-user 
  ([id]
    (with-users-db
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
    (with-users-db
      (couch/put-document user))))

(defn delete-user [id]
  (with-users-db
    (if-let [user (get-user id)]
      (couch/delete-document user))))
