#pragma once

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

inline void temp_aset(Vertex *p, size_t i, Vertex t) { p[i] = t; }
} // namespace extra
