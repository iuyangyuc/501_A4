1. Which tool you used   
   Github Copilot Gemini

2. What prompt or query you gave.  
   Copilot for in line completion;
   Gemini for generating the random function to generate a random temperature reading


3. What you kept or discarded from the output.
```
    private fun emitReading() {
        val newReading = TemperatureReading(
            timestamp = Instant.now(),
            value = Random.nextDouble(65.0, 85.0).toFloat()
        )
        _uiState.update { state ->
            val updated = (state.readings + newReading).takeLast(20)
            state.copy(readings = updated)
        }
    }
```