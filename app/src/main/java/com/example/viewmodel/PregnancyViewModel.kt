package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameProfile
import com.example.data.HighScoreEntry
import com.example.data.PregnancyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameState {
    ONBOARDING, MENU, PLAYING, GAME_OVER, PAUSED
}

enum class EnemyType {
    STALKER,   // Red: Smart BFS tracking
    AMBUSH,    // Purple: Tries to intercept
    WANDERER   // Cyan: Moves randomly on intersections, otherwise targets general direction
}

data class Enemy(
    val id: Int,
    val position: Pair<Int, Int>,
    val type: EnemyType,
    val colorHex: String,
    val speedDelayModifier: Int = 0 // adjustment on Base game tick delay
)

class PregnancyViewModel(private val repository: PregnancyRepository) : ViewModel() {

    // Onboarding / Profile Integration
    val profileState: StateFlow<GameProfile?> = repository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isOnboardingCompleted: StateFlow<Boolean> = repository.profile
        .map { it?.onboardingCompleted ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Evasion Game State Flow
    private val _gameState = MutableStateFlow(GameState.ONBOARDING)
    val gameState = _gameState.asStateFlow()

    private val _gridSize = 12
    val gridSize = _gridSize

    private val _grid = MutableStateFlow<List<List<Int>>>(emptyList())
    val grid = _grid.asStateFlow() // 0 = Path, 1 = Wall, 4 = Gem

    private val _playerPosition = MutableStateFlow(Pair(1, 1))
    val playerPosition = _playerPosition.asStateFlow()

    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies = _enemies.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level = _level.asStateFlow()

    private val _survivalSeconds = MutableStateFlow(0)
    val survivalSeconds = _survivalSeconds.asStateFlow()

    val highScores: StateFlow<List<HighScoreEntry>> = repository.highScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeNickname = MutableStateFlow("")
    val activeNickname = _activeNickname.asStateFlow()

    // Game Running Engine Coroutines
    private var gameLoopJob: Job? = null
    private var timerJob: Job? = null

    // Base difficulty speed of enemies (lower = faster)
    private fun getBaseTickDelay(): Long {
        return (380 - (level.value * 20)).coerceAtLeast(180).toLong()
    }

    init {
        // Collect profile to check onboarding completion
        viewModelScope.launch {
            repository.profile.collect { profile ->
                if (profile != null) {
                    _activeNickname.value = profile.username
                    if (profile.onboardingCompleted && _gameState.value == GameState.ONBOARDING) {
                        _gameState.value = GameState.MENU
                    }
                } else {
                    _gameState.value = GameState.ONBOARDING
                }
            }
        }
    }

    // Profile action overrides to bridge template expectations
    fun completeOnboarding(name: String, lmpTimestamp: Long? = null, dueTimestamp: Long? = null) {
        val cleanName = name.trim().ifEmpty { "EvasionPlayer" }
        _activeNickname.value = cleanName
        viewModelScope.launch {
            repository.saveProfile(
                GameProfile(
                    id = 1,
                    username = cleanName,
                    onboardingCompleted = true,
                    selectedTheme = "NEON_ARCADE"
                )
            )
            _gameState.value = GameState.MENU
        }
    }

    fun updateProfileName(name: String) {
        val cleanName = name.trim().ifEmpty { "EvasionPlayer" }
        _activeNickname.value = cleanName
        viewModelScope.launch {
            val current = repository.getProfileDirect() ?: GameProfile()
            repository.saveProfile(current.copy(username = cleanName))
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            repository.saveProfile(GameProfile(id = 1, onboardingCompleted = false))
            repository.clearAllHighScores()
            _gameState.value = GameState.ONBOARDING
        }
    }

    fun clearAllHighScores() {
        viewModelScope.launch {
            repository.clearAllHighScores()
        }
    }

    // Start New Game Session
    fun startNewGame() {
        stopGameJobs()
        _score.value = 0
        _level.value = 1
        _survivalSeconds.value = 0
        setupLevel()
        _gameState.value = GameState.PLAYING
        startGameJobs()
    }

    // Advance Level
    private fun nextLevel() {
        _level.value += 1
        setupLevel()
    }

    private fun setupLevel() {
        // 1. Generate fully valid, connected grid maze
        val (newGrid, startPos) = generateValidGridAndPlayer()
        _grid.value = newGrid
        _playerPosition.value = startPos

        // 2. Spawn collectibles (Gems) in beautiful path junctions (usually 6 gems)
        spawnGems(6)

        // 3. Setup progressive enemies based on level
        val initialEnemies = mutableListOf<Enemy>()
        // Enemy 1: Always a Smart Chaser (Red) spawning opposite of player coordinates
        val enemy1Pos = findSpawningPointOpposite(startPos, newGrid)
        initialEnemies.add(Enemy(1, enemy1Pos, EnemyType.STALKER, "#FF3E5F"))

        if (level.value >= 2) {
            // Enemy 2: An intercepting ambusher (Purple)
            val enemy2Pos = findRandomPathPosition(newGrid, exclude = listOf(startPos, enemy1Pos))
            initialEnemies.add(Enemy(2, enemy2Pos, EnemyType.AMBUSH, "#D03EFF"))
        }

        if (level.value >= 4) {
            // Enemy 3: A swift rogue wanderer (Cyan)
            val enemy3Pos = findRandomPathPosition(newGrid, exclude = listOf(startPos, enemy1Pos))
            initialEnemies.add(Enemy(3, enemy3Pos, EnemyType.WANDERER, "#3EFFFF", speedDelayModifier = -40))
        }

        _enemies.value = initialEnemies
    }

    private fun startGameJobs() {
        // Game Actions Loop (Enemy AI updates)
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value == GameState.PLAYING) {
                delay(getBaseTickDelay())
                updateEnemyMovement()
                checkEnemyCollision()
            }
        }

