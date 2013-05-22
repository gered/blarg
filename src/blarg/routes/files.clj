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
    "files/list.html" {:html-title (->html-title [(str "Files in " path)])
                       :path path
                       :files (files/list-files path)
                       :tree (files/get-tree)}))

(defn handle-new-file [path file]
  (println "path: " path ", file: " file)
  (if-let [newfile (files/add-file 
                     (ensure-prefix path "/")
                     (:filename file)
                     (:tempfile file)
                     (:content-type file))]
    (resp/redirect (str "/listfiles" (ensure-prefix path "/")))
    (throw (Exception. "Error uploading file"))))

(defn get-file [path]
  (if-let [file (files/get-file path)]
    (resp/content-type (:content_type file) (:data file))
    (resp/status 404 nil)))

(defroutes files-routes
  (restricted GET "/listfiles" [] (list-files "/"))
  (restricted GET "/listfiles/*" [*] (list-files (ensure-prefix * "/")))
  (restricted POST "/uploadfile" [path file] (handle-new-file path file))
  (GET "/files/*" [*] (get-file (ensure-prefix * "/"))))
