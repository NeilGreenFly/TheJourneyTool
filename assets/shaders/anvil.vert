attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec3 a_normal;

varying vec2 v_texCoord;
varying float v_bright;

uniform mat4 u_proj;
uniform mat4 u_trans;

void main() {
    v_texCoord = a_texCoord0;
    v_bright = dot(normalize(mat3(u_trans) * a_normal), vec3(0, 0, 1)) / 4 + 0.75;
    gl_Position = u_proj * u_trans * a_position;
}
