(ns u1stgames.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [u1stgames.core-test]))

(enable-console-print!)

(doo-tests 'u1stgames.core-test)
