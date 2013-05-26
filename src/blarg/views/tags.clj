(ns blarg.views.tags
  (:use [clabango.tags])
  (:require [clabango.filters :refer [context-lookup]]))

(deftemplatetag "ifempty" "endifempty" [[if-node & nodes] context]
  (let [args            (:args if-node)
        body-nodes      (butlast nodes)
        [flip decision] (cond (= 1 (count args))
                              [empty? (first args)]

                              (and (= 2 (count args))
                                   (= "not" (first args)))
                              [not-empty (second args)]

                              :default (throw (Exception. (str "Syntax error: "
                                                               if-node))))]
    {:nodes (if (flip (context-lookup context decision))
              (take-while #(not= "else" (:tag-name %)) body-nodes)
              (rest (drop-while #(not= "else" (:tag-name %)) body-nodes)))}))