#version 130

in vec3 a_position;
in vec4 a_color;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

out vec4 v_color;
void main()
{
    vec4 pos = u_worldTrans * vec4(a_position, 1.0);

    v_color = a_color;

    gl_Position = u_projViewTrans * pos;
}