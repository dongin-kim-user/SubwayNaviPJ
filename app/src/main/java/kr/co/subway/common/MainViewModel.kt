package kr.co.subway.common

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naver.maps.map.overlay.Marker
import kr.co.subway.activity.MainActivity
import kr.co.subway.data.CENTER_LATITUDE
import kr.co.subway.data.CENTER_LONGITUDE
import kr.co.subway.data.StationData
import kr.co.subway.data.StationDataIncludedTransfer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity UI 화면과 사용자 액션에 따른 데이터 제어 ViewModel 클래스
 */
class MainViewModel : ViewModel() {

    /*
     * 변하지 않는 로직인 stationHashMap(역 정보)를 동반객체로 선언하여 공통적으로 사용 (singleton)
     */
    companion object {
        var stationHashMap = HashMap<String, StationData>()
    }
    
    /*
     * 환승역 정보, 마커, 마지막 위도 경도 줌 레벨 초기화
     */
    private val emptyStationDataIncludedTransfer =
        StationDataIncludedTransfer("", "", 0.0, 0.0, false)
    val emptyMarker = Marker().apply { tag = emptyStationDataIncludedTransfer }

    var lastLatitude: Double = CENTER_LATITUDE // 마지막 맵 중심의 위도
    var lastLongitude: Double = CENTER_LONGITUDE // 마지막 맵 중심의 경도
    var lastZoomLevel: Double = 11.0 // 마지막 맵 줌 레벨

    var markerList = mutableListOf<Marker>()

    // 선택된 역
    private var selectedStation: MutableLiveData<Marker> = MutableLiveData(emptyMarker)
    fun getSelectedStation(): MutableLiveData<Marker> {
        return selectedStation
    }
    fun setSelectedStation(stationMarker: Marker) {
        selectedStation.value = stationMarker
    }

    // 출발역
    private var departureStation: MutableLiveData<Marker> = MutableLiveData(emptyMarker)
    fun getDeparture(): MutableLiveData<Marker> {
        return departureStation
    }
    fun setDeparture(station: Marker) {
        departureStation.value = station
    }

    // 도착역
    private var arrivalStation: MutableLiveData<Marker> = MutableLiveData(emptyMarker)
    fun getArrival(): MutableLiveData<Marker> {
        return arrivalStation
    }
    fun setArrival(station: Marker) {
        arrivalStation.value = station

    }

    // 받은 HashMap 역 정보에 환승역 여부 정보 추가해 MutableList 로 반환
    fun getStationDataIncludedTransferList(hashMap: HashMap<String, StationData>): MutableList<StationDataIncludedTransfer> {
        var stationList = mutableListOf<StationDataIncludedTransfer>()

        hashMap.forEach() { item ->
            val cnt = stationHashMap.count { it.key.contains(item.value.name) }
            val stationDataIncludedTransfer = StationDataIncludedTransfer(
                item.value.line,
                item.value.name,
                item.value.lat,
                item.value.lng,
                false
            )
            if (cnt > 1) { stationDataIncludedTransfer.transfer = true }
            stationList.add(stationDataIncludedTransfer)
        }
        return stationList
    }

    // 중심을 기준으로 반경 4km 내 마커 반환, 중복된 환승역 필터링
    /*
     * collection filter로 두 좌표 사이의 거리(getDistance) 4km 이내 반환
     * 중심 위도,경도 / 해당 역 위도 경도 거리 계산
     * split 함수로 문자열을 콤마 단위로 분할하여 배열에 저장
     */
    fun getValidStationHashMap(): HashMap<String, StationData> {
        val validStationHashMap = HashMap(stationHashMap.filter {
            getDistance(CENTER_LATITUDE, CENTER_LONGITUDE, it.value.lat, it.value.lng) <= 4000
        }.mapKeys {
            it.key.split(",")[1]
        })
        return validStationHashMap
    }


   
    /*
     * Rest 요청    
     * isEmpty함수로 초기화여부 물어봄
     */
    fun requestSubwayCoordinate() {
        val subwayRestService = RetrofitOkHttpManager.subwayRestService
        val call: Call<List<StationData>> = subwayRestService.getSubwayCoordinate()

        call.enqueue(object : Callback<List<StationData>> {
            override fun onResponse(
                call: Call<List<StationData>>,
                response: Response<List<StationData>>,
            ) {
                if (response.isSuccessful) {
                    val stationDataList = response.body() as List<StationData>
                    if (stationDataList.isEmpty()) {
                        Toast.makeText(
                            SubwayApplication.getSubwayApplication(),
                            "지하철역 정보를 불러올 수 없어요",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        initStationHashMap(stationDataList)
                    }
                }
            }

            override fun onFailure(call: Call<List<StationData>>, t: Throwable) {
                Toast.makeText(
                    SubwayApplication.getSubwayApplication(),
                    "지하철역 정보를 불러오는데 실패했어요",
                    Toast.LENGTH_SHORT).show()

                Log.e(MainActivity.TAG, t.toString())
            }
        })
    }

    /*
     * 역정보 초기화 함수
     */
    fun initStationHashMap(subwayCoordinateList: List<StationData>) {
        stationHashMap =
            HashMap(subwayCoordinateList.map { it.line.plus(",${it.name}") to it }.toMap())
    }
}