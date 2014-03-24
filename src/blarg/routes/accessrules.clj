(ns blarg.routes.accessrules
  (:require [blarg.routes.auth :as auth]
            [noir.session :as session]))

(defn auth-required [request]
  (auth/logged-in?))
