package kr.co.subway.data

data class StationDataIncludedTransfer(
    var line: String, // 호선
    var name: String, // 역명
    var lat: Double, // 위도
    var lng: Double, // 경도
    var transfer: Boolean // 환승역 여부
)
