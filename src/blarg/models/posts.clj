(ns blarg.models.posts
  (:use [blarg.models.db]
        [blarg.datetime]
        [slugger.core])
  (:require [com.ashafa.clutch :as couch]
            [clj-time.core :refer [after?]]
            [blarg.util :refer [md->html]]))

(def per-page 5)
(def timestamp-fields [:created_at])

(defonce post-md-cache (atom {}))

(defn- cache-post! [{:keys [_id created_at body] :as post}]
  (let [cached-post (get @post-md-cache _id)]
    (if (or (not cached-post)
            (after? created_at (:created_at cached-post)))
      (do
        (swap! post-md-cache assoc _id {:created_at created_at
                                        :html_body (md->html body)})
        (get @post-md-cache _id))
      cached-post)))

(defn- merge-cached-post-md! [post]
  (let [cached-post (cache-post! post)]
    (merge
      post
      (select-keys cached-post [:html_body]))))

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
  (merge-cached-post-md!
    (->single-post
      (couch/with-db posts
        (couch/get-document id)))))

(defn get-post-by-date-slug [date slug]
  (merge-cached-post-md!
    (->single-post
      (couch/with-db posts
        (couch/get-view "posts" "listPostsBySlug" {:key [date, slug]})))))

(defn add-post [title body user tags]
  (merge-cached-post-md!
    (->single-post
      (couch/with-db posts
        (couch/put-document
          {:title title
           :slug (->slug title)
           :body body
           :user user
           :tags tags
           :created_at (get-timestamp)
           :published false
           :type "post"})))))

(defn update-post [id title body user tags published? reset-date?]
  (if-let [post (get-post id)]
    (merge-cached-post-md!
      (->single-post
        (couch/with-db posts
          (couch/update-document
            (-> (date->string post timestamp-fields)
                (merge (if title {:title title :slug (->slug title)}))
                (merge (if body {:body body}))
                (merge (if user {:user user}))
                (merge (if tags {:tags tags}))
                (merge (if-not (nil? published?) {:published published?}))
                (merge (if reset-date? {:created_at (get-timestamp)})))))))))

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
      (map
        merge-cached-post-md!
        (->post-list
          (couch/with-db posts
            (couch/get-view "posts" viewName params)))))))

(defn list-posts-by-tag [unpublished? tag]
  (let [view-name (if unpublished? "listPostsByTag" "listPublishedPostsByTag")]
    (if-let [posts (->post-list
                     (couch/with-db posts
                       (couch/get-view "posts" view-name {:key tag
                                                          :descending true})))]
      ;TODO: look into couchdb partial key matching to do this sort at the DB
      (sort-by :created_at clj-time.core/after? posts))))

(defn list-posts-archive [unpublished?]
  (let [view-name (if unpublished? "listPostsArchive" "listPublishedPostsArchive")]
    (if-let [posts (->post-list
                     (couch/with-db posts
                       (couch/get-view "posts" view-name {:descending true})))]
      (map
        #(assoc % :mmyyyy (->nicer-month-year-str (:mmyyyy %)))
        posts))))

(defn count-posts
  [unpublished?]
  (->first-view-value
    (couch/with-db posts
      (couch/get-view 
        "posts" 
        (if unpublished? "countPosts" "countPublishedPosts")
        {:group true}))))

