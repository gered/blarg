(ns blarg.models.files
  (:use [blarg.models.db]
        [blarg.datetime]
        [blarg.util])
  (:require [com.ashafa.clutch :as couch]
            [clojure.java.io :as io]))

(defn file-exists? [file]
  (let [f (ensure-prefix file "/")]
    (couch/with-db files
      (not (zero? (count (couch/get-view "files" "listPublished" {:key f})))))))

(defn get-file
  ([file] (get-file file false))
  ([file allow-unpublished?]
    (let [f (ensure-prefix file "/")
          view-name (if allow-unpublished? "listAll" "listPublished")]
      (couch/with-db files
        (if-let [file-info (->first-view-value
                             (couch/get-view "files" view-name {:key f}))]
          (let [id         (:_id file-info)
                attachment (:filename file-info)
                attachment-info (second (first (:_attachments file-info)))
                gz (couch/get-attachment id attachment)]
            (if gz
              (merge 
                {:data gz}
                attachment-info))))))))

(defn list-files [path]
  (let [p (ensure-prefix-suffix path "/")]
    (if-let [file-list (->view-values
                         (couch/with-db files
                           (couch/get-view "files" "listAllByPath" {:key p})))]
      (map
        (fn [f]
          (let [attachment (second (first (:_attachments f)))]
            {:id (:_id f)
             :filename (:filename f)
             :last_modified (parse-timestamp (:last_modified_at f))
             :content-type (:content_type attachment)
             :size (:length attachment)
             :published (:published f)}))
        file-list))))

(defn get-tree []
  (->view-keys
    (couch/with-db files
      (couch/get-view "files" "listPaths" {:group true}))))

(defn add-file [path filename file content-type]
  (couch/with-db files
    (let [p (ensure-prefix-suffix path "/")
          id (str p filename)]
      (if-let [doc (couch/put-document {:_id id
                                        :filename filename
                                        :path p
                                        :last_modified_at (get-timestamp)
                                        :published true})]
        (couch/put-attachment doc file :filename filename :mime-type content-type)))))

(defn update-file [id file content-type]
  (couch/with-db files
    (if-let [doc (couch/get-document id)]
      (let [filename (:filename doc)
            attachment (second (first (:_attachments doc)))]
        (if-let [updated-doc (couch/update-document (-> doc
                                                      (assoc :last_modified_at (get-timestamp))))]
          (couch/put-attachment updated-doc file :filename filename :mime-type content-type))))))

(defn delete-file [id]
  (let [safe-id (ensure-prefix id "/")]
    (couch/with-db files
      (if-let [doc (couch/get-document safe-id)]
        (couch/delete-document doc)))))

(defn publish-file [id publish?]
  (let [safe-id (ensure-prefix id "/")]
    (couch/with-db files
      (if-let [doc (couch/get-document safe-id)]
        (couch/update-document (-> doc
                                 (assoc :last_modified_at (get-timestamp))
                                 (assoc :published publish?)))))))
