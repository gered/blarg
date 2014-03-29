(ns blarg.handler
  (:require [noir.util.middleware :refer [app-handler]]
            [noir.response :as resp]
            [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [clj-jtwig.core :as jtwig]
            [clj-jtwig.web.middleware :refer [wrap-servlet-context-path]]
            [blarg.config :refer [config-val]]
            [blarg.views.layout :as layout]
            [blarg.models.db :as db]
            [blarg.routes.accessrules :refer [auth-required]]
            [blarg.route-utils :refer [find-routes]]))

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

(defonce routes (find-routes "blarg.routes." app-routes))

(defonce app (app-handler
               routes
               :middleware [wrap-exceptions wrap-servlet-context-path]
               :access-rules [{:redirect "/unauthorized" :rule auth-required}]
               :formats [:json-kw :edn]))
