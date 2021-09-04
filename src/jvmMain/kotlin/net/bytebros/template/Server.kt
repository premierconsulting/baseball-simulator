package net.bytebros.template

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import net.bytebros.baseball.model.*
import java.util.*

fun randomScore() = Score(
    Random().nextInt(15),
    Random().nextInt(15),
)
fun randomBases() = Bases(
    Random().nextBoolean(),
    Random().nextBoolean(),
    Random().nextBoolean(),
)
fun randomCount() = Count(
    Random().nextInt(3),
    Random().nextInt(2),
    Random().nextInt(2),
)
fun randomHalfInning() = Random().nextInt(21)

fun Application.module() {

    val channel = produce { // this: ProducerScope<SseEvent> ->
        var n = 0
        while (true) {
            val baseballState = BaseballState(
                Team("CLE"),
                Team("HOU"),
                randomScore(),
                randomBases(),
                randomCount(),
                randomHalfInning(),
            )
//            send(Json.encodeToString(BaseballStateEvent.serializer(), BaseballStateEvent(baseballState)))
            send(BaseballStateEvent(baseballState))
            delay(1000)
            n++
        }
    }.broadcast()

    routing {
        get("/") {
            call.respondText(
                """
                        <html>
                            <head></head>
                            <body>
                                <ul id="events">
                                </ul>
                                <script type="text/javascript">
                                    var source = new EventSource('/sse');
                                    var eventsUl = document.getElementById('events');
                                    function logEvent(text) {
                                        var li = document.createElement('li')
                                        li.innerText = text;
                                        eventsUl.appendChild(li);
                                    }
                                    source.addEventListener('message', function(e) {
                                        logEvent('message:' + e);
                                    }, false);
                                    source.addEventListener('open', function(e) {
                                        logEvent('open');
                                    }, false);
                                    source.addEventListener('error', function(e) {
                                        if (e.readyState == EventSource.CLOSED) {
                                            logEvent('closed');
                                        } else {
                                            logEvent('error');
                                            console.log(e);
                                        }
                                    }, false);
                                </script>
                            </body>
                        </html>
                    """.trimIndent(),
                contentType = ContentType.Text.Html
            )
        }
        get("/sse") {
            val events = channel.openSubscription()
            try {
                call.respondSse(events)
            } finally {
                events.cancel()
            }
        }
        static {
            resources("app")
            defaultResource("app/index.html")
        }
    }
}
data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

suspend fun ApplicationCall.respondSse(events: ReceiveChannel<BaseballStateEvent>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondTextWriter(contentType = ContentType.Text.EventStream) {
        for (event in events) {
            val message = Json.encodeToString(BaseballStateEvent.serializer(), event)
            println(message)
            write("${message}\n")
            flush()
        }
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)