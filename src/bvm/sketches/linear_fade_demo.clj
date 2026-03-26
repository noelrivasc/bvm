(ns bvm.sketches.linear-fade-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.chaotic-spiral :refer [chaotic-spiral]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.circle :refer [circle]]))

(def config
  {:renderer :pdf
   :filename "linear-fade-demo"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :num-steps 40
   :layout-fn chaotic-spiral
   :layout-options {:turns 3
                    :min-radius 0.03
                    :max-radius 0.42
                    :initial-width 0.025
                    :initial-height 0.025
                    :end-width 0.08
                    :end-height 0.08
                    :variance 0.0
                    :dispersion 0.0
                    :direction :counter-clockwise
                    :align-to-path false
                    :seed 42}
   :style-fn linear-fade
   :style-options {:initial-fill-color [210 145 125]      ; faded coral
                   :final-fill-color [95 145 140]         ; muted seafoam
                   :initial-fill-opacity 255
                   :final-fill-opacity 180
                   :initial-stroke-color [165 105 85]     ; dusty terracotta
                   :final-stroke-color [95 125 145]       ; washed denim
                   :initial-stroke-width 1
                   :final-stroke-width 6
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 200}
   :draw-fn circle})

(defn -main [& args]
  (bvm/vera config))
