(ns bvm.dev.repl
  (:require [clojure.spec.alpha :as spec]
            [bvm.specs]
            [bvm.core :as bvm]
            [bvm.sketches.minimal :as sketch-minimal]))

(comment
  (spec/explain :bvm.specs/sketch-config sketch-minimal/config)
  (bvm/generate-instructions sketch-minimal/config)
  (bvm/vera sketch-minimal/config))
