(ns blarg.handler
  (:require [noir.util.middleware :refer [app-handler]]
            [noir.response :as resp]
            [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [taoensso.timbre :refer [set-config! log]]
            [clj-jtwig.core :as jtwig]
            [clj-jtwig.web.middleware :refer [wrap-servlet-context-path]]
            [blarg.config :refer [config-val]]
            [blarg.views.layout :as layout]
            [blarg.models.db :as db]
            [blarg.routes.accessrules :refer [auth-required]]
            [blarg.route-utils :refer [find-routes]]
            [blarg.util :refer [log-formatter]]))

(defroutes app-routes
  (route/resources "/")
  (layout/render-handler "notfound.html" :status 404))

(defn init []
  (set-config! [:shared-appender-config :spit-filename] "blarg.log")
  (set-config! [:appenders :spit :enabled?] true)
  (set-config! [:fmt-output-fn] log-formatter)

  (log :info "blarg started successfully")

  (when (= "DEV" (config-val :env))
    (log :info "Dev environment. Template caching disabled.")
    (jtwig/toggle-compiled-template-caching! false))
  
  (log :info "touching database...")
  (db/touch-databases))

(defn destroy []
  (log :info "blarg is shutting down..."))

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

(defonce routes (find-routes "blarg.routes." app-routes))

(defonce app (app-handler
               routes
               :middleware [wrap-exceptions wrap-servlet-context-path]
               :access-rules [{:redirect "/unauthorized" :rule auth-required}]
               :formats [:json-kw :edn]))
