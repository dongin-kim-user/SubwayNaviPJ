package kr.co.subway.common

import android.app.Application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kr.co.subway.R
import kr.co.subway.data.StationData
import java.io.*

/**
 * 현재 앱을 실행하기전 전처리 및 초기화에 필요한 작업을 하는 앱의 Global Application 객체
 */
class SubwayApplication : Application() {

    companion object {
        private lateinit var application: SubwayApplication
        fun getSubwayApplication() = application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
//        setAllActivitySettings()
        loadStationDataFromRaw()

    }

    //raw 폴더 내 json 로드
    private fun loadStationDataFromRaw() {
        val raw = resources.openRawResource(R.raw.station_coordinate)
        val writer: Writer = StringWriter()
        val buffer= CharArray(1024)
        raw.use { rawData ->
            val reader: Reader = BufferedReader(InputStreamReader(rawData, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0 , n)
            }
        }
        val jsonString = writer.toString()
        val gson = Gson()
        val listStationDataType = object : TypeToken<List<StationData>>() {}.type
        val stationDataList: List<StationData> = gson.fromJson(jsonString, listStationDataType)

        MainViewModel.stationHashMap = HashMap(stationDataList.map { it.line.plus(",${it.name}") to it }.toMap())
    }
}