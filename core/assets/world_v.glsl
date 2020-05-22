#version 130
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

in vec3 a_position;
in vec4 a_color;
in vec3 a_normal;
in vec3 a_binormal;
in vec3 a_tangent;
in vec2 a_texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_normalMatrix;

uniform vec4 u_cameraPosition;
uniform float u_alphaTest;

out vec2 v_textureCoords;
out vec4 v_materialWeights;
out vec3 v_normal;
out vec3 v_binormal;
out vec3 v_tangent;

out mat3 v_tbn;

out vec3 v_viewDir;
out vec3 v_viewPosition;
out vec3 v_position;
out vec2 v_blendPosition;

out float v_alphaTest;
out float v_fog;

void main() {

    vec4 pos = u_worldTrans * vec4(a_position, 1.0);
    vec3 viewDir = normalize(u_cameraPosition.xyz - pos.xyz);

    v_normal = normalize(u_normalMatrix * a_normal);
    v_binormal = normalize(u_normalMatrix * a_binormal);
    v_tangent = normalize(u_normalMatrix * a_tangent);


    v_tbn = mat3(v_tangent, v_binormal, v_normal);

    v_viewPosition = u_cameraPosition.xyz;

    v_viewDir = normalize(u_cameraPosition.xyz - pos.xyz);

    v_blendPosition = vec2(a_position.x / 64.0, a_position.z / 64.0);

    v_materialWeights = a_color;
    v_textureCoords = a_texCoord0;

    v_position = pos.xyz;

    vec3 distance = u_cameraPosition.xyz - pos.xyz;
    float fog = dot(distance, distance) * u_cameraPosition.w;

    v_fog = min(fog, 1.0);
    v_alphaTest = u_alphaTest;

    gl_Position = u_projViewTrans * pos;
}
