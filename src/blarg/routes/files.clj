(ns blarg.routes.files
  (:use [compojure.core]
        [noir.util.route]
        [blarg.routes.helpers]
        [blarg.util])
  (:require [blarg.views.layout :as layout]
            [blarg.models.files :as files]
            [noir.response :as resp]))

(defn list-files [path]
  (let [p (ensure-prefix-suffix path "/")]
    (layout/render
    "files/list.html" {:html-title (->html-title [(str "Files in " p)])
                       :path p
                       :files (files/list-files p)
                       :tree (files/get-tree)})))

(defn handle-new-file [path file]
  (let [p (ensure-prefix-suffix path "/")
        filename (:filename file)
        tempfile (:tempfile file)
        content-type (:content-type file)
        id (str p filename)
        exists? (files/file-exists? id)]
    (if-let [savedfile (if exists?
                         (files/update-file id tempfile content-type)
                         (files/add-file p filename tempfile content-type))]
      (resp/redirect (str "/listfiles" p))
      (throw (Exception. "Error uploading file")))))

(defn get-file [path]
  (if-let [file (files/get-file path)]
    (resp/content-type (:content_type file) (:data file))
    (resp/status 404 nil)))

(defroutes files-routes
  (restricted GET "/listfiles" [] (list-files "/"))
  (restricted GET "/listfiles/*" [*] (list-files *))
  (restricted POST "/uploadfile" [path file] (handle-new-file path file))
  (GET "/files/*" [*] (get-file *)))
