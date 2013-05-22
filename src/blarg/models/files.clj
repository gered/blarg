(ns blarg.models.files
  (:use [blarg.models.db]
        [blarg.datetime])
  (:require [com.ashafa.clutch :as couch]
            [clojure.java.io :as io]))

(defn get-file [file]
  (couch/with-db files
    (if-let [file-info (->first-view-value
                         (couch/get-view "files" "listPublished" {:key file}))]
      (let [id         (:_id file-info)
            attachment (:filename file-info)
            attachment-info (second (first (:_attachments file-info)))
            gz (couch/get-attachment id attachment)]
        (if gz
          (merge 
            {:data gz}
            attachment-info))))))

(defn list-files [path]
  (if-let [file-list (->view-values
                       (couch/with-db files
                         (couch/get-view "files" "listPublishedByPath" {:key path})))]
    (map
      (fn [f]
        (let [attachment (second (first (:_attachments f)))]
          {:id (:_id f)
           :filename (:filename f)
           :last_modified (parse-timestamp (:last_modified_at f))
           :content-type (:content_type attachment)
           :size (:length attachment)}))
      file-list)))

(defn get-tree []
  (->view-keys
    (couch/with-db files
      (couch/get-view "files" "listPaths" {:group true}))))