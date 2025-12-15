# Compose Dice Roller – CPU Pseudo-3D Playground

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-orange) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-blue) 
A Kotlin CPU-only pseudo-3D playground exploring 3D math, projections, and interaction, culminating in a touch-driven dice prototype.

> **Not a rendering engine, physics simulator, or production library.**  
> Experimental sandbox for step-by-step 3D understanding without GPU APIs.

---

## Motivation

Explore 3D rendering concepts from first principles:

- Explicit 3D geometry modeling  
- Manual rotations and transformations  
- 3D → 2D projection  
- User interaction without a scene graph or physics engine  

The dice acts as a concrete object to anchor experiments, prioritizing clarity over performance.

---

## Scope & Limitations

**Avoids:**  

- OpenGL, Vulkan, shaders  
- Scene graphs or hierarchical transforms  
- Physically accurate dice simulation  
- Performance optimization  

**Focuses on:**  

- Explicit data models  
- Handcrafted math primitives  
- Incremental visual experiments  
- Clear separation: configuration, state, math, rendering  

---

## Core Model

### Cube & Dice Configuration

- **CubeConfig** – local vertices + indexed faces  
- **FaceConfig** – face color, optional pips  
- Variants:
  - Classic dice layout  
  - Uniform faces  
  - Ghost cubes (layered visual effects)  

### Animation Config

- **DiceAnimationConfig** – animation intent: state, target value, rotation intensity, duration  
- Timing & interpolation handled externally  

---

## State & Interaction

- **DiceState** – `IDLE`, `ROLLING`, `LANDING` (visual behavior only)  
- **LayerLockState** – lock cube layers to rotation  
- **RotationState** – mutable rotation angles per cube  

---

## Math Primitives

- **Vec3** – 3D vector with add, scale, dot, cross, rotateX/Y  
- **Pip** – normalized 2D position (0..1) for dice pips  

> No matrices, quaternions, or external libraries. Low-level approach for transparency and learning.

---

## UI Experiments

Composables visualize:

- Cube wireframes  
- Face filling + depth ordering  
- Shading & highlights  
- Drag-driven rotation  
- Layered dice compositions  

> Visual checkpoints documenting rendering evolution, not reusable widgets.

---

## Visual Progression

### Core 3D Cube

1. **InteractiveCube (V1)** – basic rotation, colored faces, damping  
2. **InteractiveCubeV2** – gradient effect, refined strokes  
3. **InteractiveCubeV3** – finger-based highlights & reflections  
4. **InteractiveCubeV4** – Fresnel effects, refined alpha & gradient  

### Cube Cavity Containers

1. **CubeCavityContainerV1** – hollow cube, central content  
2. **CubeCavityContainerV2** – nested cubes, lag, depth sorting, optional strokes  
3. **CubeCavityContainerV3** – toggles per cube, edge visibility, per-axis inversion  

### Interactive Cube with Nested Variants

1. **InteractiveCubeWith3Nested (V1)** – parent + 2 nested cubes, basic rotation & damping  
2. **InteractiveCubeWith3NestedCrystal (V2)** – pulse on innermost cube, Fresnel lighting  
3. **InteractiveCubeWith3NestedLag (V3)** – lag interpolation for inner cube  
4. **InteractiveCubeWith3NestedShiny (V4)** – reflections, glow, dynamic shading, gradient faces  

### Interactive Dice Composables

1. **InteractiveDiceComposable (V1)** – single dice, pips, shading, optional wireframe  
2. **InteractiveDiceNested (V2)** – multi-layer dice, per-layer lag, color, alpha, inversion, layer locks  
3. **NestedInteractiveDice (V3)** – full multi-layer dice with animation states, bounce/squash, callbacks, lag & rotation  

---

## Status

- Learning & experimentation sandbox  
- Not maintained, not a library  
- Code reflects exploration over production constraints  

---


