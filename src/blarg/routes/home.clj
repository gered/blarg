(ns blarg.routes.home
  (:use [slugger.core]
        [compojure.core]
        [blarg.routes.helpers])
  (:require [blarg.views.layout :as layout]
            [blarg.route-utils :refer [register-routes]]
            [noir.response :as resp]))

(defn about-page []
  (layout/render
    "about.html"
    :params {:htmlTitle (->html-title ["About"])}))

(defn projects-page []
  (layout/render
    "projects.html"
    :params {:htmlTitle (->html-title ["Projects"])}))

(register-routes home-routes
  (GET "/about" [] (about-page))
  (GET "/projects" [] (projects-page))
  (GET "/toslug" [text] (resp/json {:slug (if text (->slug text))})))
