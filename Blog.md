# Creating a MLB Simulator with Fritz2

Create a class to represent the state of game at any given time.

`src/commonMain/kotlin/net/bytebros/baseball/model`
```kotlin
data class BaseballState(
    val awayScore: Int = 0,
    val homeScore: Int = 0,
)
```

Create an object to hold the current state
```kotlin
object BaseballStateStore: RootStore<BaseballState>(BaseballState()) {
    
}
```

Create a component to render both scores
```kotlin
fun RenderContext.scores() {
    div {
        score(BaseballStateStore.data.map { it.awayScore })
        h1 {
            +"-"
        }
        score(BaseballStateStore.data.map { it.homeScore })
    }
}

fun RenderContext.score(scoreFlow: Flow<Int>) {
    h1 {
        scoreFlow.asText()
    }
}
```

Update `BaseballState` to hold the current inning. We're storing the half inning, because we want to be able to tell if it's the top or the bottom of an inning.

0 - Top 1st, 1 - Bottom 1st, 2 - Top 2nd, 3 - Bottom 2nd, and so on. We can create properties for this logic.
Whether it's currently the top inning is determined by whether the halfInning is even.

|  Top     | Bottom |  Inning |
| ----------- | ----------- | ----------- |
| 0      |  1       |1 |
| 2      |  3       |2 |
| 4      |  5       |3 |

It takes 2 half innings to make 1 inning. So the first inning consists of two halfs.
To calculate the inning, we divide the halfInning in half. This gives us two divisions for every 1 inning, just like in a baseball game.

| Half Inning | Half Inning / 2.0 | Round down | +1 |
| ----------- | ----------- | ----------- | --- |
| 0 | 0.0 | 0 | 1 |
| 1 | 0.5 | 0 | 1 |
| 2 | 1.0 | 1 | 2 |
| 3 | 1.5 | 1 | 2 |


```kotlin
data class BaseballState(
    val awayScore: Int = 0,
    val homeScore: Int = 0,
    val halfInning: Int = 0,
) {
    val topInning: Boolean = halfInning % 2 == 0

    val inning: Int = ceil(it.halfInning / 2.0).toInt() + 1

    val inningSide: String = if (topInning) "TOP" else "BOT"

    val inningDisplay: String = "$inningSide $inning"
}
```

Create a component to show the current inning


```kotlin
fun RenderContext.inning() {
    div {
        h2 {
            BaseballState.inningDisplay.asText()
        }
    }
}
```

Update `BaseballState` to hold whether there are runners on the bases. 

```kotlin
data class BaseballState(
    val awayScore: Int = 0,
    val homeScore: Int = 0,
    val halfInning: Int = 0,
    val base1: Boolean = false,
    val base2: Boolean = false,
    val base3: Boolean = false,
) {
    /// ...
}
```

Create a component to contain the bases

```kotlin
fun RenderContext.bases() {
    div {
        base(BaseballState.base1)
        base(BaseballState.base2)
        base(BaseballState.base3)
    }
}
```

Create a base component that accepts a flow of boolean

```kotlin
fun RenderContext.base(onBase: Flow<Boolean>) {
    val classes = onBase.map { on ->
        mapOf("on" to on)
    }
    div({
        height {
            normal
        }
        width {
            normal
        }
        border {
            width { normal }
            style { solid }
            color { "#000" }
        }
        children("&.on") {
            background {
                color { "#000" }
            }
        }
    }) {
        classMap(classes)
    }
}
```

Update the BaseballState to hold the current count (the number of balls, strikes, and outs)

```kotlin
data class BaseballState(
    val awayScore: Int = 0,
    val homeScore: Int = 0,
    val halfInning: Int = 0,
    val base1: Boolean = false,
    val base2: Boolean = false,
    val base3: Boolean = false,
    val balls: Int = 0,
    val strikes: Int = 0,
    val outs: Int = 0,
) {
    // ...
}
```

Create a count component to hold the 3 count sections. We will make a generic count section to handle the similarities between the strikes, balls, and outs sections.
Each section has a label (B, S, O), and each of them have to render a number of circles. Let's create a class
to contain these parameters as a single type CountSectionProps. We will also create a countCircle component.


```kotlin
data class CountSectionProps(
    val label: String,
    val number: Int,
)

fun RenderContext.count() {
    val sectionPropsList = listOf(
        CountSectionProps("B", 3),
        CountSectionProps("S", 2),
        CountSectionProps("O", 2),
    )

    div({
        width { "50%" }
        margin { auto }
        direction { column }
    }) {
        sectionPropsList.forEach { sectionProps ->
            countSection(sectionProps)
        }
    }
}
```

