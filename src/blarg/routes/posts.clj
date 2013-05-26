(ns blarg.routes.posts
  (:use [slugger.core]
        [compojure.core]
        [noir.util.route]
        [blarg.util]
        [blarg.routes.helpers])
  (:require [clj-time.core]
            [clojure.math.numeric-tower :as math]
            [noir.response :as resp]
            [noir.validation :as vali]
            [noir.session :as session]
            [blarg.views.layout :as layout]
            [blarg.models.posts :as posts]
            [blarg.routes.auth :as auth]))

(defn string->tags [s]
  (set
    (map
      (fn [tag]
        (->slug tag))
      (clojure.string/split s #","))))

(defn tags->string [tags]
  (clojure.string/join "," tags))

(defn postcount->pagecount
  ([] (postcount->pagecount (posts/count-posts (auth/logged-in?))))
  ([postcount]
    (int (math/ceil (/ postcount posts/per-page)))))

(defn valid-post? [title tags body]
  (vali/rule (vali/has-value? title)
             [:title "Title must be provided."])
  (vali/rule (vali/has-value? tags)
             [:tags "One or more tags must be provided."])
  (vali/rule (vali/has-value? body)
             [:body "Post body must be provided."])
  (not (vali/errors? :title :tags :body)))

(defn list-page [page]
  (let [totalpages (postcount->pagecount )
        lastpage (- totalpages 1)
        currentpage (make-in-range page 0 lastpage)
        offset (* currentpage posts/per-page)]
    (layout/render
    "posts/list.html" {:posts (posts/list-posts (auth/logged-in?) posts/per-page offset)
                       :prevpage (- currentpage 1)
                       :nextpage (+ currentpage 1)
                       :atlastpage (= currentpage lastpage)
                       :atfirstpage (= currentpage 0)
                       :inlist true})))

(defn list-by-tag [tag]
  (layout/render
    "posts/listbytag.html" {:posts (posts/list-posts-by-tag (auth/logged-in?) tag)
                            :tag tag}))

(defn list-archive []
  (layout/render
    "posts/listarchive.html" {:months (posts/list-posts-archive (auth/logged-in?))}))

(defn show-post-page [year month day slug]
  (let [date (str year "-" month "-" day)
        post (posts/get-post-by-date-slug date slug)]
    (if (not-empty post)
      (layout/render
        "posts/showpost.html" {:post post
                               :html-title (->html-title [(:title post)])})
      (resp/redirect "/notfound"))))

(defn new-post-page [& post]
  (layout/render
    "posts/newpost.html" (merge (first post)
                           {:all-tags (posts/list-tags)
                            :html-title (->html-title ["New Post"])
                            :validation-errors @vali/*errors*})))

(defn handle-new-post [title tags body]
  (if (valid-post? title tags body)
    (if-let [post (posts/add-post title body (session/get :user) (string->tags tags))]
      (resp/redirect (get-post-url post))
      (throw (Exception. "Error creating new post.")))
    (new-post-page {:title title
                    :tags tags
                    :body body})))

(defn edit-post-page [id & posted-post]
  (let [post (if posted-post 
               (first posted-post) 
               (posts/get-post id))]
    (layout/render
    "posts/editpost.html" (merge post
                           {:tags (tags->string (:tags post))
                            :all-tags (posts/list-tags)
                            :html-title (->html-title ["Edit Post"])
                            :validation-errors @vali/*errors*}))))

(defn handle-edit-post [id title tags body]
  (if (valid-post? title tags body)
    (if-let [post (posts/update-post id title body (session/get :user) (string->tags tags) nil nil)]
      (resp/redirect (get-post-url post))
      (throw (Exception. "Error updating post.")))
    (edit-post-page id {:title title
                        :tags tags
                        :body body})))

(defn handle-publish-post [id publish? reset-date?]
  (if-let [post (posts/publish-post id publish? reset-date?)]
    (resp/redirect (get-post-url post))
    (throw (Exception. "Error toggling publish on post."))))

(defn handle-delete-post [id]
  (if-let [post (posts/delete-post id)]
    (resp/redirect "/")
    (throw (Exception. "Error deleting post."))))

(defroutes posts-routes
  (GET "/" [page] (list-page (parse-int page 0)))
  (GET 
    ["/:year/:month/:day/:slug" :year #"[0-9]{4}" :month #"[0-9]{1,2}" :day #"[0-9]{1,2}" :slug #"(.*)"] 
    [year month day slug] 
    (show-post-page year month day slug))
  (GET "/tag/:tag" [tag] (list-by-tag tag))
  (GET "/archive" [] (list-archive))
  (restricted GET "/newpost" [] (new-post-page))
  (restricted POST "/newpost" [title tags body] (handle-new-post title tags body))
  (restricted GET "/editpost/:id" [id] (edit-post-page id))
  (restricted POST "/editpost/:id" [id title tags body] (handle-edit-post id title tags body))
  (restricted GET "/publish/:id" [id reset-date] (handle-publish-post id true reset-date))
  (restricted GET "/unpublish/:id" [id] (handle-publish-post id false false))
  (restricted GET "/deletepost/:id" [id] (handle-delete-post id)))
