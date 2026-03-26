(ns bvm.sketches.multi-layer-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.simple-grid :refer [simple-grid]]
   [bvm.layouts.chaotic-spiral :refer [chaotic-spiral]]
   [bvm.styles.static-test :refer [static-test] :rename {static-test static-style}]
   [bvm.drawing.circle :refer [circle]]))

(def grid-layer
  {:num-steps 16
   :layout-fn simple-grid
   :layout-options {:max-cols 4
                    :gap-x 0.02}
   :style-fn static-style
   :draw-fn circle})

(def spiral-layer
  {:num-steps 30
   :layout-fn chaotic-spiral
   :layout-options {:turns 2
                    :min-radius 0.05
                    :max-radius 0.4
                    :initial-width 0.015
                    :initial-height 0.015
                    :end-width 0.04
                    :end-height 0.04
                    :variance 0.1
                    :dispersion 0.0
                    :direction :clockwise
                    :align-to-path false
                    :seed 42}
   :style-fn static-style
   :draw-fn circle})

(def config
  {:renderer :pdf
   :filename "multi-layer-demo"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :layers [grid-layer spiral-layer]})

(defn -main [& args]
  (bvm/vera-multi config))
