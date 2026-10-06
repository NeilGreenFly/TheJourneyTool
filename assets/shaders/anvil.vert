attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord;

uniform mat4 u_proj;
uniform mat4 u_trans;

void main() {
    v_texCoord = a_texCoord0;
    gl_Position = u_proj * u_trans * a_position;
}
