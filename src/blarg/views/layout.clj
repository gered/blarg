(ns blarg.views.layout
  (:use noir.request)
  (:require [clabango.parser :as parser]
            [noir.session :as session]
            [blarg.views.viewfilters]))

(def template-path "blarg/views/templates/")

(defn render [template & [params]]
  (parser/render-file (str template-path template)
                      (assoc params 
                        :context (:context *request*)
                        :user-id (session/get :user))))