        // Clock Survival Timer
        timerJob = viewModelScope.launch {
            while (_gameState.value == GameState.PLAYING) {
                delay(1000)
                _survivalSeconds.value += 1
                // Add 1 bonus score element per second survived
                _score.value += 1
            }
        }
    }

    private fun stopGameJobs() {
        gameLoopJob?.cancel()
        gameLoopJob = null
        timerJob?.cancel()
        timerJob = null
    }

    fun pauseGame() {
        if (_gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.PAUSED
            stopGameJobs()
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED) {
            _gameState.value = GameState.PLAYING
            startGameJobs()
        }
    }

    // Grid System Generation Engine
    // Generates a 12x12 maze where player position can reach any point of the paths.
    private fun generateValidGridAndPlayer(): Pair<List<List<Int>>, Pair<Int, Int>> {
        var attempts = 0
        while (attempts < 100) {
            attempts++
            // Create clear grid initialized with paths (0) inside solid outer walls (1)
            val tempGrid = MutableList(_gridSize) { row ->
                MutableList(_gridSize) { col ->
                    if (row == 0 || row == _gridSize - 1 || col == 0 || col == _gridSize - 1) 1 else 0
                }
            }

            // Scatter standard random blocker walls (approx 20% coverage)
            val rnd = Random(System.nanoTime())
            for (r in 2 until _gridSize - 2) {
                for (c in 2 until _gridSize - 2) {
                    if (rnd.nextFloat() < 0.22f) {
                        tempGrid[r][c] = 1
                    }
                }
            }

            val playerStartPos = Pair(1, 1) // Always clean start
            tempGrid[playerStartPos.first][playerStartPos.second] = 0 // Clear player start

            // Run BFS checking connectivity of paths from player position
            val visitedPaths = runConnectivityBFS(playerStartPos, tempGrid)
            val totalWalkable = countWalkables(tempGrid)

            // If we connected at least 70% of the interior grid map, it's a stellar valid playground!
            if (visitedPaths.size >= totalWalkable && visitedPaths.size >= 40) {
                // Ensure all disconnected items (if an island did not connect) are turned into walls
                for (r in 0 until _gridSize) {
                    for (c in 0 until _gridSize) {
                        val pos = Pair(r, c)
                        if (tempGrid[r][c] == 0 && pos !in visitedPaths) {
                            tempGrid[r][c] = 1 // turn isolated cells into walls
                        }
                    }
                }
                return Pair(tempGrid, playerStartPos)
            }
        }

        // Absolute Fallback: simple ring layout with zero blocking walls so it always runs
        val solidFallbackGrid = List(_gridSize) { row ->
            List(_gridSize) { col ->
                if (row == 0 || row == _gridSize - 1 || col == 0 || col == _gridSize - 1) 1 else 0
            }
        }
        return Pair(solidFallbackGrid, Pair(1, 1))
    }

    private fun runConnectivityBFS(start: Pair<Int, Int>, grid: List<List<Int>>): Set<Pair<Int, Int>> {
        val visited = mutableSetOf(start)
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(start)

        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            for (dir in dirs) {
                val nr = curr.first + dir.first
                val nc = curr.second + dir.second
                val nPos = Pair(nr, nc)
                if (nr in 0 until _gridSize && nc in 0 until _gridSize &&
                    grid[nr][nc] == 0 && nPos !in visited) {
                    visited.add(nPos)
                    queue.add(nPos)
                }
            }
        }
        return visited
    }

    private fun countWalkables(grid: List<List<Int>>): Int {
        var count = 0
        for (r in 0 until _gridSize) {
            for (c in 0 until _gridSize) {
                if (grid[r][c] == 0) count++
            }
        }
        return count
    }

    private fun spawnGems(amount: Int) {
        val curGrid = _grid.value.map { it.toMutableList() }
        val player = _playerPosition.value
        val rnd = Random(System.nanoTime())

        var gemsSpawns = 0
        var attempts = 0
        while (gemsSpawns < amount && attempts < 200) {
            attempts++
            val r = rnd.nextInt(1, _gridSize - 1)
            val c = rnd.nextInt(1, _gridSize - 1)
            // Can only spawn on an empty Path (0) and not on top of the player
            if (curGrid[r][c] == 0 && Pair(r, c) != player) {
                curGrid[r][c] = 4 // Code 4 is Gem
                gemsSpawns++
            }
        }
        _grid.value = curGrid
    }

    private fun countRemainingGems(grid: List<List<Int>>): Int {
        var count = 0
        for (r in 0 until _gridSize) {
            for (c in 0 until _gridSize) {
                if (grid[r][c] == 4) count++
            }
        }
        return count
    }

    // Spawning coordinates helpers
    private fun findSpawningPointOpposite(player: Pair<Int, Int>, grid: List<List<Int>>): Pair<Int, Int> {
        val targetRow = if (player.first < _gridSize / 2) _gridSize - 2 else 1
        val targetCol = if (player.second < _gridSize / 2) _gridSize - 2 else 1

        // Check if walkable, otherwise find nearest walkable
        if (grid[targetRow][targetCol] == 0) return Pair(targetRow, targetCol)

        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = mutableSetOf(Pair(targetRow, targetCol))
        queue.add(Pair(targetRow, targetCol))
        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            if (grid[curr.first][curr.second] == 0 && curr != player) {
                return curr
            }
            for (dir in dirs) {
                val nr = curr.first + dir.first
                val nc = curr.second + dir.second
                val nPos = Pair(nr, nc)
                if (nr in 1 until _gridSize - 1 && nc in 1 until _gridSize - 1 && nPos !in visited) {
                    visited.add(nPos)
                    queue.add(nPos)
                }
            }
        }
        return Pair(_gridSize - 2, _gridSize - 2) // absolute safe boundaries
    }

    private fun findRandomPathPosition(grid: List<List<Int>>, exclude: List<Pair<Int, Int>>): Pair<Int, Int> {
        val rnd = Random(System.nanoTime())
        var attempts = 0
        while (attempts < 150) {
            attempts++
            val r = rnd.nextInt(1, _gridSize - 1)
            val c = rnd.nextInt(1, _gridSize - 1)
            val pos = Pair(r, c)
            if (grid[r][c] == 0 && pos !in exclude) {
                return pos
            }
        }
        return Pair(_gridSize - 2, 1) // default fallback corner
    }

    // Grid Movement Controls
    fun movePlayer(rowDelta: Int, colDelta: Int) {
        if (_gameState.value != GameState.PLAYING) return
        val current = _playerPosition.value
        val nr = current.first + rowDelta
        val nc = current.second + colDelta

        if (nr in 0 until _gridSize && nc in 0 until _gridSize) {
            // Cannot pass through obstacles / walls (Value 1)
            if (_grid.value[nr][nc] != 1) {
                _playerPosition.value = Pair(nr, nc)
                checkGemCollection(nr, nc)
                checkEnemyCollision()
            }
        }
    }

    private fun checkGemCollection(r: Int, c: Int) {
        val currentGridState = _grid.value
        if (currentGridState[r][c] == 4) { // Gem
            // Edit grid state to clear gem
            val updatedGrid = currentGridState.mapIndexed { ri, row ->
                row.mapIndexed { ci, cell ->
                    if (ri == r && ci == c) 0 else cell
                }
            }
            _grid.value = updatedGrid
            _score.value += 15 // +15 points per Gem collected

            // Check if all gems are cleared!
            if (countRemainingGems(_grid.value) == 0) {
                // Reward +100 for level clearance and transition!
                _score.value += 100
                nextLevel()
            }
        }
    }

    // Enemy AI Movement Update Cycle
    private fun updateEnemyMovement() {
        val currentEnemies = _enemies.value
        val playerPos = _playerPosition.value
        val currentGrid = _grid.value

        val updatedEnemies = currentEnemies.map { enemy ->
            val nextStep = when (enemy.type) {
                EnemyType.STALKER -> {
                    // Smart navigation towards player via BFS pathfinder
                    findNextStepBFS(enemy.position, playerPos, currentGrid)
                }
                EnemyType.AMBUSH -> {
                    // Interception: try to aim slightly ahead or greedy pursuit with dynamic offsets
                    if (Random.nextFloat() < 0.70f) {
                        findNextStepGreedy(enemy.position, playerPos, currentGrid)
                    } else {
                        // semi-random choice
                        findRandomNeighbors(enemy.position, currentGrid)
                    }
                }
                EnemyType.WANDERER -> {
                    // Quick random motion at intersections
                    val neighbors = getValidMoves(enemy.position, currentGrid)
                    if (neighbors.size > 2 && Random.nextFloat() < 0.75f) {
                        neighbors.random()
                    } else {
                        findNextStepGreedy(enemy.position, playerPos, currentGrid)
                    }
                }
            }
            enemy.copy(position = nextStep)
        }
        _enemies.value = updatedEnemies
    }

    private fun getValidMoves(pos: Pair<Int, Int>, grid: List<List<Int>>): List<Pair<Int, Int>> {
        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        val moves = mutableListOf<Pair<Int, Int>>()
        for (dir in dirs) {
            val nr = pos.first + dir.first
            val nc = pos.second + dir.second
            if (nr in 0 until _gridSize && nc in 0 until _gridSize && grid[nr][nc] != 1) {
                moves.add(Pair(nr, nc))
            }
        }
        return moves.ifEmpty { listOf(pos) }
    }

    private fun findRandomNeighbors(pos: Pair<Int, Int>, grid: List<List<Int>>): Pair<Int, Int> {
        return getValidMoves(pos, grid).random()
    }

    // Path finding BFS Algorithms
    private fun findNextStepBFS(start: Pair<Int, Int>, target: Pair<Int, Int>, grid: List<List<Int>>): Pair<Int, Int> {
        if (start == target) return start

        val queue = ArrayDeque<List<Pair<Int, Int>>>()
        queue.add(listOf(start))
        val visited = mutableSetOf(start)

        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val current = path.last()

            if (current == target) {
                return if (path.size > 1) path[1] else start
            }

            for (dir in dirs) {
                val nr = current.first + dir.first
                val nc = current.second + dir.second
                val nPos = Pair(nr, nc)

                if (nr in 0 until _gridSize && nc in 0 until _gridSize &&
                    grid[nr][nc] != 1 && nPos !in visited) {
                    visited.add(nPos)
                    val newPath = path.toMutableList().apply { add(nPos) }
                    queue.add(newPath)
                }
            }
        }

        // BFS block fallback: greedy step
        return findNextStepGreedy(start, target, grid)
    }

    private fun findNextStepGreedy(start: Pair<Int, Int>, target: Pair<Int, Int>, grid: List<List<Int>>): Pair<Int, Int> {
        val validMoves = getValidMoves(start, grid)
        if (validMoves.isEmpty()) return start

        var bestMove = validMoves[0]
        var minDist = Double.MAX_VALUE

        for (move in validMoves) {
            val dist = Math.hypot((move.first - target.first).toDouble(), (move.second - target.second).toDouble())
            if (dist < minDist) {
                minDist = dist
                bestMove = move
            }
        }
        return bestMove
    }

    // Check collision between player and enemies
    private fun checkEnemyCollision() {
        val player = _playerPosition.value
        val collided = _enemies.value.any { it.position == player }

        if (collided && _gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.GAME_OVER
            stopGameJobs()
            saveScoreEntry()
        }
    }

    // SQLite persist save entry
    private fun saveScoreEntry() {
        viewModelScope.launch {
            val name = _activeNickname.value.ifEmpty { "SurvivalPlayer" }
            val newEntry = HighScoreEntry(
                username = name,
                score = _score.value,
                survivalTimeSeconds = _survivalSeconds.value,
                difficulty = when {
                    level.value >= 5 -> "HARD"
                    level.value >= 3 -> "NORMAL"
                    else -> "EASY"
                }
            )
            repository.addHighScore(newEntry)
        }
    }
}

class PregnancyViewModelFactory(private val repository: PregnancyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PregnancyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PregnancyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
