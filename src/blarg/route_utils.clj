(ns blarg.route-utils
  (:require [clj-metasearch.core :refer [find-vars]]
            [compojure.core :refer [defroutes]]
            [noir.util.route :refer [def-restricted-routes]]
            [taoensso.timbre :refer [log]]
            [clojure.string :as string]))

(defmacro register-routes [name & routes]
  `(defroutes
     ~(with-meta name {:compojure-routes? true})
     ~@routes))

(defmacro register-restricted-routes [name & routes]
  `(def-restricted-routes
     ~(with-meta name {:compojure-routes? true})
     ~@routes))

(defn find-routes [namespace-filter & more-routes]
  (log :info "Discovering routes ...")
  (let [routes (find-vars
                 :compojure-routes?
                 :require-all-namespaces? true
                 :namespace-pred (if (coll? namespace-filter)
                                   (fn [namespace]
                                     (some #(.startsWith (str namespace) %) namespace-filter))
                                   (fn [namespace]
                                     (.startsWith (str namespace) namespace-filter))))
        names  (->> routes
                    (map #(str (:var %)))
                    (string/join ", "))]
    (log :info (str "Found " (count routes) " routes: " names))
    (as-> routes x
          (map :var x)
          (map var-get x)
          (concat x more-routes)
          (vec x))))

