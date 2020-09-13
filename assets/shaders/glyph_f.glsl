#version 130

out vec4 fragColor;
in vec4 v_color;

void main()
{
    fragColor.rgb = v_color.rgb;
}