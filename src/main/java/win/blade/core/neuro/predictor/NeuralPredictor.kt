package win.blade.core.neuro.predictor

import win.blade.core.neuro.model.ModelData
import win.blade.core.neuro.model.ModelType
import win.blade.core.neuro.set.DataSet
import win.blade.core.neuro.trainer.TrainData
import win.blade.core.neuro.trainer.NeuralTrainer

class NeuralPredictor {
    private val trainer = NeuralTrainer()
    private var bld1Model: List<TrainData> = emptyList()
    private var bld2Model: List<TrainData> = emptyList()
    private var isActive = false

    fun start() {
        loadModels()
        isActive = true
    }

    fun stop() {
        isActive = false
    }

    private fun loadModels() {
        bld1Model = trainer.loadModel(ModelType.BLD1)
        bld2Model = trainer.loadModel(ModelType.BLD2)
    }

    fun predict(dataSet: DataSet, modelType: ModelType, targetMoving: Boolean,
                playerHealth: Float, combatTime: Long, isAttacking: Boolean): ModelData {
        if (!isActive) return ModelData()

        val model = when (modelType) {
            ModelType.BLD1 -> bld1Model
            ModelType.BLD2 -> bld2Model
        }

        if (model.isEmpty()) return ModelData()

        val input = createInput(dataSet, modelType, targetMoving, playerHealth, combatTime, isAttacking)
        val result = findBestMatch(input, model)

        return ModelData(
            yaw = result[0] * 20f,
            pitch = result[1] * 20f,
            speed = result[2],
            distanceToTarget = dataSet.distanceToTarget
        )
    }

    private fun findBestMatch(input: FloatArray, model: List<TrainData>): FloatArray {
        val matches = model.map { data ->
            val distance = calculateDistance(input, data.input)
            Pair(distance, data)
        }.sortedBy { it.first }.take(3)

        if (matches.isEmpty()) return floatArrayOf(0f, 0f, 0.5f)

        val avgYaw = matches.map { it.second.output[0] }.average().toFloat()
        val avgPitch = matches.map { it.second.output[1] }.average().toFloat()
        val avgSpeed = matches.map {
            if (it.second.output.size >= 3) it.second.output[2] else 0.5f
        }.average().toFloat()

        return floatArrayOf(avgYaw, avgPitch, avgSpeed)
    }

    private fun createInput(dataSet: DataSet, modelType: ModelType, targetMoving: Boolean,
                            playerHealth: Float, combatTime: Long, isAttacking: Boolean): FloatArray {
        return when (modelType) {
            ModelType.BLD1 -> floatArrayOf(
                kotlin.math.min(1f, dataSet.distanceToTarget / 10f),
                dataSet.yaw / 180f,
                dataSet.pitch / 90f,
                if (targetMoving) 1f else 0f,
                playerHealth / 20f,
                if (isAttacking) 1f else 0f
            )
            ModelType.BLD2 -> floatArrayOf(
                kotlin.math.min(1f, dataSet.distanceToTarget / 50f),
                dataSet.yaw / 180f,
                dataSet.pitch / 90f,
                if (targetMoving) 1f else 0f,
                playerHealth / 20f,
                kotlin.math.min(1f, combatTime / 10000f)
            )
        }
    }

    private fun calculateDistance(input1: FloatArray, input2: FloatArray): Float {
        var sum = 0f
        val minSize = kotlin.math.min(input1.size, input2.size)
        for (i in 0 until minSize) {
            val diff = input1[i] - input2[i]
            sum += diff * diff
        }
        return kotlin.math.sqrt(sum)
    }

    fun isActive(): Boolean = isActive
}