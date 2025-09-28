package win.blade.core.neuro

import win.blade.core.neuro.model.Model
import win.blade.core.neuro.model.ModelData
import win.blade.core.neuro.model.ModelType
import win.blade.core.neuro.set.DataSet
import win.blade.core.neuro.trainer.NeuralTrainer
import win.blade.core.neuro.predictor.NeuralPredictor
import win.blade.core.neuro.recorder.NeuralRecorder

class NeuralController {
    private val model = Model()
    private val dataSet = DataSet()
    private val recorder = NeuralRecorder()
    private val trainer = NeuralTrainer()
    private val predictor = NeuralPredictor()

    private var currentModelType = ModelType.BLD1

    fun startRecord(sessionName: String = "session_${System.currentTimeMillis()}") {
        recorder.startRecord(sessionName)
    }

    fun stopRecord() {
        recorder.stopRecord()
        val frames = recorder.getRecordedData()
        if (frames.isNotEmpty()) {
            trainer.processRecordedData(frames)
        }
    }

    fun record(yaw: Float, pitch: Float, distance: Float, isAttacking: Boolean,
               targetMoving: Boolean, playerHealth: Float, combatTime: Long,
               desiredYawDelta: Float, desiredPitchDelta: Float, desiredSpeed: Float) {
        dataSet.updateData(yaw, pitch, distance)
        recorder.recordFrame(dataSet, isAttacking, targetMoving, playerHealth, combatTime, desiredYawDelta, desiredPitchDelta, desiredSpeed)
    }

    fun predict(yaw: Float, pitch: Float, distance: Float, targetMoving: Boolean,
                playerHealth: Float, combatTime: Long, isAttacking: Boolean): ModelData {
        dataSet.updateData(yaw, pitch, distance)
        return predictor.predict(dataSet, currentModelType, targetMoving, playerHealth, combatTime, isAttacking)
    }

    fun start() {
        predictor.start()
    }

    fun stop() {
        predictor.stop()
    }

    fun switchModel(modelType: ModelType) {
        currentModelType = modelType
        model.model(modelType)
    }

    fun getCurrentModel(): ModelType = currentModelType

    fun isRecording(): Boolean = recorder.isRecording()
    fun isActive(): Boolean = predictor.isActive()
}