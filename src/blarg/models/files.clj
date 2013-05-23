(ns blarg.models.files
  (:use [blarg.models.db]
        [blarg.datetime]
        [blarg.util])
  (:require [com.ashafa.clutch :as couch]
            [clojure.java.io :as io]))

(defn get-file [file]
  (let [f (ensure-prefix file "/")]
    (couch/with-db files
    (if-let [file-info (->first-view-value
                         (couch/get-view "files" "listPublished" {:key f}))]
      (let [id         (:_id file-info)
            attachment (:filename file-info)
            attachment-info (second (first (:_attachments file-info)))
            gz (couch/get-attachment id attachment)]
        (if gz
          (merge 
            {:data gz}
            attachment-info)))))))

(defn list-files [path]
  (let [p (ensure-prefix-suffix path "/")]
    (if-let [file-list (->view-values
                       (couch/with-db files
                         (couch/get-view "files" "listPublishedByPath" {:key p})))]
    (map
      (fn [f]
        (let [attachment (second (first (:_attachments f)))]
          {:id (:_id f)
           :filename (:filename f)
           :last_modified (parse-timestamp (:last_modified_at f))
           :content-type (:content_type attachment)
           :size (:length attachment)}))
      file-list))))

(defn get-tree []
  (->view-keys
    (couch/with-db files
      (couch/get-view "files" "listPaths" {:group true}))))

(defn add-file [path filename file content-type]
  (let [p (ensure-prefix-suffix path "/")
        id (str p filename)]
    (if-let [doc (couch/with-db files
                   (couch/put-document {:_id id
                                        :filename filename
                                        :path p
                                        :last_modified_at (get-timestamp)
                                        :published true}))]
      (couch/with-db files
        (couch/put-attachment doc file :filename filename :mime-type content-type)))))
