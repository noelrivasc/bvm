(ns bvm.utils.interpolation)

(defn lerp
  "Linear interpolation of a to b at position t.
   a and b can be any two numbers; t must be a float
   [0, 1]"
  [a b t] (+ a (* (- b a) t)))
