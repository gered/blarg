(ns blarg.routes.home
  (:use [slugger.core]
        [compojure.core]
        [blarg.routes.helpers])
  (:require [blarg.views.layout :as layout]
            [noir.response :as resp]))

(defn about-page []
  (layout/render
    "about.html" {:html-title (->html-title ["About"])}))

(defn projects-page []
  (layout/render
    "projects.html" {:html-title (->html-title ["Projects"])}))

(defroutes home-routes
  (GET "/about" [] (about-page))
  (GET "/projects" [] (projects-page))
  (GET "/toslug" [text] (resp/json {:slug (if text (->slug text))})))
