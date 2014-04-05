(ns blarg.middleware
  (:require [taoensso.timbre :refer [log]]
            [blarg.views.layout :as layout]))

(defn wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (log :error e "Unhandled exception.")
        (layout/render-response
          request
          "error.html"
          :params {:errorInfo e}
          :status 500)))))