#pragma once

#include <SDL3/SDL.h>
#include <cstddef>

namespace extra {
struct Vertex {
  float x, y, z;
  float r, g, b, a;
  float u, v;
};

struct UniformBuffer {
  float time;
};

static UniformBuffer timeUniform{};

// https://clojurians.slack.com/archives/C03SRH97FDK/p1757647872126659?thread_ts=1757644640.338809&cid=C03SRH97FDK
inline void temp_aset(Vertex *p, size_t i, Vertex t) { p[i] = t; }

typedef struct PositionTextureVertex {
  float x, y, z;
  float u, v;
} PositionTextureVertex;

inline void aset(PositionTextureVertex *p, size_t i, PositionTextureVertex t) {
  p[i] = t;
}

inline void aset(Uint16 *p, size_t i, Uint16 t) { p[i] = t; }

} // namespace extra
