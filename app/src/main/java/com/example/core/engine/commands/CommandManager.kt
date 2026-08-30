package com.example.core.engine.commands

import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.Layer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CanvasCommand {
    val description: String
    fun execute(current: CanvasProject): CanvasProject
    fun undo(current: CanvasProject): CanvasProject
}

class ProjectSnapshotCommand(
    override val description: String,
    private val oldLayers: List<Layer>,
    private val newLayers: List<Layer>,
    private val oldBgColor: Long? = null,
    private val newBgColor: Long? = null
) : CanvasCommand {
    override fun execute(current: CanvasProject): CanvasProject {
        return current.copy(
            layers = newLayers,
            backgroundColor = newBgColor ?: current.backgroundColor,
            updatedAt = System.currentTimeMillis()
        )
    }

    override fun undo(current: CanvasProject): CanvasProject {
        return current.copy(
            layers = oldLayers,
            backgroundColor = oldBgColor ?: current.backgroundColor,
            updatedAt = System.currentTimeMillis()
        )
    }
}

class CommandManager {
    private val undoStack = ArrayDeque<CanvasCommand>()
    private val redoStack = ArrayDeque<CanvasCommand>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun execute(command: CanvasCommand, current: CanvasProject): CanvasProject {
        val next = command.execute(current)
        undoStack.addLast(command)
        redoStack.clear()
        updateState()
        return next
    }

    fun undo(current: CanvasProject): CanvasProject {
        if (undoStack.isEmpty()) return current
        val command = undoStack.removeLast()
        val prev = command.undo(current)
        redoStack.addLast(command)
        updateState()
        return prev
    }

    fun redo(current: CanvasProject): CanvasProject {
        if (redoStack.isEmpty()) return current
        val command = redoStack.removeLast()
        val next = command.execute(current)
        undoStack.addLast(command)
        updateState()
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        updateState()
    }

    private fun updateState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
}
