(ns bvm.styles.lch-fade
  "Like `bvm.styles.linear-fade`, but interpolates colours in LCH along the
   shortest-hue arc, then converts to RGB bytes at the last step. Non-colour
   attributes (opacity, stroke width) stay linear.

   Options (via :style-options):
     :initial-fill-color     - LCH tuple [L C H] for first object's fill
     :final-fill-color       - LCH tuple [L C H] for last object's fill
     :initial-fill-opacity   - Integer 0-255
     :final-fill-opacity     - Integer 0-255
     :initial-stroke-color   - LCH tuple [L C H]
     :final-stroke-color     - LCH tuple [L C H]
     :initial-stroke-width   - Integer
     :final-stroke-width     - Integer
     :initial-stroke-opacity - Integer 0-255
     :final-stroke-opacity   - Integer 0-255
     :color-space            - :srgb (default) or :adobe-rgb — target gamut
                               for the final LCH -> RGB conversion.

   Note: :num-steps is injected by the core."
  (:require [bvm.utils.interpolation :refer [lerp]]
            [bvm.utils.color :refer [lerp-lch lch->rgb-bytes]]))

(defn lch-fade
  [_style-config transform-map options]
  (let [index (:index transform-map)
        num-steps (:num-steps options)
        progress (if (<= num-steps 1) 0.0 (/ (double index) (dec num-steps)))
        color-space (get options :color-space :srgb)
        initial-fill (get options :initial-fill-color [0 0 0])
        final-fill (get options :final-fill-color [100 0 0])
        initial-stroke (get options :initial-stroke-color [0 0 0])
        final-stroke (get options :final-stroke-color [0 0 0])
        fill-lch (lerp-lch initial-fill final-fill progress)
        stroke-lch (lerp-lch initial-stroke final-stroke progress)
        initial-fill-opacity (get options :initial-fill-opacity 255)
        final-fill-opacity (get options :final-fill-opacity 255)
        initial-stroke-width (get options :initial-stroke-width 1)
        final-stroke-width (get options :final-stroke-width 1)
        initial-stroke-opacity (get options :initial-stroke-opacity 255)
        final-stroke-opacity (get options :final-stroke-opacity 255)]
    {:fill-color (mapv int (lch->rgb-bytes fill-lch color-space))
     :fill-opacity (int (lerp initial-fill-opacity final-fill-opacity progress))
     :stroke-color (mapv int (lch->rgb-bytes stroke-lch color-space))
     :stroke-width (int (lerp initial-stroke-width final-stroke-width progress))
     :stroke-opacity (int (lerp initial-stroke-opacity final-stroke-opacity progress))}))
