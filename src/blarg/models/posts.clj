(ns blarg.models.posts
  (:use [blarg.models.db]
        [blarg.datetime]
        [slugger.core])
  (:require [com.ashafa.clutch :as couch]))

(def per-page 5)
(def timestamp-fields [:created_at])

(defmacro ->post-list [& body]
  `(string->date
     (->view-values
       ~@body)
     ~timestamp-fields))

(defmacro ->single-post [& body]
  `(string->date
     (let [result# ~@body]
       (if (seq? result#)
         (->first-view-value result#)
         result#))
     ~timestamp-fields))

(defn get-post [id]
  (->single-post
    (couch/with-db posts
      (couch/get-document id))))

(defn get-post-by-date-slug [date slug]
  (->single-post
    (couch/with-db posts
      (couch/get-view "posts" "listPostsBySlug" {:key [date, slug]}))))

(defn add-post [title body user tags]
  (->single-post
    (couch/with-db posts
      (couch/put-document {:title title
                           :slug (->slug title)
                           :body body
                           :user user
                           :tags tags
                           :created_at (get-timestamp)
                           :published false
                           :type "post"}))))

(defn update-post [id title body user tags published? reset-date?]
  (if-let [post (get-post id)]
    (->single-post
      (couch/with-db posts
        (couch/update-document
          (-> (date->string post timestamp-fields)
            (merge (if title {:title title :slug (->slug title)}))
            (merge (if body {:body body}))
            (merge (if user {:user user}))
            (merge (if tags {:tags tags}))
            (merge (if-not (nil? published?) {:published published?}))
            (merge (if reset-date? {:created_at (get-timestamp)}))))))))

(defn delete-post [id]
  (if-let [post (get-post id)]
    (couch/with-db posts
      (couch/delete-document post))))

(defn publish-post [id publish? reset-date?]
  (update-post id nil nil nil nil publish? reset-date?))

(defn list-tags []
  (->view-keys
    (couch/with-db posts
      (couch/get-view "posts" "listTags" {:group true}))))

(defn list-posts
  ([unpublished?] (list-posts unpublished? per-page 0))
  ([unpublished? n] (list-posts unpublished? n 0))
  ([unpublished? n offset]
    (let [params (merge
                   {:descending true}
                   (when n {:limit n})
                   (when offset {:skip offset}))
          viewName (if unpublished? "listPosts" "listPublishedPosts")]
      (->post-list
        (couch/with-db posts
          (couch/get-view "posts" viewName params))))))

(defn count-posts
  [unpublished?]
  (->first-view-value
    (couch/with-db posts
      (couch/get-view 
        "posts" 
        (if unpublished? "countPosts" "countPublishedPosts")
        {:group true}))))

