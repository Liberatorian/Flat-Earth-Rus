package com.example.model

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import org.json.JSONObject
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min

object NaturalEarthLandData {
    fun load(context: Context): List<List<GeoPoint>> = runCatching {
        val root = context.assets.open("natural_earth_land.geojson").bufferedReader().use {
            JSONObject(it.readText())
        }
        val features = root.getJSONArray("features")
        buildList {
            for (featureIndex in 0 until features.length()) {
                val feature = features.getJSONObject(featureIndex)
                if (feature.isNull("geometry")) continue
                if (feature.representsAntarcticContinent()) continue
                val geometry = feature.getJSONObject("geometry")
                when (geometry.getString("type")) {
                    "Polygon" -> addOuterRing(geometry.getJSONArray("coordinates").getJSONArray(0))
                    "MultiPolygon" -> {
                        val polygons = geometry.getJSONArray("coordinates")
                        for (polygonIndex in 0 until polygons.length()) {
                            val polygon = polygons.getJSONArray(polygonIndex)
                            if (polygon.length() > 0) addOuterRing(polygon.getJSONArray(0))
                        }
                    }
                }
            }
        }.filter { it.size >= 4 }
    }.getOrDefault(emptyList())

    private fun JSONObject.representsAntarcticContinent(): Boolean {
        val bounds = optJSONArray("bbox") ?: return false
        if (bounds.length() < 4) return false
        val south = bounds.getDouble(1)
        val north = bounds.getDouble(3)
        val longitudeSpan = bounds.getDouble(2) - bounds.getDouble(0)
        return south <= -85.0 && north < -55.0 && longitudeSpan > 300.0
    }

    private fun MutableList<List<GeoPoint>>.addOuterRing(ring: org.json.JSONArray) {
        val points = ArrayList<GeoPoint>(ring.length())
        for (pointIndex in 0 until ring.length()) {
            val coordinate = ring.getJSONArray(pointIndex)
            points += GeoPoint(
                lat = coordinate.getDouble(1).coerceIn(-90.0, 90.0),
                lon = coordinate.getDouble(0).let { ((it + 180.0) % 360.0 + 360.0) % 360.0 - 180.0 }
            )
        }
        add(points)
    }
}

/** Keeps Natural Earth paths out of the per-frame geometry work. */
class ProjectedLandPathCache {
    private data class Key(
        val width: Float,
        val height: Float,
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val projection: MapProjection,
        val flipY: Boolean
    )

    private var cachedKey: Key? = null
    private var cachedPath = Path()

    fun get(
        polygons: List<List<GeoPoint>>,
        width: Float,
        height: Float,
        centerX: Float,
        centerY: Float,
        radius: Float,
        projection: MapProjection,
        flipY: Boolean
    ): Path {
        val key = Key(width, height, centerX, centerY, radius, projection, flipY)
        if (key == cachedKey) return cachedPath

        cachedPath = buildProjectedPath(polygons, centerX, centerY, radius, projection, flipY)
        cachedKey = key
        return cachedPath
    }

    private fun buildProjectedPath(
        polygons: List<List<GeoPoint>>,
        centerX: Float,
        centerY: Float,
        radius: Float,
        projection: MapProjection,
        flipY: Boolean
    ): Path {
        val result = Path()
        for (polygon in polygons) {
            val screenPoints = polygon.map { point ->
                val (xKm, yKm) = CelestialEngine.geoToDiscKm(point.lat, point.lon, projection)
                Offset(
                    centerX + (xKm / FlatEarthConstants.DISC_RADIUS_KM * radius).toFloat(),
                    centerY + (yKm / FlatEarthConstants.DISC_RADIUS_KM * radius).toFloat() * if (flipY) -1f else 1f
                )
            }.dropDuplicateClosure()
            if (screenPoints.size < 3) continue

            val simplified = simplifyClosed(screenPoints, tolerancePx = 0.8f)
            if (simplified.size < 3 || simplified.extent() < 1.4f) {
                val center = Offset(
                    screenPoints.sumOf { it.x.toDouble() }.toFloat() / screenPoints.size,
                    screenPoints.sumOf { it.y.toDouble() }.toFloat() / screenPoints.size
                )
                val dotRadius = max(0.7f, screenPoints.extent() * 0.5f)
                result.addOval(Rect(center.x - dotRadius, center.y - dotRadius, center.x + dotRadius, center.y + dotRadius))
                continue
            }

            result.moveTo(simplified.first().x, simplified.first().y)
            simplified.drop(1).forEach { result.lineTo(it.x, it.y) }
            result.close()
        }
        return result
    }

    private fun List<Offset>.dropDuplicateClosure(): List<Offset> =
        if (size > 1 && first() == last()) dropLast(1) else this

