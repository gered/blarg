(ns blarg.routes.home
  (:use [slugger.core]
        [compojure.core]
        [blarg.routes.helpers])
  (:require [blarg.views.layout :as layout]
            [noir.response :as resp]))

(defn about-page []
  (layout/render
    "about.html" {:html-title (->html-title ["About"])}))

(defroutes home-routes
  (GET "/about" [] (about-page))
  (GET "/toslug" [text] (resp/json {:slug (if text (->slug text))})))
