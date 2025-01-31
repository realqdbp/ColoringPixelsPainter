package codes.qdbp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.types.TelegramBotResult
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.math.min


object Clicker {
    @Volatile
    var paused = false

    val lock = Object()

    fun start(
        imageX: Int,
        imageY: Int,
        posColors: List<Point>,
        visibleColors: Int,
        nonSquare: Boolean,
        background: Point,
        nonSquarePositions: Array<Point>,
    ) {
        this.paused = false

        thread {
            val tgBot = bot { token = "7661113005:AAFhou-W_LF213uEB42U0_YMzqQ-jMWXItA" }
            tgBot.sendMessage("CPP Started!")

            val mouse = Robot()

            val fakeBgImg = mouse.createScreenCapture(Rectangle(background.x, background.y, 1, 1))
            val (realTopX, realTopY, realBottomX, realBottomY) = nonSquarePositions.findExactPositions(mouse, fakeBgImg)


            val pixelSize = (realBottomX-realTopX) / imageX.toDouble()
            val halfPixel = pixelSize / 2
            val backgroundImage = background.capturePixel(mouse, halfPixel.toInt())

            val pixelList = mutableListOf<Point>()
            var startY = realTopY + halfPixel
            repeat(imageY) {
                var startX = realTopX + halfPixel
                repeat(imageX) {
                    pixelList.add(Point(startX.toInt(), startY.toInt()))
                    startX += pixelSize
                }
                startY += pixelSize
            }

            if (nonSquare) {
                val iterator = pixelList.iterator()
                var times = 0
                val size = pixelList.size
                while (iterator.hasNext()) {
                    ++times
                    val pixel = iterator.next()
                    if (pixel.isBackground(mouse, halfPixel.toInt(), backgroundImage)) iterator.remove()
                    updateMessage("Masking Image: $times / $size")
                }
            }

            val pixelAmount = pixelList.size
            var pixelCount = 0
            var ya = 10

            val toNextColor = (posColors[1].x - posColors[0].x)/(visibleColors-1)
            val colorPositions = (posColors[0].x .. posColors[1].x step toNextColor).map { Point(it, posColors[0].y) }.toMutableList()

            tailrec fun Point.colorNeighbors(mouse: Robot, pixelList: MutableList<Point>, stack: MutableList<Point> = mutableListOf(this)) {

                mouse.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                mouse.delay(SharedState.longDelay)

                synchronized(lock) {
                    while (paused) {
                        lock.wait()
                    }
                }

                if (stack.isEmpty()) {
                    mouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    mouse.delay(SharedState.longDelay)
                    return
                }


                var current = stack.removeLast()
                while (!pixelList.contains(current)) {
                    if (stack.isEmpty()) {
                        mouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                        mouse.delay(SharedState.longDelay)
                        return
                    }
                    current = stack.removeLast()
                }

                mouse.mouseMove(current.x, current.y)
                mouse.delay(SharedState.longDelay)

                val newPixelColor = current.capturePixel(mouse, halfPixel.toInt())
                if (newPixelColor.isColored()) {
                    pixelList.remove(current)
                    ++pixelCount
                    val percentage = (pixelCount * 100/ pixelAmount.toDouble())
                    val progressText = "Progress: $pixelCount / $pixelAmount | ${percentage.toString().take(4)}%"
                    updateMessage(progressText)
                    if (percentage >= ya) {
                        val currentPercent = ya
                        thread {
                            val photo = mouse.createScreenCapture(Rectangle(realTopX, realTopY, realBottomX - realTopX, realBottomY - realTopY))
                            val os = ByteArrayOutputStream()
                            ImageIO.write(photo, "png", os)
                            tgBot.sendPhoto(ChatId.fromId(SharedState.tgChatId), TelegramFile.ByByteArray(os.toByteArray(), "$ya%-done.png"), "$ya% done!")
                        }
                        ya += 10
                    }

                    pixelList.find { it.x in pixelRange(current.x+pixelSize) && it.y in pixelRange(current.y+pixelSize) }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                    pixelList.find { it.x in pixelRange(current.x-pixelSize) && it.y in pixelRange(current.y-pixelSize) }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                    pixelList.find { it.x in pixelRange(current.x-pixelSize) && it.y in pixelRange(current.y+pixelSize) }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                    pixelList.find { it.x in pixelRange(current.x+pixelSize) && it.y in pixelRange(current.y-pixelSize) }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }

                    pixelList.find { it.x in pixelRange(current.x+pixelSize) && it.y == current.y }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                    pixelList.find { it.x in pixelRange(current.x-pixelSize) && it.y == current.y }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                    pixelList.find { it.x == current.x && it.y in pixelRange(current.y+pixelSize) }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                    pixelList.find { it.x == current.x && it.y in pixelRange(current.y-pixelSize) }?.let { if (!stack.contains(it) && pixelList.contains(it)) stack.add(it) }
                }

                colorNeighbors(mouse, pixelList, stack)
            }

            while (pixelList.size > 0) {

                synchronized(lock) {
                    while (paused) {
                        lock.wait()
                    }
                }


                val pixel = pixelList.random()

                var it = 0
                while (it <= min(20, colorPositions.size - 1)) {

                    val pixelC = pixel.capturePixel(mouse, halfPixel.toInt())
                    if (pixelC.isColored()) {
                        pixelList.remove(pixel)
                        ++pixelCount
                        updateMessage("Progress: $pixelCount / $pixelAmount | ${(pixelCount * 100/ pixelAmount.toDouble()).toString().take(4)}%")
                        break
                    }

                    val current = colorPositions[it]
                    mouse.mouseMove(current.x, current.y)
                    mouse.delay(SharedState.longDelay)
                    mouse.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                    mouse.delay(SharedState.shortDelay)
                    mouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    mouse.delay(SharedState.longDelay)
                    if (!current.exists(mouse, toNextColor)) {
                        colorPositions.removeAt(it)
                        continue
                    }

                    val oldPixelColor = pixel.capturePixel(mouse, halfPixel.toInt())
                    mouse.mouseMove(pixel.x, pixel.y)
                    mouse.delay(SharedState.longDelay)
                    mouse.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                    mouse.delay(SharedState.shortDelay)
                    mouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    mouse.delay(SharedState.longDelay)

                    val newPixelColor = pixel.capturePixel(mouse, halfPixel.toInt())
                    if (!(oldPixelColor.compare(newPixelColor))) {
                        pixelList.remove(pixel)
                        ++pixelCount
                        updateMessage("Progress: $pixelCount / $pixelAmount | ${(pixelCount * 100/ pixelAmount.toDouble()).toString().take(4)}%")
                        pixelList.find { it.x in pixelRange(pixel.x+pixelSize) && it.y in pixelRange(pixel.y+pixelSize) }?.run { colorNeighbors(mouse, pixelList) }
                        pixelList.find { it.x in pixelRange(pixel.x-pixelSize) && it.y in pixelRange(pixel.y-pixelSize) }?.run { colorNeighbors(mouse, pixelList) }
                        pixelList.find { it.x in pixelRange(pixel.x-pixelSize) && it.y in pixelRange(pixel.y+pixelSize) }?.run { colorNeighbors(mouse, pixelList) }
                        pixelList.find { it.x in pixelRange(pixel.x+pixelSize) && it.y in pixelRange(pixel.y-pixelSize) }?.run { colorNeighbors(mouse, pixelList) }

                        pixelList.find { it.x in pixelRange(pixel.x+pixelSize) && it.y == pixel.y }?.run { colorNeighbors(mouse, pixelList) }
                        pixelList.find { it.x in pixelRange(pixel.x-pixelSize) && it.y == pixel.y }?.run { colorNeighbors(mouse, pixelList) }
                        pixelList.find { it.x == pixel.x && it.y in pixelRange(pixel.y+pixelSize) }?.run { colorNeighbors(mouse, pixelList) }
                        pixelList.find { it.x == pixel.x && it.y in pixelRange(pixel.y-pixelSize) }?.run { colorNeighbors(mouse, pixelList) }
                        break
                    }
                    ++it
                }
            }
            tgBot.sendMessage("CPP finished!")
        }
    }
}


