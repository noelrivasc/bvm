(ns bvm.drawing.rectangle
  (:require [quil.core :as q])
  (:import [java.util Random]))

(defn- rotate-point
  "Rotate a point (x, y) around origin by angle (in radians)."
  [x y angle]
  (let [cos-a (Math/cos angle)
        sin-a (Math/sin angle)]
    [(- (* x cos-a) (* y sin-a))
     (+ (* x sin-a) (* y cos-a))]))

(defn rectangle
  "Draws a rectangle (as a polygon) with optional vertex and size variance.

  Options:
    :vertex-variance - Float 0-1. How much corners can be offset from ideal
                       positions. At 1, offset can equal object dimensions.
                       Default: 0.0 (perfect rectangle)
    :size-variance   - Positive float. Random size multiplier added to base size.
                       Total multiplier is (1 + random * size-variance).
                       Default: 0.0 (exact size from transform)
    :seed            - Random seed for reproducibility. Default: 42"
  [drawing-geometry style-map options]
  (let [;; Extract geometry
        cx (:drawing-x drawing-geometry)
        cy (:drawing-y drawing-geometry)
        base-width (:drawing-width drawing-geometry)
        base-height (:drawing-height drawing-geometry)
        rotation (Math/toRadians (:drawing-rotation drawing-geometry))
        index (:index drawing-geometry)
        ;; Extract options with defaults
        vertex-variance (get options :vertex-variance 0.0)
        size-variance (get options :size-variance 0.0)
        seed (get options :seed 42)
        ;; Create seeded random generator for this shape
        rng (Random. (+ seed (* index 1000)))
        ;; Apply size variance: multiply by (1 + random * size-variance)
        size-multiplier (+ 1.0 (* (.nextDouble rng) size-variance))
        width (* base-width size-multiplier)
        height (* base-height size-multiplier)
        half-w (/ width 2.0)
        half-h (/ height 2.0)
        ;; Calculate base corner positions (relative to center)
        ;; Top-left, top-right, bottom-right, bottom-left
        base-corners [[(- half-w) (- half-h)]
                      [half-w (- half-h)]
                      [half-w half-h]
                      [(- half-w) half-h]]
        ;; Apply vertex variance to each corner
        corners (mapv (fn [[bx by]]
                        (let [x-offset (* vertex-variance width (- (* 2.0 (.nextDouble rng)) 1.0))
                              y-offset (* vertex-variance height (- (* 2.0 (.nextDouble rng)) 1.0))]
                          [(+ bx x-offset) (+ by y-offset)]))
                      base-corners)
        ;; Apply rotation and translate to canvas position
        final-corners (mapv (fn [[x y]]
                              (let [[rx ry] (rotate-point x y rotation)]
                                [(+ cx rx) (+ cy ry)]))
                            corners)]
    ;; Set up stroke and fill
    (q/stroke-weight (:stroke-width style-map))
    (apply q/stroke (conj (:stroke-color style-map) (:stroke-opacity style-map)))
    (apply q/fill (conj (:fill-color style-map) (:fill-opacity style-map)))
    ;; Draw polygon
    (q/begin-shape)
    ; Notes for a future curve implementation:
    ; the order shoudl be 4 (control point), 1, 2, 3, 4
    ; (curve vertices), 1, 4 (control points)
    ; use the curve-vertex function instead of vertex
    (doseq [[x y] final-corners]
      (q/vertex x y))
    (q/end-shape :close)))
