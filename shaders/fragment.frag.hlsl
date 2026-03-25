
struct Input {
  float4 color : COLOR0;
  float2 uv : TEXCOORD0;
};

Texture2D Texture : register(t0, space2);
SamplerState Sampler : register(s0, space2);

cbuffer UniformBlock : register(b0) { float time; };

float4 main(Input input) : SV_Target0 {
  float pulse = sin(time * 2.0) * 0.5 + 0.5; // range [0, 1]
  float4 fromVertex =
      float4(input.color.rgb * (0.8 + pulse * 0.5), input.color.a);
  float4 fromTex = Texture.Sample(Sampler, input.uv);
  return 0.5 * fromTex + 0.5 * fromVertex;
}