```kotlin
fun RenderContext.countSection(props: CountSectionProps) {
    val (
        label,
        number,
    ) = props
    div {
        div {
            +label
        }
        for(index in 1..number) {
            countCircle(index)
        }
    }
}
```

```kotlin
fun RenderContext.countCircle(index: Int) {
    div({
        width { small }
        height { small }
        border {
            width { "1px" }
            style { solid }
            color { "#000" }
        }
        radius { "50%" }
    }) {
    }
}
```
At this point, we have all of the components that will read state from the BaseballStateStore.
We will now focus on handling the different types of events can happen in a baseball game.

Let's start with the strike event. The Store can receive events in order to determine how its state should update. 
This is what this looks like.

```kotlin
object BaseballStateStore: RootStore<BaseballState>(BaseballState()) {
    val handleStrike = handle<Unit> { state ->
        state
    }
}
```

`handleStrike` is an event handler, It's a Handler of type `Unit` because it expects no parameters with an event (besides the fact that it occurred.)
The handler also receives the current state. In this example we left the state alone, but we can update the state in the store by returning a new BaseballState in handle.
Before we do that, let's see how we trigger this handler. add a console.log statement in `handleStrike`

```kotlin
object BaseballStateStore: RootStore<BaseballState>(BaseballState()) {
    val handleStrike = handle<Unit> { state ->
        console.log("A strike occurred")
        state
    }
}
```

In the div of our `count` component we have access to a flow of dom events, like clicks. Let's update the count component temporarily.

```kotlin
fun RenderContext.count() {
    val sectionPropsList = listOf(
        CountSectionProps("B", 3),
        CountSectionProps("S", 2),
        CountSectionProps("O", 2),
    )

    div({
        width { "50%" }
        margin { auto }
        direction { column }
    }) {
        clicks handledBy BaseballStateStore.handleStrike
        
        sectionPropsList.forEach { sectionProps ->
            countSection(sectionProps)
        }
    }
}
```

Here we make use of the function `handledBy` to connect the BaseballStateStore handler, to the flow of dom events. This is an infix function.
Click anywhere in the count div, and see the console print that a strike has occurred. Yay! Our web app is reacting, but unfortunately our count components are connected to the store to see the state.
Let's fix that by updating our CountSectionProps to receive the flow of count data that a section needs, as well as an event handler to update its count.


```kotlin
data class CountSectionProps(
    val label: String,
    val number: Int,
    val countFlow: Flow<Int>,
    val onClick: Handler<Unit>,
)
```

Now in our count component, we'll update our props list

```kotlin
fun RenderContext.count() {
    val sectionPropsList = listOf(
        CountSectionProps("B", 3, BaseballStateStore.balls, BaseballStateStore.handleBall),
        CountSectionProps("S", 2, BaseballStateStore.strikes, BaseballStateStore.handleStrike),
        CountSectionProps("O", 2, BaseballStateStore.outs, BaseballStateStore.handleBatterOut),
    )

    div({
        width { "50%" }
        margin { auto }
        direction { column }
    }) {
        clicks handledBy BaseballStateStore.handleStrike
        
        sectionPropsList.forEach { sectionProps ->
            countSection(sectionProps)
        }
    }
}
```

Let's create some default handlers for the ball and out events.

```kotlin
object BaseballStateStore: RootStore<BaseballState>(BaseballState()) {
    val handleStrike = handle<Unit> { state ->
        state
    }

    val handleBall = handle<Unit> { state ->
        state
    }

    val handleBatterOut = handle<Unit> { state ->
        state
    }
}
```

Notice we created `handleBatterOut`, this is because not all outs have the exact same logic. For example, the strike count is not reset if a runner is thrown out.
More on this later.

With our handlers set up, let's update our `countSection` components to make use of the new store parameters we passed.
In the top-level div, we can attach the `clicks` events to the `onClick` handler passed from the parent component.

```kotlin
fun RenderContext.countSection(props: CountSectionProps) {
    val (
        label,
        number,
        countFlow,
        onClick
    ) = props
    div {
        clicks handledBy onClick
        
        div {
            +label
        }
        for(index in 1..number) {
            countCircle(index)
        }
    }
}
```

