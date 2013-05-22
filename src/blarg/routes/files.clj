(ns blarg.routes.files
  (:use [compojure.core]
        [noir.util.route]
        [blarg.routes.helpers]
        [blarg.util])
  (:require [blarg.views.layout :as layout]
            [blarg.models.files :as files]
            [noir.response :as resp]))

(defn list-files [path]
  (layout/render
    "files/list.html" {:files (files/list-files path)}))

(defn get-file [path]
  (if-let [file (files/get-file path)]
    (resp/content-type (:content_type file) (:data file))
    (resp/status 404 nil)))

(defroutes files-routes
  (restricted GET "/listfiles" [] (list-files "/"))
  (restricted GET "/listfiles/*" [*] (list-files (ensure-prefix * "/")))
  (GET "/files/*" [*] (get-file (ensure-prefix * "/"))))