    private fun List<Offset>.extent(): Float {
        val minX = minOf { it.x }
        val maxX = maxOf { it.x }
        val minY = minOf { it.y }
        val maxY = maxOf { it.y }
        return max(maxX - minX, maxY - minY)
    }

    private fun simplifyClosed(points: List<Offset>, tolerancePx: Float): List<Offset> {
        if (points.size <= 4) return points
        val anchor = points.indices.maxBy { index -> distanceSquared(points.first(), points[index]) }
        val firstArc = simplifyOpen(points.subList(0, anchor + 1), tolerancePx)
        val secondArcInput = ArrayList<Offset>(points.size - anchor + 1).apply {
            addAll(points.subList(anchor, points.size))
            add(points.first())
        }
        val secondArc = simplifyOpen(secondArcInput, tolerancePx)
        return firstArc + secondArc.drop(1).dropLast(1)
    }

    private fun simplifyOpen(points: List<Offset>, tolerancePx: Float): List<Offset> {
        if (points.size <= 2) return points
        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.lastIndex] = true
        val ranges = ArrayDeque<Pair<Int, Int>>()
        ranges.addLast(0 to points.lastIndex)
        val toleranceSquared = tolerancePx * tolerancePx

        while (ranges.isNotEmpty()) {
            val (start, end) = ranges.removeLast()
            var farthestIndex = -1
            var farthestDistance = toleranceSquared
            for (index in start + 1 until end) {
                val distance = distanceToSegmentSquared(points[index], points[start], points[end])
                if (distance > farthestDistance) {
                    farthestDistance = distance
                    farthestIndex = index
                }
            }
            if (farthestIndex >= 0) {
                keep[farthestIndex] = true
                ranges.addLast(start to farthestIndex)
                ranges.addLast(farthestIndex to end)
            }
        }
        return points.filterIndexed { index, _ -> keep[index] }
    }

    private fun distanceSquared(first: Offset, second: Offset): Float {
        val dx = first.x - second.x
        val dy = first.y - second.y
        return dx * dx + dy * dy
    }

    private fun distanceToSegmentSquared(point: Offset, start: Offset, end: Offset): Float {
        val dx = end.x - start.x
        val dy = end.y - start.y
        if (dx == 0f && dy == 0f) return distanceSquared(point, start)
        val projection = (((point.x - start.x) * dx + (point.y - start.y) * dy) / (dx * dx + dy * dy)).coerceIn(0f, 1f)
        val nearest = Offset(start.x + projection * dx, start.y + projection * dy)
        return distanceSquared(point, nearest)
    }
}

class ProjectedSceneLandPathCache {
    private data class Key(
        val width: Float,
        val height: Float,
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val pitch: Float,
        val yaw: Float,
        val projection: MapProjection
    )

    private var cachedKey: Key? = null
    private var cachedPath = Path()

    fun get(
        polygons: List<List<GeoPoint>>,
        width: Float,
        height: Float,
        centerX: Float,
        centerY: Float,
        radius: Float,
        pitch: Float,
        yaw: Float,
        projection: MapProjection,
        project: (Double, Double, Double) -> Offset
    ): Path {
        val key = Key(width, height, centerX, centerY, radius, pitch, yaw, projection)
        if (key == cachedKey) return cachedPath

        val result = Path()
        for (polygon in polygons) {
            val screenPoints = polygon.map { point ->
                val (xKm, yKm) = CelestialEngine.geoToDiscKm(point.lat, point.lon, projection)
                project(
                    xKm / FlatEarthConstants.DISC_RADIUS_KM,
                    yKm / FlatEarthConstants.DISC_RADIUS_KM,
                    0.005
                )
            }.let { if (it.size > 1 && it.first() == it.last()) it.dropLast(1) else it }
            if (screenPoints.size < 3) continue

            val minX = screenPoints.minOf { it.x }
            val maxX = screenPoints.maxOf { it.x }
            val minY = screenPoints.minOf { it.y }
            val maxY = screenPoints.maxOf { it.y }
            val extent = maxOf(maxX - minX, maxY - minY)
            if (extent < 1.2f) {
                val center = Offset(
                    screenPoints.sumOf { it.x.toDouble() }.toFloat() / screenPoints.size,
                    screenPoints.sumOf { it.y.toDouble() }.toFloat() / screenPoints.size
                )
                val dotRadius = maxOf(0.65f, extent * 0.5f)
                result.addOval(Rect(center.x - dotRadius, center.y - dotRadius, center.x + dotRadius, center.y + dotRadius))
            } else {
                result.moveTo(screenPoints.first().x, screenPoints.first().y)
                screenPoints.drop(1).forEach { result.lineTo(it.x, it.y) }
                result.close()
            }
        }
        cachedPath = result
        cachedKey = key
        return result
    }
}