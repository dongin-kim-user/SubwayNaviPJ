package kr.co.subway.data

data class StationData(
    var line: String, // 호선
    var name: String, // 역명
    var lat: Double, // 위도
    var lng: Double // 경도
)