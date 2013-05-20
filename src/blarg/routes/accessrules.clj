(ns blarg.routes.accessrules
  (:require [blarg.routes.auth :as auth]
            [noir.session :as session]))

(defn auth-required [method url params]
  (auth/logged-in?))
