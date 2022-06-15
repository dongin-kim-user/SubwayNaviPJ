package kr.co.subway.activity

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding4.view.clicks
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.subway.common.MainViewModel
import kr.co.subway.data.StationDataIncludedTransfer
import kr.co.subway.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

/**
 * Entry Point 에 해당하는 화면이며 지도 및 사용자에게 출발역/종착역에 대한 안내를 진행하는 Activity
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        val TAG: String = "MainActivity"
        lateinit var naverMap: NaverMap
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    //Rx를 이용해 사용자의 인터액션을 제어 및 처리
    private val compositeDisposable = CompositeDisposable()

    /*
     * CompositeDisposable : 여러개의 subscribe 를 한번에 해제시키기 위해 CompositeDisposable 사용
     * onDestroy 에서 자원해제 요함
     */
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        /*
         * ReactiveX는 이벤트 기반 비동기 프로그래밍을 위한 라이브러리이다.
         * 콜백 방식과 달리 이벤트 스트림을 관찰하다가 원하는 이벤트를 감지하면 이에 따른 동작을 수행하는 방식
         */

        /*
         * mapView 바인딩 후 getMapAsync로 지도 콜백을 등록
         * backBtn에 onBackPressed()로 종료처리 추가
         * Rx 라이브러리 : 비동기 이벤트를 보다 쉽게 가공 및 사용하기 위함
                         (아래 코드에서는 setOnClickListener 대신사용한다.)
         * throttleLast : 버튼 누른 후 0.3초간의 행동을 막음, 더블 클릭등의 중복처리를 막기 위함
         * Observer를 통한 아이템 정의, subscribe를 통한 아이템 발행
         * ComposteDisposable를 통해 subscribe 한꺼번에 해제
         */
        
        with(binding) {
            mapView.apply {
                onCreate(savedInstanceState)
                getMapAsync(this@MainActivity)
            }
            backBtn.setOnClickListener {
                onBackPressed()
            }

            /*
             * 선택된 역이 있을 시
             * departure, Arrival(Btn, Oval, Text) 클릭하면 카메라 화면이 해당 역 중심으로 가도록 설정
             * Rx라이브러리의 Disposable을 이용하여 Obversable의 구독이 필요없을 때 메모리 누수 방지를
             * 위해 폐기(disposable)하는데 여러명의 구독자가 있을 시 compositeDisposable을 이용하여
             * 구독을 한꺼번에 해지할 수 있다.
             * tag : 네이버 맵의 부수적 정보를 세팅(setter)및 알아내는(getter)함수
             */

            compositeDisposable
                .add(
                    departureBtn
                        .clicks()
                        .throttleLast(300, TimeUnit.MILLISECONDS)
                        .subscribe {
                            with((viewModel.getDeparture().value!!.tag as StationDataIncludedTransfer)) {
                                if (lat != 0.0 && lng != 0.0) {
                                    moveCameraPosition(lat, lng)
                                }
                            }
                        }
                )
            compositeDisposable
                .add(
                    departureOval
                        .clicks()
                        .throttleLast(300, TimeUnit.MILLISECONDS)
                        .subscribe {
                            with((viewModel.getDeparture().value!!.tag as StationDataIncludedTransfer)) {
                                if (lat != 0.0 && lng != 0.0) {
                                    moveCameraPosition(lat, lng)
                                }
                            }
                        }
                )
            compositeDisposable
                .add(
                    departureText
                        .clicks()
                        .throttleLast(300, TimeUnit.MILLISECONDS)
                        .subscribe {
                            with((viewModel.getDeparture().value!!.tag as StationDataIncludedTransfer)) {
                                if (lat != 0.0 && lng != 0.0) {
                                    moveCameraPosition(lat, lng)
                                }
                            }
                        }
                )
            compositeDisposable
                .add(
                    arrivalBtn
                        .clicks()
                        .throttleLast(300, TimeUnit.MILLISECONDS)
                        .subscribe {
                            with((viewModel.getArrival().value!!.tag as StationDataIncludedTransfer)) {
                                if (lat != 0.0 && lng != 0.0) {
                                    moveCameraPosition(lat, lng)
                                }
                            }
                        }
                )
            compositeDisposable
                .add(
                    arrivalOval
                        .clicks()
                        .throttleLast(300, TimeUnit.MILLISECONDS)
                        .subscribe {
                            with((viewModel.getArrival().value!!.tag as StationDataIncludedTransfer)) {
                                if (lat != 0.0 && lng != 0.0) {
                                    moveCameraPosition(lat, lng)
                                }
                            }
                        }
                )
            compositeDisposable
                .add(
                    arrivalText
                        .clicks()
                        .throttleLast(300, TimeUnit.MILLISECONDS)
                        .subscribe {
                            with((viewModel.getArrival().value!!.tag as StationDataIncludedTransfer)) {
                                if (lat != 0.0 && lng != 0.0) {
                                    moveCameraPosition(lat, lng)
                                }
                            }
                        }
                )
        }

        /* 
         * viewModel의 인스턴스를 받아오기 위한 Provider생성
         */

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

