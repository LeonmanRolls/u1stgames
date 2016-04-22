(ns u1stgames.utils
  (:require
    [cljs-uuid-utils.core :as uuid]))

(defn uid-gen
  ([] (uid-gen "prefix"))
  ([prefix]
   (str prefix "_" (uuid/uuid-string (uuid/make-random-uuid)))))


