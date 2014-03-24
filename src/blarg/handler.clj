(ns blarg.handler
  (:use blarg.routes.home
        blarg.routes.posts
        blarg.routes.auth
        blarg.routes.files
        blarg.routes.rss
        blarg.routes.accessrules
        blarg.config
        ring.middleware.head
        compojure.core)
  (:require [noir.util.middleware :refer [app-handler]]
            [noir.response :as resp]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [blarg.views.layout :as layout]
            [blarg.models.db :as db]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found (layout/render "notfound.html")))

(defn init []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level             :info
     :enabled?              true
     :async?                false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn                    rotor/appender-fn})
  
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "blarg.log" :max-size (* 512 1024) :backlog 10})
  
  (timbre/info "blarg started successfully")

  (when (= "DEV" (config-val :env))
    (timbre/info "Dev environment. Template caching disabled.")
    (parser/toggle-caching))
  
  (timbre/info "touching database...")
  (db/touch-databases))

(defn destroy []
  (timbre/info "blarg is shutting down..."))

(defn wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (.printStackTrace e)
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (layout/render "error.html" {:error-info e})}))))

(def app (app-handler
           [auth-routes home-routes posts-routes files-routes rss-routes app-routes]
           :middleware [wrap-exceptions]
           :access-rules [{:redirect "/unauthorized" :rule auth-required}]
           :formats [:json-kw :edn]))
