varying vec2 v_texCoord;
varying float v_bright;

uniform sampler2D u_texture;

void main() {
    gl_FragColor = texture2D(u_texture, v_texCoord) * vec4(vec3(v_bright), 1);
}
