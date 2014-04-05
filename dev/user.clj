(ns user
  (:use main))

(.start (Thread. #(-main)))

