(ns blarg.handler
  (:use blarg.routes.home
        blarg.routes.posts
        blarg.routes.auth
        blarg.routes.files
        blarg.routes.rss
        blarg.routes.accessrules
        compojure.core)
  (:require [noir.util.middleware :as middleware]
            [noir.response :as resp]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]
            [blarg.views.layout :as layout]
            [blarg.models.db :as db]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/append})
  
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "blarg.log" :max-size 10000 :backlog 10})
  
  (timbre/info "blarg started successfully")
  
  (timbre/info "touching database...")
  (db/touch-databases))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "blarg is shutting down..."))

(defn wrap-exceptions [app]
  (fn [request]
    (try
      (app request)
      (catch Exception e
        (.printStackTrace e)
        (->> (layout/render "error.html" {:error-info e})
             (resp/status 500))))))

;;append your application routes to the all-routes vector
(def all-routes [auth-routes home-routes posts-routes files-routes rss-routes])

(def app (middleware/app-handler
           (conj all-routes app-routes)
           :middleware [wrap-exceptions]
           :access-rules [[{:redirect "/unauthorized"} auth-required]]))

(def war-handler (middleware/war-handler app))
