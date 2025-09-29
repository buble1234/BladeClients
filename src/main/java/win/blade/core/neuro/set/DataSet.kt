package win.blade.core.neuro.set

import win.blade.core.neuro.model.ModelData
import win.blade.core.neuro.model.ModelType

public class DataSet {

    var yaw = 0.0f
    var pitch = 0.0f
    var distanceToTarget = 0.0f

    public fun updateData(yaw: Float, pitch: Float, distanceToTarget: Float) {
        this.yaw = yaw
        this.pitch = pitch
        this.distanceToTarget = distanceToTarget
    }
}