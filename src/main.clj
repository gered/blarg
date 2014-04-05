(ns main
  (:require [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.server.standalone :refer [serve]]
            [blarg.handler :refer [handle-app init destroy]])
  (:gen-class))

(defonce server (atom nil))

(defn get-handler []
  (-> #'handle-app
      (wrap-file "resources")
      (wrap-file-info)))

(defn start-server [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port         port
                    :init         init
                    :auto-reload? true
                    :destroy      destroy
                    :join?        false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

(defn -main [& args]
  (start-server))