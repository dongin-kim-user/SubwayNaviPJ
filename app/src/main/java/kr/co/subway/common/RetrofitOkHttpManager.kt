package kr.co.subway.common

import android.util.Log
import kr.co.subway.activity.MainActivity
import kr.co.subway.data.TARGET_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

/**
 * Http Core 작업은 OkHttp 가 작업하고 REST 작업은 Retrobit2에 위임하기 위한
 * REST 초기 설정 클래스
 */
object RetrofitOkHttpManager {
    private var okHttpClient: OkHttpClient

    /*
     * Builder 생성
     */

    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(TARGET_URL)

    val subwayRestService: SubwayRestService
        get() = retrofitBuilder.build().create(SubwayRestService::class.java)

    /*
     * request/response에 대한 모니터링, retry등의 작업을 처리하기 위한 Interceptor 추가
     * 여러개의 Interceptor 사용 시 Chain 사용
     * 새로운 요청 Interceptor, Retry요청 Interceptor
     * 연결시간 초과값 설정
     * retrofit과 okhttp를 연동하여 REST 및 GSON 변환은 retrofit,
     * Client core를 이루는 통신은 okhttp가 담당
     */
    init {
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                val newRequest: Request = request.newBuilder()
                    .addHeader("Accept","application/json")
                    .build()
                chain.proceed(newRequest)
            }).addInterceptor(RetryInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        retrofitBuilder.client(okHttpClient) // OkHttp 와 연동
    }

    /*
     * 요청 실패시 Retry 2번 실행
     */
    private class RetryInterceptor: Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            var response: Response = chain.proceed(request)
            var tryCount = 0
            val maxLimit = 2
            while (!response.isSuccessful && tryCount < maxLimit) {
                tryCount++
                Log.e(MainActivity.TAG, "요청 실패 - tryCount= $tryCount")
                response.close()
                response = chain.proceed(request)
            }
            return response
        }
    }
}
