(ns bvm.sketches.rectangle-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.chaotic-spiral :refer [chaotic-spiral]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.rectangle :refer [rectangle]]))

(def config
  {:renderer :pdf
   :filename "rectangle-demo.pdf"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :num-steps 45
   :layout-fn chaotic-spiral
   :layout-options {:turns 3
                    :min-radius 0.04
                    :max-radius 0.44
                    :initial-width 0.03
                    :initial-height 0.03
                    :end-width 0.09
                    :end-height 0.09
                    :variance 0.05
                    :dispersion 0.0
                    :direction :clockwise
                    :align-to-path true
                    :seed 77}
   :style-fn linear-fade
   :style-options {:initial-fill-color [225 190 160]      ; warm sand
                   :final-fill-color [120 145 135]        ; sage green
                   :initial-fill-opacity 240
                   :final-fill-opacity 200
                   :initial-stroke-color [180 130 110]    ; terracotta
                   :final-stroke-color [85 110 125]       ; slate blue
                   :initial-stroke-width 1
                   :final-stroke-width 4
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 220}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.12
                     :size-variance 0.15
                     :seed 42}})

(defn -main [& args]
  (bvm/vera config))
