(ns bvm.sketches.minimal
  (:require
   [bvm.layouts.static-test :refer [static-test] :rename {static-test static-layout}]
   [bvm.styles.static-test :refer [static-test] :rename {static-test static-style}]
   [bvm.drawing.circle :refer [circle]]))

(def config
  {:renderer :pdf
   :filename "minimal.pdf"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :num-steps 1
   :layout-fn static-layout
   :style-fn static-style
   :draw-fn circle})
