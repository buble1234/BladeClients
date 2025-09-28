package win.blade.core.neuro.trainer

import win.blade.core.neuro.model.ModelData
import win.blade.core.neuro.model.ModelType
import win.blade.core.neuro.recorder.RecordFrame
import win.blade.core.neuro.set.DataSet
import com.google.gson.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.random.Random
import java.util.Random as JavaRandom

data class TrainData(
    val input: FloatArray,
    val output: FloatArray
)

class NeuralTrainer {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataDirectory: Path = Paths.get("blade", "neuro", "data")
    private val modelDirectory: Path = Paths.get("blade", "neuro", "models")

    private val bld1Data = mutableListOf<TrainData>()
    private val bld2Data = mutableListOf<TrainData>()

    init {
        try {
            Files.createDirectories(modelDirectory)
        } catch (e: IOException) {
        }
    }

    fun processRecordedData(frames: List<RecordFrame>) {
        bld1Data.clear()
        bld2Data.clear()

        val processedFrames = if (frames.size > 5000) {
            frames.subList(0, 5000)
        } else {
            frames
        }

        processedFrames.forEachIndexed { index, frame ->
            if (index > 0) {
                processBLD1(frame)
                if (index % 2 == 0) processBLD2(frame)
            }
        }

        saveBLD1Model()
        saveBLD2Model()
    }

    private fun processBLD1(frame: RecordFrame) {
        val input = floatArrayOf(
            kotlin.math.min(1f, frame.distance / 10f),
            frame.yaw / 180f,
            frame.pitch / 90f,
            if (frame.targetMoving) 1f else 0f,
            frame.playerHealth / 20f,
            if (frame.isAttacking) 1f else 0f
        )

        val output = floatArrayOf(
            kotlin.math.max(-1f, kotlin.math.min(1f, frame.desiredYawDelta / 20f)),
            kotlin.math.max(-1f, kotlin.math.min(1f, frame.desiredPitchDelta / 20f)),
            frame.desiredSpeed
        )

        bld1Data.add(TrainData(input, output))
    }

    private fun processBLD2(frame: RecordFrame) {
        val baseInput = floatArrayOf(
            kotlin.math.min(1f, frame.distance / 50f),
            frame.yaw / 180f,
            frame.pitch / 90f,
            if (frame.targetMoving) 1f else 0f,
            frame.playerHealth / 20f,
            kotlin.math.min(1f, frame.combatTime / 10000f)
        )

        val baseYawDelta = frame.desiredYawDelta / 20f
        val basePitchDelta = frame.desiredPitchDelta / 20f
        val baseSpeed = frame.desiredSpeed

        bld2Data.add(TrainData(baseInput, floatArrayOf(baseYawDelta.coerceIn(-1f, 1f), basePitchDelta.coerceIn(-1f, 1f), baseSpeed)))

        val numAugmentations = if (frame.targetMoving) 3 else 2
        val javaRandom = JavaRandom()
        repeat(numAugmentations) {
            val timeFactor = (frame.combatTime % 10000).toFloat() / 10000f
            val distFactor = kotlin.math.sin(frame.distance * 0.1f) * 0.1f
            val yawNoise = javaRandom.nextGaussian().toFloat() * 0.1f + kotlin.math.sin(timeFactor * kotlin.math.PI.toFloat() * 2) * 0.05f + distFactor
            val pitchNoise = javaRandom.nextGaussian().toFloat() * 0.1f + kotlin.math.cos(timeFactor * kotlin.math.PI.toFloat() * 2) * 0.05f - distFactor
            val speedNoise = javaRandom.nextGaussian().toFloat() * 0.05f + (if (frame.isAttacking) 0.02f else -0.02f)

            val noisyInput = FloatArray(baseInput.size) { i ->
                when (i) {
                    0 -> (baseInput[i] + Random.nextFloat() * 0.05f - 0.025f).coerceAtLeast(0f).coerceAtMost(1f)
                    1 -> baseInput[i] + yawNoise * 0.1f
                    2 -> baseInput[i] + pitchNoise * 0.1f
                    3 -> baseInput[i]
                    4 -> (baseInput[i] + Random.nextFloat() * 0.02f - 0.01f).coerceAtLeast(0f).coerceAtMost(1f)
                    else -> baseInput[i] + Random.nextFloat() * 0.05f - 0.025f
                }
            }

            val noisyYawDelta = (baseYawDelta + yawNoise).coerceIn(-1f, 1f)
            val noisyPitchDelta = (basePitchDelta + pitchNoise).coerceIn(-1f, 1f)
            val noisySpeed = (baseSpeed + speedNoise).coerceIn(0f, 1f)

            bld2Data.add(TrainData(noisyInput, floatArrayOf(noisyYawDelta, noisyPitchDelta, noisySpeed)))
        }
    }

    private fun saveBLD1Model() {
        try {
            val file = modelDirectory.resolve("bld1_model.json").toFile()
            FileWriter(file).use { writer ->
                gson.toJson(bld1Data, writer)
            }
        } catch (e: IOException) {
        }
    }

    private fun saveBLD2Model() {
        try {
            val file = modelDirectory.resolve("bld2_model.json").toFile()
            FileWriter(file).use { writer ->
                gson.toJson(bld2Data, writer)
            }
        } catch (e: IOException) {
        }
    }

    fun loadModel(modelType: ModelType): List<TrainData> {
        val fileName = when (modelType) {
            ModelType.BLD1 -> "bld1_model.json"
            ModelType.BLD2 -> "bld2_model.json"
        }

        try {
            val file = modelDirectory.resolve(fileName).toFile()
            if (!file.exists()) return emptyList()

            FileReader(file).use { reader ->
                val type = object : com.google.gson.reflect.TypeToken<List<TrainData>>() {}.type
                return gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }
}