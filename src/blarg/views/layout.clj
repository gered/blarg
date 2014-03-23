(ns blarg.views.layout
  (:require [selmer.parser :as parser]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [noir.session :as session])
  (:use [blarg.views.viewfilters]))

(def template-path "blarg/views/templates/")

(deftype RenderableTemplate [template params]
  Renderable
  (render [this request]
    (content-type
      (response
        (parser/render-file
          (str template-path template)
          (assoc params
            :context (:context request)
            :user-id (session/get :user))))
      "text/html; charset=utf-8")))

(defn render [template & [params]]
  (RenderableTemplate. template params))
