(ns bvm.sketches.spiral-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.chaotic-spiral :refer [chaotic-spiral]]
   [bvm.styles.static-test :refer [static-test] :rename {static-test static-style}]
   [bvm.drawing.circle :refer [circle]]))

(def config
  {:renderer :pdf
   :filename "spiral-demo.pdf"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :num-steps 50
   :layout-fn chaotic-spiral
   :layout-options {:turns 3
                    :min-radius 0.05
                    :max-radius 0.45
                    :initial-width 0.02
                    :initial-height 0.02
                    :end-width 0.06
                    :end-height 0.06
                    :variance 0.2
                    :dispersion 0.0
                    :direction :counter-clockwise
                    :align-to-path false
                    :seed 123}
   :style-fn static-style
   :draw-fn circle})

(defn -main [& args]
  (bvm/vera config))