The last thing we need to do before moving on to the handler logic, is pass the `countFlow` to the `countCircle` component.


```kotlin
for(index in 1..number) {
    countCircle(index, countFlow)
}
```

```kotlin
fun RenderContext.countCircle(index: Int, countFlow: Flow<Int>) {
    val classesMap = countFlow.map { count ->
        val highlighted = count >= index
        mapOf(
            "highlighted" to highlighted
        )
    }
    
    div({
        width { small }
        height { small }
        border {
            width { "1px" }
            style { solid }
            color { color }
        }
        padding { small }
        children("&.highlighted") {
            background {
                color { "#fff" }
            }
        }
        radius { "50%" }
    }) {
        classMap(
            classesMap
        )
    }
}
```

Each `countCircle` knows what place it has in the count (the index), and that count flow it needs to keep track of.
Whenever the count on the flow, is greater than or equal to the index, we will add a `highlighted` class to the circle.
We also add styling so that the circles with `highlighted` class have a black background. With this we should finally be able to see our page update based on events.

# Handlers

Go to the `handleStrike` handler in `BaseballStateStore`.

```kotlin
    val handleStrike = handle<Unit> { state ->
        val strikes = state.strikes + 1
        state.copy(
            strikes = strikes
        )
    }
```

Here we calculate one more strike than the current count, and return a new copy of the state where everything is the same, except the strikes is set to our new calculated value.
We've officially made a reactive web app! Congrats but there's plenty more to go.

Let's update the `handleBall` handler to look like the `handleStrike`

```kotlin
    val handleBall = handle<Unit> { state ->
        val balls = state.balls + 1
        state.copy(
            balls = balls
        )
    }
```

And we'll also update the `handleBatterOut` to reset the strike count, and increase the out count.

```kotlin
    val handleBatterOut = handle<Unit> { state ->
        val outs = state.outs + 1
        state.copy(
            balls = 0,
            strikes = 0,
            outs = outs,
        )
    }
```

## Defensive handlers

### Connective handlers

Now we come to an interesting problem, where we want to handle events such as a strike out, a walk, or 3 outs happen. Otherwise, our simulator will never make any progress.
Notice that a strike out comes from a strike, a walk comes from a ball, and 3 outs come from an out. But the logic associated with those specific events
shouldn't always happen. (Every square is a rectangle, but not every rectangle is a square).
Luckily Fritz2 has the concept of Emitting Handlers, handlers which themselves emit values (or Unit) on a flow, so that other handlers can be informed.

### Handle Strike Out

We already have a handler that will reset the count `handleBatterOut`. We just need to trigger that when the strikes get to 3.
To connect `handleStrike` to `handleBatterOut`, we have to first make `handleStrike` an emitting handler.

```kotlin
    val handleStrike = handleAndEmit<Unit> { state ->
        val strikes = state.strikes + 1
        if (strikes > 2) emit(Unit)
        state.copy(
            strikes = strikes
        )
    }
```

Inside `handleAndEmit` we have access to the `emit` function, which broadcasts whatever we want on to a flow for someone to observe.

The someone in this case is the `handleBatterOut`. Let's create an init block inside `BaseballStateStore`

```kotlin
object BaseballStateStore: RootStore<BaseballState>(BaseballState()) {
    // ...
    
    init {
        handleStrike handledBy handleBatterOut
    }
}
```

Just like when we connected the flow of click events to handlers, here we use `handledBy` to connect the flow of strikeouts, to the batter out handler.
At this point, we only need to handle a runner being out, and 3 outs to handle all of the defensive aspects of the game. 

### Runner Out
First notice that a batter out and a runner out both increase the number of outs by 1, and should both potentially trigger an inning change.
To avoid duplicate logic, we'll create a generic `handleOut`, and trigger that from the more specific `handleBatterOut` and `handleRunnerOut`

In `handleBatterOut` remove the increasing of the outs, and let's create `handleRunnerOut` and `handleOut` handlers.

```kotlin
    val handleBatterOut = handleAndEmit<Unit> { state ->
        emit(Unit)
        state.copy(
            balls = 0,
            strikes = 0,
        )
    }

    val handleRunnerOut = handleAndEmit<Unit> { state ->
        emit(Unit)
        state
    }

    val handleOut = handle<Unit> { state ->
        val outs = state.outs + 1
        state.copy(
            outs = outs,
        )
    }

    // ...

    init {
        handleStrike handledBy handleBatterOut
        handleBatterOut handledBy handleOut
        handleRunnerOut handledBy handleOut
    }
```

