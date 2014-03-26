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
            [compojure.response :refer [render]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [clj-jtwig.core :as jtwig]
            [clj-jtwig.web.middleware :refer [wrap-servlet-context-path]]
            [blarg.views.layout :as layout]
            [blarg.models.db :as db]))

(defroutes app-routes
  (route/resources "/")
  (layout/render-handler "notfound.html" :status 404))

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
    (jtwig/toggle-compiled-template-caching! false))
  
  (timbre/info "touching database...")
  (db/touch-databases))

(defn destroy []
  (timbre/info "blarg is shutting down..."))

(defn wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (timbre/error e)
        (layout/render-response
          request
          "error.html"
          :params {:errorInfo e}
          :status 500)))))

(def app (app-handler
           [auth-routes home-routes posts-routes files-routes rss-routes app-routes]
           :middleware [wrap-servlet-context-path wrap-exceptions]
           :access-rules [{:redirect "/unauthorized" :rule auth-required}]
           :formats [:json-kw :edn]))