//      viewModel.requestSubwayCoordinate()

        /*
         * observer를 통한 객체의 변화 감지
         * 1. 출발역,도착역 모두 미선택 or 도착역만 선택된 상태 : 역을 선택하면 출발역이 됨
         * 2. 출발역,도착역 모두 선택 or 출발역만 선택된 상태 : 역을 선택하면 도착역이 됨
         */

        with(viewModel) {
            // 관찰자 로직
            val selectedStationObserver = Observer<Marker> {
                if ((getDeparture().value!!.tag as StationDataIncludedTransfer).name == "" && (getArrival().value!!.tag as StationDataIncludedTransfer).name == "") { // 출발역, 도착역 모두 미선택시
                    setDeparture(getSelectedStation().value!!)
                } else if ((getDeparture().value!!.tag as StationDataIncludedTransfer).name == "" && (getArrival().value!!.tag as StationDataIncludedTransfer).name != "") { // 도착역만 선택시
                    setDeparture(getSelectedStation().value!!)
                } else { // 출발역, 도착역 모두 선택시 + 출발역만 선택시
                    with(getArrival().value!!) {
                        if ((this.tag as StationDataIncludedTransfer).name != "") {
                            this.iconTintColor =
                                if ((this.tag as StationDataIncludedTransfer).transfer) Color.YELLOW else Color.BLACK
                        }
                    }
                    setArrival(getSelectedStation().value!!)
                }
            }

            /*
             * 출발역 선택 없을 시 : '출발역을 선택해주세요' 문구 표시와 Oval 회색 처리
             * 선택 시 : 해당 역 이름 표시와 Oval 빨간색 처리
             */

            val departureStationObserver = Observer<Marker> {

                when ((getDeparture().value!!.tag as StationDataIncludedTransfer).name) {
                    "" -> {
                        binding.departureText.text = "출발역을 선택해주세요"
                        (binding.departureOval.background as GradientDrawable).setColor(Color.rgb(
                            226,
                            226,
                            226))
                    }
                    else -> {
                        binding.departureText.text =
                            ((getDeparture().value!!.tag as StationDataIncludedTransfer).name)
                        (binding.departureOval.background as GradientDrawable).setColor(Color.RED)
                        getDeparture().value!!.iconTintColor = Color.RED
                    }
                }
            }

            /*
             * 도착역 선택 없을 시 : '도착역을 선택해주세요' 문구 표시와 Oval 회색 처리
             * 선택 시 : 해당 역 이름 표시와 Oval 파란색 처리
             */

            val arrivalStationObserver = Observer<Marker> {

                when ((getArrival().value!!.tag as StationDataIncludedTransfer).name) {
                    "" -> {
                        binding.arrivalText.text = "도착역을 선택해주세요"
                        (binding.arrivalOval.background as GradientDrawable).setColor(Color.rgb(226,
                            226,
                            226))
                    }
                    else -> {
                        binding.arrivalText.text =
                            (getArrival().value!!.tag as StationDataIncludedTransfer).name
                        (binding.arrivalOval.background as GradientDrawable).setColor(Color.BLUE)
                        getArrival().value!!.iconTintColor = Color.BLUE
                    }
                }
            }

            // 관찰자 설정
            getSelectedStation().observe(this@MainActivity, selectedStationObserver)
            getDeparture().observe(this@MainActivity, departureStationObserver)
            getArrival().observe(this@MainActivity, arrivalStationObserver)
        }
    }

    /*
     * 카메라 이동 설정
     */

    private fun moveCameraPosition(lat: Double, lng: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
            .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onMapReady(nMap: NaverMap) {
        initNaverMap(nMap)
    }
    
    /*
     * 지도 초기화
     * 역 재선택 시 : 일반역은 검은색, 환승역은 노란색
     */

    private fun initNaverMap(nMap: NaverMap) {
        val listener = Overlay.OnClickListener { o ->
            val selectedMarker = o as Marker
            val selectedStationDataIncludedTransfer =
                selectedMarker.tag as StationDataIncludedTransfer

            with(viewModel) {
                when (selectedStationDataIncludedTransfer.name) {
                    (getDeparture().value!!.tag as StationDataIncludedTransfer).name -> { // 출발역 재선택 시
                        setDeparture(emptyMarker)
                        selectedMarker.iconTintColor =
                            if (selectedStationDataIncludedTransfer.transfer) Color.YELLOW else Color.BLACK
                    }
                    (getArrival().value!!.tag as StationDataIncludedTransfer).name -> { // 도착역 재선택 시
                        setArrival(emptyMarker)
                        selectedMarker.iconTintColor =
                            if (selectedStationDataIncludedTransfer.transfer) Color.YELLOW else Color.BLACK
                    }
                    else -> {
                        setSelectedStation(selectedMarker)
                    }
                }
            }
            true // false: 이벤트를 맵으로 전파, true: 이벤트를 소비하여 이벤트가 발생하지 않음
        }

        /*
         * CoroutineScope : 다운로드나 서버통신을 위해 필요할때만 시작, 완료되면 종료
         * CoroutineScope의 Dispatchers를 이용해 작업 쓰레드 지정
         * UI 작업을 위한 Main 지정
         * uiSettings : 화면을 무효화/유효화 시키기 위함 (안보이게하는 setVisible하고는 다른 개념)
         * OnCameraChangeListener 카메라 움직임 종료에 대한 리스너
         */

        with(CoroutineScope(Dispatchers.Main)) {
            launch {
                naverMap = nMap.apply {
                    with(uiSettings) {
                        isZoomControlEnabled = false             //줌 컨트롤 활성화 여부
                        isCompassEnabled = false                //나침반 활성화 여부
                        isLocationButtonEnabled = false          //현위치버튼 활성화 여부
                        isScaleBarEnabled = false                //축척 바 활성화 여부

                        addOnCameraChangeListener { _, _ ->
                            val cameraPosition = nMap.cameraPosition
                            with(viewModel) {
                                lastLatitude = cameraPosition.target.latitude
                                lastLongitude = cameraPosition.target.longitude
                                lastZoomLevel = cameraPosition.zoom
                            }
                        }
                    }
                    with(viewModel) {
                        cameraPosition =
                            CameraPosition(LatLng(lastLatitude, lastLongitude), lastZoomLevel)
                    }
                    minZoom = 6.0
                }

                /*
                 * 마커 설정
                 * iterator 사용 : 객체나 자료를 열거자 형식으로 바꿈
                 */
                with(viewModel.markerList) {
                    if (this.isEmpty()) {
                        val stationIterator =
                            viewModel.getStationDataIncludedTransferList(viewModel.getValidStationHashMap())
                                .iterator()
                        while (stationIterator.hasNext()) {
                            val marker = Marker().apply {
                                val now = stationIterator.next()
                                position = LatLng(now.lat, now.lng)
                                icon = OverlayImage.fromResource(kr.co.subway.R.drawable.station_marker)
                                tag = now
                                if (now.transfer) {
                                    iconTintColor = Color.YELLOW
                                }
                                onClickListener = listener
                                map =
                                    kr.co.subway.activity.MainActivity.naverMap // Only the main thread can call this method.
                            }
                            this.add(marker)
                        }
                    } else {
                        this.map {
                            it.map = kr.co.subway.activity.MainActivity.naverMap
                        }
                    }
                }
            }
        }
    }

    /**
     * Naver Map 과 Lify Cycle 동기화
     */
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        this.compositeDisposable.clear()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}