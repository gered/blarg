(ns blarg.routes.files
  (:use [compojure.core]
        [noir.util.route]
        [blarg.routes.helpers]
        [blarg.util])
  (:require [blarg.views.layout :as layout]
            [blarg.models.files :as files]
            [blarg.routes.auth :as auth]
            [noir.response :as resp]
            [noir.session :as session]))

(defn valid-upload? [file]
  (and (not (nil? file))
       (not-empty (:filename file))))

(defn list-files [path]
  (let [p (ensure-prefix-suffix path "/")]
    (layout/render
      "files/list.html"
      {:html-title (->html-title [(str "Files in " p)])
       :path p
       :files (files/list-files p)
       :tree (files/get-tree)
       :error (session/flash-get :file-error)
       :success (session/flash-get :file-success)
       :notice (session/flash-get :file-notice)})))

(defn handle-new-file [path file returnpath]
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
          (if exists? (session/flash-put! :file-notice "Existing file with the same name was updated with the uploaded file.")))
        (session/flash-put! :file-error "File could not be uploaded.")))
    (session/flash-put! :file-error "No file selected to upload."))
  (resp/redirect (str "/listfiles" returnpath)))

(defn handle-update-file [id file returnpath]
  (if (valid-upload? file)
    (let [tempfile (:tempfile file)
          content-type (:content-type file)]
      (if-let [updatedfile (files/update-file id tempfile content-type)]
        (session/flash-put! :file-success (str "<strong>" id "</strong> was updated successfully."))
        (session/flash-put! :file-error "File could not be updated.")))
    (session/flash-put! :file-error "No file selected to upload."))
  (resp/redirect (str "/listfiles" returnpath)))

(defn handle-delete-file [id returnpath]
  (if-let [deleted (files/delete-file id)]
    (session/flash-put! :file-success (str "<strong>" id "</strong> was deleted successfully."))
    (session/flash-put! :file-error "File could not be deleted."))
  (resp/redirect (str "/listfiles" returnpath)))

(defn handle-publish-file [id publish? returnpath]
  (if-let [published (files/publish-file id publish?)]
    (session/flash-put! :file-success (str "<strong>" id "</strong> was " (if (:published published) "published" "unpublished") " successfully."))
    (session/flash-put! :file-error "Could not update file's published state."))
  (resp/redirect (str "/listfiles" returnpath)))

(defn get-file [path]
  (if-let [file (files/get-file path (auth/logged-in?))]
    (resp/content-type (:content_type file) (:data file))
    (resp/redirect "/notfound")))

(defroutes files-routes
  (GET "/listfiles" [] (restricted (list-files "/")))
  (GET "/listfiles/*" [*] (restricted (list-files *)))
  (POST "/uploadfile" [path file returnpath] (restricted (handle-new-file (ensure-prefix-suffix path "/") file returnpath)))
  (POST "/updatefile" [id file returnpath] (restricted (handle-update-file (ensure-prefix id "/") file returnpath)))
  (POST "/deletefile" [id returnpath] (restricted (handle-delete-file (ensure-prefix id "/") returnpath)))
  (GET "/publishfile/*" [* returnpath] (restricted (handle-publish-file * true returnpath)))
  (GET "/unpublishfile/*" [* returnpath] (restricted (handle-publish-file * false returnpath)))
  (GET "/files/*" [*] (get-file *)))
