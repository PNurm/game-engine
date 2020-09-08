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

struct Material {
    vec4 ambientColor, diffuseColor, specularColor;
    sampler2D ambientTexture, diffuseTexture, normalTexture, specularTexture;
    float shininess;
};

uniform Material materials[4];
uniform sampler2D u_blendTexture;

struct Light {
	vec3 position, direction;
	vec3 ambient, diffuse, specular;
};

uniform Light lights[4];
uniform Light u_directionalLight;
uniform int u_numLights;

uniform vec4 u_diffuseColor;

uniform int u_debugNormals;

in vec2 v_textureCoords;
in vec3 v_normal;
in mat3 v_tbn;

in vec3 v_viewDir;
in vec4 v_viewPosition;
in vec3 v_position;
in vec2 v_blendPosition;

out float v_alphaTest;
out float v_fog;

out vec4 fragColor;

vec4 sampleDiffuse (int id) {
    return vec4(texture(materials[id].diffuseTexture, v_textureCoords));
}
vec3 sampleNormal (int id) {
    return vec3(texture(materials[id].normalTexture, v_textureCoords));
}

vec3 calculatePointLight(Light light, vec3 normal, vec3 worldPosition, vec3 viewDir, vec3 diffuseT, float shininess) {

    vec3 lightDir = normalize(light.position - worldPosition);

    //How much in same direction are normals & light pointing.
    float nDotLight = clamp(dot(normal, lightDir), 0.0, 1.0);

    // specular shading
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float specularity = pow(max(dot(normal, halfwayDir), 0.0), shininess);

    // attenuation
    float distance    = length(light.position - worldPosition);
    float attenuation = 1.0 / (1.0 + 0.09 * distance + 0.032 * (distance * distance));

    // combine results
    vec3 diffuse  = light.diffuse  * nDotLight * diffuseT;
    vec3 specular = light.specular * specularity;

    diffuse *= attenuation;
    specular *= attenuation;

    return (diffuse + specular);
}

vec3 calculateDirLight(Light directionalLight, vec3 normal, vec3 viewDir, vec3 diffuseT, float shininess)
{
    //Invert light direction so it will point away from the normal
    vec3 lightDir = normalize(-directionalLight.direction);

    //How much in same direction are normals & light pointing.
    float nDotLight = clamp(dot(normal, lightDir), 0.0, 1.0);

    // specular shading
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float specularity = pow(max(dot(normal, halfwayDir), 0.0), shininess);

    // combine results
    vec3 diffuse  = directionalLight.diffuse * nDotLight * diffuseT;
    vec3 specular = directionalLight.specular * specularity;

    return (diffuse + specular);
}

void main()
{

    vec3 diffuseNull = vec3(1f, 0f, 1f);

    vec3 normal = normalize(v_normal);

    float shininess = 0;
    vec4 diffuseT = vec4(0);
    vec3 normalT = vec3(0);

    vec4 blend = texture2D(u_blendTexture, v_blendPosition);

    float whiteWeight = 1.0 - blend.r - blend.g - blend.b - blend.a;
    diffuseT += vec4(1.0) * whiteWeight;
    for(int i = 0; i < 4;i++) {
        vec4 diff = sampleDiffuse(i);
        float weight = blend[i];

        diffuseT.rgb += sampleDiffuse(i).rgb * weight;
        normalT.rgb += sampleNormal(i).rgb * weight;
       // diffuseT = mix(diffuseT, sampleDiffuse(i), weight);
        //normalT = mix(normalT, sampleNormal(i), weight).rgb;
    }
    if(normalT.r != 0) {
        normal = normalize(normalT * 2.0 - 1.0);
        //normal = normalize(v_tbn * normal);
    }
    vec3 lightDiffuse = vec3(0.0);
    vec3 lightSpecular = vec3(0.0);

    lightDiffuse += calculateDirLight(u_directionalLight, normal, v_viewDir, diffuseT.rgb, shininess);

    for(int i = 0; i < u_numLights; i++) {
        Light light = lights[i];
        lightDiffuse += calculatePointLight(light, normal, v_position, v_viewDir, diffuseT.rgb, shininess);
    }

    lightDiffuse = max(lightDiffuse, 0.2);

    fragColor.rgb = lightDiffuse;
    if(u_debugNormals == 1) {
        fragColor.rgb = abs(normal);
    }
}
