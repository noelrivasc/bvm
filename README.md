# Bonjour Vera Molnár !

A collection of generative art sketches, written in Quil and inspired by the work of Vera Molnár.

## License

Copyright © 2025 Noel Rivas

## Sketch structure

The definition of a sketch has evolved with the project. Initially, it was a drawing defined by a configuration map, which included a layout function, a style function and a drawing function. See notes about them below under _Particle drawing_.

In the most recent version, I think of a sketch in the following terms:

### Particles

A Particle is a single object (a square, circle, line... ) that's drawn by a drawing function.

Particles are essential to a sketch, but aren't produced manually; they are the outcomes of processing a Stroke (layer) configuration.

### Strokes

A Stroke is a collection of Particles. It's defined by the triad of layout, style and drawing function.

In the configuration spec and the core functions, it's referred to as a _layer_.

Strokes are essential to a sketch. Even for single-particle drawing, a stroke has to be used to generate its configuration.

### Fields

A Field is a collection of Strokes. The concept is derived from the wave_depth sketch, in which I used a function to produce a set of layers, which define something that looks like a color field (or plane, passage, patch, whatever you want to call an identifiable surface on a painting).

Fields are not essential for a sketch; they are helpers that aid in the creation of stroke progressions.

## Particle drawing

### Layout function

Determines the size, position and rotation of objects to be drawn.

Takes a step and num-steps as input, and produces a Transform Map.

### Style function

Determines the visual properties (colors, stroke, etc) for each object.

Takes a transform map and a style config, and produces a Style Map.

### Drawing function

Actually draws objects on the canvas. Takes a geometry map (which is the result of projecting the Transform Map to the canvas) and a style map.

See specs.clj

## Running a sketch

See bvm.sketches.minimal.

```bash
# bvm.sketches.minimal has its own -main function so it can be called directly
# Running through Lein is required to use the dependencies defined in project.clj 
lein run -m bvm.sketches.minimal
```

## Interactive development

Start the REPL server. The simplest way is to do that with Leiningen:

```bash
lein repl
```

While you can use the REPL server directly, Rebel readline provides a better experience. You caon connect to a running REPL server with:

```bash
./scripts/nrebel.sh
```

Vim-fireplace allows you to run expressions from within Vim.

- `cqq` prepopulate the command window with the expression under the cursor.
- `cqc` blank line in command window
- 

## Intended output

The current intended output is PNG or PDF. The code can be modified to run in the browser but it's not currently set up that way.

## Running a sketch

Run the `vera` function with a valid configuration object:

```clojure
(bvm.core/vera bvm.sketches.minimal/config)
```
