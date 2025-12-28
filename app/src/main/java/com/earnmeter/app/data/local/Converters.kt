package com.earnmeter.app.data.local

import androidx.room.TypeConverter
import com.earnmeter.app.domain.model.RideAction
import com.earnmeter.app.domain.model.RideClassification

class Converters {
    
    @TypeConverter
    fun fromRideAction(action: RideAction): String = action.name
    
    @TypeConverter
    fun toRideAction(value: String): RideAction = RideAction.valueOf(value)
    
    @TypeConverter
    fun fromRideClassification(classification: RideClassification): String = classification.name
    
    @TypeConverter
    fun toRideClassification(value: String): RideClassification = RideClassification.valueOf(value)
}

