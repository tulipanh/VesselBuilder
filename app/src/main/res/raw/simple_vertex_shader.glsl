uniform mat4 u_MVPMatrix;
uniform mat4 u_MMatrix;
uniform vec4 u_Color;
uniform vec3 u_VectorToLight;

attribute vec3 a_Normal;
attribute vec4 a_Position;

varying vec4 v_Color;

void main()                    
{
    vec3 modelNormal = vec3(u_MMatrix * vec4(a_Normal, 0.0));
    float diffuse = max(dot(modelNormal, u_VectorToLight), 0.0);
    float ambient = 0.2;
    v_Color = u_Color;
    v_Color *= diffuse;
    v_Color += ambient;
    gl_Position = u_MVPMatrix * a_Position;
} 