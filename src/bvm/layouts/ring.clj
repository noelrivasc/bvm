(ns bvm.layouts.ring)

(defn ring
  "Arranges objects along an ellipse.

  Options:
    :ellipse-width  - Required. Width of ellipse (fraction of canvas).
    :ellipse-height - Required. Height of ellipse (fraction of canvas).
    :object-width   - Required. Width of objects (fraction of canvas).
    :object-height  - Required. Height of objects (fraction of canvas).
    :start          - Optional. Starting position (0-1). Default: 0.
                      0 = top, 0.25 = right, 0.5 = bottom, 0.75 = left.
    :completion     - Optional. How much of ellipse to span (0-1). Default: 1.
                      1 = full ellipse, 0.5 = half ellipse.
    :center-x       - Optional. X center of ellipse (0-1). Default: 0.5.
    :center-y       - Optional. Y center of ellipse (0-1). Default: 0.5.
    :align-to-path  - Optional. Rotate objects to follow ellipse. Default: false."
  [index num-steps options]
  (let [;; Required options
        ellipse-width (:ellipse-width options)
        ellipse-height (:ellipse-height options)
        object-width (:object-width options)
        object-height (:object-height options)
        ;; Optional with defaults
        start (get options :start 0.0)
        completion (get options :completion 1.0)
        center-x (get options :center-x 0.5)
        center-y (get options :center-y 0.5)
        align-to-path (get options :align-to-path false)
        ;; Calculate progress along the arc (0 to 1)
        progress (if (<= num-steps 1)
                   0.0
                   (/ (double index) (dec num-steps)))
        ;; Convert to angle (in radians)
        ;; Start from top (-π/2) and go clockwise
        ;; position 0 = top, 0.25 = right, 0.5 = bottom, 0.75 = left
        angle-fraction (+ start (* progress completion))
        angle (- (* angle-fraction 2.0 Math/PI) (/ Math/PI 2.0))
        ;; Calculate position on ellipse
        rx (/ ellipse-width 2.0)
        ry (/ ellipse-height 2.0)
        x (+ center-x (* rx (Math/cos angle)))
        y (+ center-y (* ry (Math/sin angle)))
        ;; Calculate rotation if aligning to path
        ;; Tangent angle for ellipse at angle θ: atan2(rx * cos(θ), -ry * sin(θ))
        rotation (if align-to-path
                   (Math/toDegrees
                    (Math/atan2 (* rx (Math/cos angle))
                                (- (* ry (Math/sin angle)))))
                   0.0)]
    {:index index
     :x (float (max 0.0 (min 1.0 x)))
     :y (float (max 0.0 (min 1.0 y)))
     :width (float object-width)
     :height (float object-height)
     :rotation (float rotation)}))
