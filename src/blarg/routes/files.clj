(ns blarg.routes.files
  (:use [compojure.core]
        [noir.util.route]
        [blarg.routes.helpers]
        [blarg.util])
  (:require [blarg.views.layout :as layout]
            [blarg.models.files :as files]
            [noir.response :as resp]
            [noir.session :as session]))

(defn valid-upload? [file]
  (and (not (nil? file))
       (not-empty (:filename file))))

(defn list-files [path]
  (let [p (ensure-prefix-suffix path "/")]
    (layout/render
    "files/list.html" {:html-title (->html-title [(str "Files in " p)])
                       :path p
                       :files (files/list-files p)
                       :tree (files/get-tree)
                       :error (session/flash-get :file-error)
                       :success (session/flash-get :file-success)
                       :notice (session/flash-get :file-notice)})))

(defn handle-new-file [path file]
  (if (valid-upload? file)
    (let [filename (:filename file)
          tempfile (:tempfile file)
          content-type (:content-type file)
          id (str path filename)
          exists? (files/file-exists? id)]
      (if-let [savedfile (if exists?
                           (files/update-file id tempfile content-type)
                           (files/add-file path filename tempfile content-type))]
        (do
          (session/flash-put! :file-success (str "<strong>" (:id savedfile) "</strong> was uploaded successfully."))
          (session/flash-put! :file-notice "Existing file with the same name was updated with the uploaded file."))
        (session/flash-put! :file-error "File could not be uploaded.")))
    (session/flash-put! :file-error "No file selected to upload."))
  (resp/redirect (str "/listfiles" path)))

(defn handle-update-file [id file]
  (if (valid-upload? file)
    (let [tempfile (:tempfile file)
          content-type (:content-type file)]
      (if-let [updatedfile (files/update-file id tempfile content-type)]
        (session/flash-put! :file-success (str "<strong>" id "</strong> was updated successfully."))
        (session/flash-put! :file-error "File could not be updated.")))
    (session/flash-put! :file-error "No file selected to upload."))
  (resp/redirect (str "/listfiles")))

(defn handle-delete-file [id]
  (if-let [deleted (files/delete-file id)]
    (session/flash-put! :file-success (str "<strong>" id "</strong> was deleted successfully."))
    (session/flash-put! :file-error "File could not be deleted."))
  (resp/redirect "/listfiles"))

(defn get-file [path]
  (if-let [file (files/get-file path)]
    (resp/content-type (:content_type file) (:data file))
    (resp/status 404 nil)))

(defroutes files-routes
  (restricted GET "/listfiles" [] (list-files "/"))
  (restricted GET "/listfiles/*" [*] (list-files *))
  (restricted POST "/uploadfile" [path file] (handle-new-file (ensure-prefix-suffix path "/") file))
  (restricted POST "/updatefile" [id file] (handle-update-file (ensure-prefix id "/") file))
  (restricted POST "/deletefile" [id] (handle-delete-file (ensure-prefix id "/")))
  (GET "/files/*" [*] (get-file *)))
