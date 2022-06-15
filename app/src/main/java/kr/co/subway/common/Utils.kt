package kr.co.subway.common

import kotlin.math.pow

/**
 * 두 좌표 사이의 거리
 */
fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val R = 6372.8 * 1000
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a =
        Math.sin(dLat / 2).pow(2.0) + Math.sin(dLon / 2)
            .pow(2.0) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(
            lat2))
    val c = 2 * Math.asin(Math.sqrt(a)) 
    /*
     * sqrt : 제곱근
     */

    return (R * c).toInt() // 두 좌표 사이의 거리 (m)
}
