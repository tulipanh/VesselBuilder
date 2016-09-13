uniform mat4 u_Matrix;
uniform vec4 u_GoodColor;
uniform vec4 u_BadColor;

attribute vec4 a_Position;
attribute float a_ColorBool;

varying vec4 v_Color;

void main() {
    if (a_ColorBool > 0.5) {
        v_Color = u_GoodColor;
    } else {
        v_Color = u_BadColor;
    }

    gl_Position = u_Matrix * a_Position;
    gl_PointSize = 5.0;

}
