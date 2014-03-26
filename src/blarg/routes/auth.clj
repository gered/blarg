(ns blarg.routes.auth
  (:use [blarg.routes.helpers]
        [compojure.core]
        [noir.util.route])
  (:require [blarg.views.layout :as layout]
            [blarg.models.users :as users]
            [ring.util.response :refer [status]]
            [ring.middleware.head :refer [wrap-head]]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]))

(defn logged-in? []
  (not (nil? (session/get :user))))

(defn login-page []
  (if (logged-in?)
    (resp/redirect "/")
    (layout/render
      "auth/login.html"
      :params {:loginError (session/flash-get :login-error)
               :htmlTitle  (->html-title ["Login"])})))

(defn handle-login [id pass]
  (if-let [user (users/get-user id pass)]
    (do
        (session/put! :user id)
        (resp/redirect "/"))
    (do
        (session/flash-put! :login-error "Invalid username/password.")
        (resp/redirect "/login"))))

(defn logout []
  (session/clear!)
  (resp/redirect "/"))

(defn handle-unauthorized []
  (layout/render "unauthorized.html" :status 401))

(defroutes auth-routes
  (GET "/unauthorized" [] (handle-unauthorized))
  (GET "/login" [] (login-page))
  (POST "/login" [id pass] (handle-login id pass))
  (GET "/logout" [] (logout))
  (GET "/private" [] (restricted "private!")))