Great, we've simplified our logic a bit, but our runner out logic is incomplete. Whichever base the runner was on needs to be unloaded.
This means our `handleRunnerOut` handler needs to know which base the runner was out at. Up until this point, we've accepted Unit
for all of our handlers, but now we will create a new class to represent the base runners, and pass it to a handler. We will create
an enum class, because we know all possibles values, and can easily enumerate them.

```kotlin
enum class BaseRunner {
    FIRST,
    SECOND,
    THIRD
}
```

Now let's update `handleRunnerOut` to accept not Unit, but BaseRunner. Because this is an emitting handler, we have to specify both the type to handle and emit, which are `BaseRunner` and `Unit` respectively.

```kotlin
    val handleRunnerOut = handleAndEmit<BaseRunner, Unit> { state ->
        val base1 = if (runner == Runner.FIRST) false else state.base1
        val base2 = if (runner == Runner.SECOND) false else state.base2
        val base3 = if (runner == Runner.THIRD) false else state.base3
        emit(Unit)
        state.copy(
            base1 = base1,
            base2 = base2,
            base3 = base3,
        )
    }
```

In the handler, we reset which ever base is associated with the now out base runner.

### Inning Change
And now on to our last defensive handler. Forcing an inning change.
Hopefully you see a pattern developing. We want to emit an event from `handleOut` to a `handleInningChange` when there are 3 outs.

```kotlin
    val handleOut = handleAndEmit<Unit> { state ->
        val outs = state.outs + 1
        if (outs > 2) emit(Unit)
        state.copy(
            outs = outs,
        )
    }
    
    val handleInningChange = handle<Unit> { state ->
        state.copy(
            halfInning = state.halfInning + 1,
            balls = 0,
            strikes = 0,
            outs = 0,
            base1 = false,
            base2 = false,
            base3 = false
        )
    }

    // ...

    init {
        handleStrike handledBy handleBatterOut
        handleBatterOut handledBy handleOut
        handleRunnerOut handledBy handleOut
        handleOut handledBy handleInningChange
    }
```

## Offensive handlers

Now we can move on to getting on base, and scoring some runs! In order to get on base you have to Hit the ball.
We will make a handler `handleHit`, but like `handleRunnerOut`, we need to know which type of hit happened. Let's create
another enum class for the hit type.

```kotlin
enum class Hit {
    SINGLE,
    DOUBLE,
    TRIPLE,
    HOME_RUN
}
```

Let's also create some buttons to trigger hit events.

```kotlin
fun RenderContext.hits() {
    val handleHit = BaseballStateStore.handleHit
    div {
        button {
            clicks.map { Hit.SINGLE } handledBy handleHit
            +"SINGLE"
        }
        button {
            clicks.map { Hit.DOUBLE } handledBy handleHit
            +"DOUBLE"
        }
        button {
            clicks.map { Hit.TRIPLE } handledBy handleHit
            +"TRIPLE"
        }
        button {
            clicks.map { Hit.HOME_RUN } handledBy handleHit
            +"HOME_RUN"
        }
    }
}
```

And let's create the `handleHit` handler in the `BaseballStateStore`.

```kotlin
    val handleScore = handle<Int> { state, runsScored ->
        val awayScore = if (state.awayIsBatting) state.awayScore + runsScored else state.awayScore
        val homeScore = if (!state.awayIsBatting) state.homeScore + runsScored else state.homeScore
    
        // Home team can win on a hit, if they take the lead, and it's the 9th inning or higher
        if (state.homeIsBatting && homeScore > awayScore && state.halfInning > 16) emit(Unit)
    
        state.copy(
            awayScore = awayScore,
            homeScore = homeScore,
        )
    }
```

This is our most complex handler yet, so let's look at it step by step.

First we look create a hitIndex, from the enum ordinal. This will simply return 1 for SINGLE, 2 for DOUBLE, etc.

```kotlin
val hitIndex = hit.ordinal
```

Next we create a list of all the possible scorers, starting from 3 base (the closest to scoring). The fourth is always true because the batter can score on a home run.
```kotlin
val possibleScorers = listOf(state.base3, state.base2, state.base1, true)
    .slice(0..hitIndex)
```
The runner on third base will automatically score on a hit. We will include more runners with stronger hits. (2 runners for doubles, 3 runners for triple, etc)