fun BufferedImage.isColored(): Boolean {
    val colors = mutableSetOf<Int>()
    (0 until this.height).forEach { y ->
        (0 until this.width).forEach { x ->
            colors.add(this.getRGB(x, y))
        }
    }
    return colors.size == 1
}

fun pixelRange(value: Double): IntRange {
    val x = value.toInt()
    return (x-1..x+2)
}

fun Point.exists(mouse: Robot, toNextColor: Int): Boolean {
    val a = mouse.createScreenCapture(Rectangle(x, y, toNextColor, 1))

    (0 until a.width - 1).forEach {
        if (a.getRGB(it, 0) != a.getRGB(it + 1, 0)) {
            return true
        }
    }
    return false
}


fun Point.isBackground(mouse: Robot, halfPixel: Int, backgroundImage: BufferedImage): Boolean {
    val image = capturePixel(mouse, halfPixel)
    return image.compare(backgroundImage)
}

fun Point.capturePixel(mouse: Robot, halfPixel: Int): BufferedImage {
    val offset = halfPixel/2
    return mouse.createScreenCapture(Rectangle(this.x - offset, this.y - offset, halfPixel, halfPixel))
}

fun BufferedImage.compare(other: BufferedImage): Boolean {
    (0 until this.width).forEach { x ->
        (0 until this.height).forEach { y ->
            if (this.getRGB(x,y) != other.getRGB(x,y)) return false
        }
    }
    return true
}

fun Bot.sendMessage(message: String): TelegramBotResult<Message> {
    return sendMessage(
        chatId = ChatId.fromId(SharedState.tgChatId),
        text = message
    )
}

fun Robot.createScreenCapture(point: Point): BufferedImage {
    return createScreenCapture(Rectangle(point.x-20, point.y-20, 40, 40))
}


fun Array<Point>.findExactPositions(mouse: Robot, bg: BufferedImage): List<Int> {
    val (l, t, r, b) = this
    val (lImg, tImg, rImg, bImg) = this.map { mouse.createScreenCapture(it) }
    return sequence {
        for (i in (0 until lImg.width)) if (lImg.getRGB(i, 20) != bg.getRGB(0, 0)) {yield(l.x + (i - 20)); break}
        for (i in (0 until tImg.height)) if (tImg.getRGB(20, i) != bg.getRGB(0, 0)) {yield(t.y + (i - 20)); break}
        for (i in (rImg.width-1 downTo 0)) if (rImg.getRGB(i, 20) != bg.getRGB(0, 0)) {yield(r.x + (i - 20)); break}
        for (i in (bImg.height-1 downTo 0)) if (bImg.getRGB(20, i) != bg.getRGB(0, 0)) {yield(b.y + (i - 20)); break}
    }.toMutableList()
}