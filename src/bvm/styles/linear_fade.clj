(ns bvm.styles.linear-fade)

(defn- lerp
  "Linear interpolation between a and b by t (0-1)."
  [a b t]
  (+ a (* (- b a) t)))

(defn- lerp-color
  "Linear interpolation between two RGB colors."
  [[r1 g1 b1] [r2 g2 b2] t]
  [(int (lerp r1 r2 t))
   (int (lerp g1 g2 t))
   (int (lerp b1 b2 t))])

(defn linear-fade
  "A style function that linearly interpolates between initial and final values.

  Options (passed via :style-options in sketch config):
    :initial-fill-color   - RGB tuple [r g b] for first object's fill
    :final-fill-color     - RGB tuple [r g b] for last object's fill
    :initial-fill-opacity - Integer 0-255 for first object's fill opacity
    :final-fill-opacity   - Integer 0-255 for last object's fill opacity
    :initial-stroke-color - RGB tuple [r g b] for first object's stroke
    :final-stroke-color   - RGB tuple [r g b] for last object's stroke
    :initial-stroke-width - Integer for first object's stroke width
    :final-stroke-width   - Integer for last object's stroke width
    :initial-stroke-opacity - Integer 0-255 for first object's stroke opacity
    :final-stroke-opacity   - Integer 0-255 for last object's stroke opacity

  Note: :num-steps is automatically injected by the core."
  [_style-config transform-map options]
  (let [index (:index transform-map)
        num-steps (:num-steps options)
        progress (if (<= num-steps 1) 0.0 (/ (double index) (dec num-steps)))
        ;; Extract options with sensible defaults
        initial-fill-color (get options :initial-fill-color [0 0 0])
        final-fill-color (get options :final-fill-color [255 255 255])
        initial-fill-opacity (get options :initial-fill-opacity 255)
        final-fill-opacity (get options :final-fill-opacity 255)
        initial-stroke-color (get options :initial-stroke-color [0 0 0])
        final-stroke-color (get options :final-stroke-color [0 0 0])
        initial-stroke-width (get options :initial-stroke-width 1)
        final-stroke-width (get options :final-stroke-width 1)
        initial-stroke-opacity (get options :initial-stroke-opacity 255)
        final-stroke-opacity (get options :final-stroke-opacity 255)]
    {:fill-color (lerp-color initial-fill-color final-fill-color progress)
     :fill-opacity (int (lerp initial-fill-opacity final-fill-opacity progress))
     :stroke-color (lerp-color initial-stroke-color final-stroke-color progress)
     :stroke-width (int (lerp initial-stroke-width final-stroke-width progress))
     :stroke-opacity (int (lerp initial-stroke-opacity final-stroke-opacity progress))}))
