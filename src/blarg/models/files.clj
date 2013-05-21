(ns blarg.models.files
  (:use [blarg.models.db])
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