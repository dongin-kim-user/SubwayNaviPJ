package kr.co.subway.common

import kr.co.subway.data.StationData
import retrofit2.Call
import retrofit2.http.GET

/*
 * station_coordinate json파일 call (가상서버)
 */
interface SubwayRestService {
//    @GET("/y-road/subway-coordinate/blob/main/db.json")
    @GET("/y-road/subway-coordinate/stations")
    fun getSubwayCoordinate(): Call<List<StationData>>
}