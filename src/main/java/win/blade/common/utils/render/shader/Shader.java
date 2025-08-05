package win.blade.common.utils.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.commons.io.IOUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programId;
    private final Map<String, Integer> uniformCache = new HashMap<>();

    public Shader(String path, String shaderName) throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Не удалось создать шейдерную программу");
        }

        try {
            String basePath = "/assets/blade/shaders/core/" + path + "/";
            String vertexShaderSource = readShaderSource(basePath + shaderName + ".vsh");
            String fragmentShaderSource = readShaderSource(basePath + shaderName + ".fsh");

            glAttachShader(programId, createShader(vertexShaderSource, GL_VERTEX_SHADER));
            glAttachShader(programId, createShader(fragmentShaderSource, GL_FRAGMENT_SHADER));

            glLinkProgram(programId);

            if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
                throw new IllegalStateException("Ошибка связывания шейдера: " + glGetProgramInfoLog(programId));
            }
        } catch (Exception e) {
            glDeleteProgram(programId);
            throw new RuntimeException("Не удалось создать шейдер: " + shaderName, e);
        }
    }

    private String readShaderSource(String path) throws Exception {
        InputStream stream = Shader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new Exception("Файл шейдера не найден: " + path);
        }
        return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }

    private int createShader(String source, int type) throws Exception {
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw new Exception("Ошибка при создании шейдера типа " + type);
        }
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            String log = glGetShaderInfoLog(shader, 1024);
            glDeleteShader(shader);
            throw new Exception("Ошибка компиляции шейдера типа " + type + ": " + log);
        }
        return shader;
    }

    public void bind() {
        glUseProgram(this.programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    private int getUniformLocation(String name) {
        return uniformCache.computeIfAbsent(name, n -> glGetUniformLocation(this.programId, n));
    }

    public void setUniform1i(String name, int value) {
        RenderSystem.assertOnRenderThread();
        glUniform1i(getUniformLocation(name), value);
    }

    public void setUniform1f(String name, float value) {
        RenderSystem.assertOnRenderThread();
        glUniform1f(getUniformLocation(name), value);
    }

    public void setUniform2f(String name, float v1, float v2) {
        RenderSystem.assertOnRenderThread();
        glUniform2f(getUniformLocation(name), v1, v2);
    }

    public void setUniform2f(String name, Vector2f vector) {
        RenderSystem.assertOnRenderThread();
        glUniform2f(getUniformLocation(name), vector.x, vector.y);
    }

    public void setUniform3f(String name, Vector3f vector) {
        RenderSystem.assertOnRenderThread();
        glUniform3f(getUniformLocation(name), vector.x, vector.y, vector.z);
    }

    public void setUniformBool(String name, boolean value) {
        setUniform1i(name, value ? 1 : 0);
    }

    public void delete() {
        if (programId != 0) {
            glDeleteProgram(this.programId);
        }
    }
}