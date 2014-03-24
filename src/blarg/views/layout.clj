(ns blarg.views.layout
  (:require [selmer.parser :as parser]
            [ring.util.response :as resp]
            [compojure.response :refer [Renderable]]
            [noir.session :as session])
  (:use [blarg.views.viewfilters]))

(def template-path "blarg/views/templates/")

(defn render-template [request template params]
  (parser/render-file
    (str template-path template)
    (assoc params
      :context (:context request)
      :user-id (session/get :user))))

(deftype RenderableTemplate [template params status content-type]
  Renderable
  (render [this request]
    (-> (render-template request template params)
        (resp/response)
        (resp/content-type (or content-type "text/html; charset=utf-8"))
        (resp/status (or status 200)))))

(defn render [template & {:keys [params status content-type]}]
  (RenderableTemplate. template params status content-type))

