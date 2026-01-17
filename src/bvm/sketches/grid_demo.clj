(ns bvm.sketches.grid-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.simple-grid :refer [simple-grid]]
   [bvm.styles.static-test :refer [static-test] :rename {static-test static-style}]
   [bvm.drawing.circle :refer [circle]]))

(def config
  {:renderer :pdf
   :filename "grid-demo.pdf"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :num-steps 12
   :layout-fn simple-grid
   :layout-options {:max-cols 4
                    :gap-x 0.02}
   :style-fn static-style
   :draw-fn circle})

(defn -main [& args]
  (bvm/vera config))