```kotlin
val runsScored = possibleScorers
    .map { baseLoaded -> if (baseLoaded) 1 else 0 }
    .sum()
```

For every possible scorer included based on the hit strength, if there is a runner on base, we will add the number 1, else we will add 0.

Now that we have the number of runsScored, let's award them to the appropriate team. If it's the top half of the inning, the away team is batting and they receive the points,
otherwise it's the bottom half, the home team is batting, and they receive the points.

```kotlin
val awayScore = if (state.topInning) state.awayScore + runsScored else state.awayScore
val homeScore = if (!state.topInning) state.homeScore + runsScored else state.homeScore
```

And now for the trickiest part, how to we get a new set of bases?

| Hit | Runner on 3rd | Runner on 2nd | Runner on 1st |
| ---- | ---- | ---- | ---- |
| SINGLE | Runner was on 2nd | Runner was on 1st | true |
| DOUBLE | Runner was on 1st | true | false |
| TRIPLE | true | false | false |
| HOME_RUN | false | false | false |

We can see the bases are determined by the runners on the bases behind it. And the stronger the hit, the more false values we shift into the bases.

We create a list that represents all possible bases changes.

```listOf(state.base2, state.base1, true, false, false, false)```

From this we want to take 3 consecutive values, starting based on the hitIndex.
- If hitIndex is 1, I take the first 3 for state.base2, state.base1, true.
- If hitIndex is 2, I offset 1 to the right for state.base1, true, false.
- If hitIndex is 3 I offset 2 to the right for true, false, false
- If hitIndex is 4, I offset 3 to the right for false, false, false

```kotlin
val (base3, base2, base1) = listOf(state.base2, state.base1, true, false, false, false)
    .slice(hitIndex..hitIndex + 2)
```

## Winning the Game

### handleWin

We've now arrived at the ways a team can win a baseball game. The offense can score, or the defense can force an inning change.
In our `handleScore` and `handleInningChange` let's check for these conditions, and emit for another listener.

#### Hit
The home team wins on a hit when
- The home team is batting
- The home team is winning
- The current inning is at least bottom of the 9th inning

#### Inning Change
The away team wins on an inning change when
- The home team is batting (they just got 3 outs)
- The away team is winning
- The current inning is at least the bottom of the 9th. (The inning change would trigger top of the 10th)

The home team can also win on an inning change when
- The away team is batting (they just got 3 outs)
- The home team is winning
- The current inning is at least the top of the 9th. (The inning change would trigger bottom of the 9th)

Let's store figure out how many halfInnings is the top of the 9th, and store it in our BaseballState class.

```kotlin
    companion object {
        const val TOP_OF_THE_9TH = 16
    }
```

Now we can use this in our handlers.

```kotlin
    val handleScore = handleAndEmit<Int, Unit> { state, runsScored ->
        val awayScore = if (state.awayIsBatting) state.awayScore + runsScored else state.awayScore
        val homeScore = if (!state.awayIsBatting) state.homeScore + runsScored else state.homeScore
    
        // Home team can win on a hit, if they take the lead, and it's the 9th inning or higher
        if (state.homeIsBatting 
            && homeScore > awayScore
            && state.halfInning > BaseballState.TOP_OF_THE_9TH) {
            emit(Unit)
        }
    
        state.copy(
            awayScore = awayScore,
            homeScore = homeScore,
        )
    }
```

Both the home team and the away team can win on an inning change

```kotlin
    val handleInningChange = handleAndEmit<Unit> { state ->
        val gameOver = state.gameOver()
        if (gameOver) emit(Unit)
        if (state.halfInning > BaseballState.TOP_OF_THE_9TH && state.awayScore > state.homeScore && !state.awayIsBatting) {
            gameIsWon = true
        }
        if (state.halfInning >= BaseballState.TOP_OF_THE_9TH && state.homeScore > state.awayScore) {
            gameIsWon = true
        }
        if (gameIsWon) {
            emit(Unit)
        }
        state.copy(
            halfInning = if (gameIsWon) state.halfInning else state.halfInning + 1,
            balls = 0,
            strikes = 0,
            outs = 0,
            base1 = false,
            base2 = false,
            base3 = false
        )
    }
```

```kotlin
    val handleWin = handleAndEmit<Unit> { state ->
        state.copy(
            final = true,
        )
    }
```

Let's update the components to observe whether the game is ended. The inning will display F,
The buttons will 




