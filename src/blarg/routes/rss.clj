(ns blarg.routes.rss
  (:use [blarg.routes.helpers]
        [compojure.core])
  (:require [markdown.core :as md]
            [noir.response :as resp]
            [clj-rss.core :as rss]
            [clj-time.core]
            [clj-time.coerce]
            [blarg.models.posts :as posts]))

(def rss-title "blarg.ca")
(def rss-site-url "http://www.blarg.ca/")
(def rss-description "Assorted code and random ramblings.")

(defn handle-rss []
  (let [channel {:title rss-title
                 :link rss-site-url
                 :description rss-description}
        items (doall
                (map
                  (fn [post]
                    {:title (:title post)
                     :pubDate (clj-time.coerce/to-date (:created_at post))
                     :link (str rss-site-url (subs (get-post-url post) 1))
                     :description (md/md-to-html-string (:body post))})
                  (posts/list-posts false 10)))]
    (resp/content-type "text/xml"
      (apply (partial rss/channel-xml channel)
        items))))

(defroutes rss-routes
    (GET "/rss" [] (handle-rss)))