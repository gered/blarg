(ns blarg.routes.rss
  (:use [blarg.routes.helpers]
        [compojure.core])
  (:require [markdown.core :as md]
            [noir.response :refer [xml]]
            [clj-rss.core :as rss]
            [clj-time.core]
            [clj-time.coerce]
            [blarg.models.posts :as posts]
            [blarg.route-utils :refer [register-routes]]))

(def rss-title "blarg.ca")
(def rss-site-url "http://www.blarg.ca/")
(def rss-description "Assorted code and random ramblings.")

(defn handle-rss []
  (let [channel {:title rss-title
                 :link rss-site-url
                 :description rss-description}]
    (xml
      (apply
        (partial rss/channel-xml channel)
        (->> (posts/list-posts false 10)
             (map (fn [post]
                    {:title (:title post)
                     :pubDate (clj-time.coerce/to-date (:created_at post))
                     :link (str rss-site-url (subs (get-post-url post) 1))
                     :description (md/md-to-html-string (:body post))}))
             (doall))))))

(register-routes rss-routes
  (GET "/rss" [] (handle-rss)))
