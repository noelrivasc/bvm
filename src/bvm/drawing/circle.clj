(ns bvm.drawing.circle
  (:require [quil.core :as q]))

(defn circle [drawing-geometry style-map]
  (prn drawing-geometry)
  (q/stroke-weight (:stroke-width style-map))
  (apply q/stroke (conj (:stroke-color style-map) (:stroke-opacity style-map)))
  (apply q/fill (conj (:fill-color style-map) (:fill-opacity style-map)))
  (q/ellipse (:drawing-x drawing-geometry) (:drawing-y drawing-geometry) (:drawing-width drawing-geometry) (:drawing-height drawing-geometry)))
