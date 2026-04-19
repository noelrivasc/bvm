(ns bvm.layouts.ring
  (:import [java.util Random]))

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
    :align-to-path  - Optional. Rotate objects to follow ellipse. Default: false.
    :variance       - Optional. Random radial offset (0-1), scaled by the
                      larger of the two semi-axes. Default: 0.0.
    :seed           - Optional. Random seed for reproducibility. Default: 42."
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
        variance (get options :variance 0.0)
        seed (get options :seed 42)
        rng (Random. (+ seed index))
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
        ;; Radial variance: random offset in [-variance*r, +variance*r]
        ;; where r is the larger semi-axis (keeps jitter feeling proportional).
        r-max (max rx ry)
        radial-offset (* variance r-max (- (* 2.0 (.nextDouble rng)) 1.0))
        cos-a (Math/cos angle)
        sin-a (Math/sin angle)
        x (+ center-x (* (+ rx radial-offset) cos-a))
        y (+ center-y (* (+ ry radial-offset) sin-a))
        ;; Calculate rotation if aligning to path
        ;; Tangent angle for ellipse at angle θ: atan2(rx * cos(θ), -ry * sin(θ))
        rotation (if align-to-path
                   (Math/toDegrees
                    (Math/atan2 (* rx cos-a)
                                (- (* ry sin-a))))
                   0.0)]
    {:index index
     :x (float (max 0.0 (min 1.0 x)))
     :y (float (max 0.0 (min 1.0 y)))
     :width (float object-width)
     :height (float object-height)
     :rotation (float rotation)}))
