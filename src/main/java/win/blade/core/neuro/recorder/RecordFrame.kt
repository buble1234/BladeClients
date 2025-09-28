package win.blade.core.neuro.recorder

import win.blade.core.neuro.model.ModelData
import win.blade.core.neuro.model.ModelType
import win.blade.core.neuro.set.DataSet
import com.google.gson.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList

data class RecordFrame(
    val yaw: Float,
    val pitch: Float,
    val distance: Float,
    val isAttacking: Boolean,
    val targetMoving: Boolean,
    val playerHealth: Float,
    val combatTime: Long,
    val desiredYawDelta: Float,
    val desiredPitchDelta: Float,
    val desiredSpeed: Float
)

class NeuralRecorder {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val recordedFrames = CopyOnWriteArrayList<RecordFrame>()
    private val dataDirectory: Path = Paths.get("blade", "neuro", "data")

    private var isRecording = false
    private var sessionName = ""
    private var lastYaw = 0f
    private var lastPitch = 0f

    init {
        try {
            Files.createDirectories(dataDirectory)
        } catch (e: IOException) {
        }
    }

    fun startRecord(name: String = "session_${System.currentTimeMillis()}") {
        isRecording = true
        sessionName = name
        recordedFrames.clear()
    }

    fun stopRecord() {
        if (!isRecording) return

        isRecording = false
        saveSession()
    }

    fun recordFrame(dataSet: DataSet, isAttacking: Boolean, targetMoving: Boolean,
                    playerHealth: Float, combatTime: Long, desiredYawDelta: Float, desiredPitchDelta: Float, desiredSpeed: Float) {
        if (!isRecording) return

        val frame = RecordFrame(
            yaw = dataSet.yaw,
            pitch = dataSet.pitch,
            distance = dataSet.distanceToTarget,
            isAttacking = isAttacking,
            targetMoving = targetMoving,
            playerHealth = playerHealth,
            combatTime = combatTime,
            desiredYawDelta = desiredYawDelta,
            desiredPitchDelta = desiredPitchDelta,
            desiredSpeed = desiredSpeed
        )

        recordedFrames.add(frame)
    }

    private fun saveSession() {
        try {
            val file = dataDirectory.resolve("$sessionName.json").toFile()
            FileWriter(file).use { writer ->
                gson.toJson(recordedFrames, writer)
            }
        } catch (e: IOException) {
        }
    }

    fun getRecordedData(): List<RecordFrame> = recordedFrames.toList()

    fun isRecording(): Boolean = isRecording
}