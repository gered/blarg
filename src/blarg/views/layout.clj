(ns blarg.views.layout
  (:require [clj-jtwig.core :as jtwig]
            [ring.util.response :as resp]
            [compojure.response :refer [Renderable]]
            [noir.session :as session]
            [blarg.config :refer [config-val]])
  (:use [blarg.views.viewfilters]))

(def template-path "views/")

(defn- render-template [request template params]
  (jtwig/render-resource
    (str template-path template)
    (assoc params
      :context (:context request)
      :isDev (= "DEV" (config-val :env))
      :userId (session/get :user))))

(defn render-response [request template & {:keys [params status content-type]}]
  (-> (render-template request template params)
      (resp/response)
      (resp/content-type (or content-type "text/html; charset=utf-8"))
      (resp/status (or status 200))))

(defn render-handler [template & {:keys [params status content-type]}]
  (fn [request]
    (render-response request template :params params :status status :content-type content-type)))

(deftype RenderableTemplate [template params status content-type]
  Renderable
  (render [this request]
    (render-response request template :params params :status status :content-type content-type)))

(defn render [template & {:keys [params status content-type]}]
  (RenderableTemplate. template params status content-type))
