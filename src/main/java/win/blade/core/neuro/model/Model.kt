package win.blade.core.neuro.model

enum class ModelType {
    BLD1, BLD2
}

class Model {
    fun model(modelType: ModelType) {
        when (modelType) {
            ModelType.BLD1 -> model1()
            ModelType.BLD2 -> model2()
        }
    }

    private fun model1() {
    }

    private fun model2() {
    }
}