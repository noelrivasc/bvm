# Bonjour Vera Molnár !

A collection of generative art sketches, written in Quil and inspired by the work of Vera Molnár.

## License

Copyright © 2025 Noel Rivas

## Sketch structure

Each sketch is defined by a configuration map, and composed of several functions:

- A layout function to determine the sie, position and rotation of objects to be drawn.
- A styling function to generate the visual properties (colors, stroke, etc) for each object.
- A drawing function that actually puts objects on screen.

See specs.clj

## Running a sketch

See bvm.sketches.minimal.

```clojure
; bvm.sketches.minimal has its own -main function so it can be called directly
; Running through Lein is required to use the dependencies defined in project.clj 
lein run -m bvm.sketches.minimal
```


## Intended output

The current intended output is PDF. The code can be modified to run in the browser but it's not currently set up that way.

## Running a sketch

Run the `vera` function with a valid configuration object:

```clojure
(bvm.core/vera bvm.sketches.minimal/config)
```
