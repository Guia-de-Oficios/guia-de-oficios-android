package ar.guiaoficios.app.data.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import ar.guiaoficios.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Proveedor de Retrofit2 + OkHttp3 con AuthInterceptor JWT, propagación de X-Trace-ID y soporte Multipart real (GDO-20).
 */
object RetrofitClient {
    private const val LOG_TAG = "GuiaOficiosTrace"

    fun createApi(sessionManager: SessionManager): GuiaDeOficiosApi {
        val traceAndAuthInterceptor = Interceptor { chain ->
            val startNs = System.nanoTime()
            val original = chain.request()
            val traceId = "and-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(8)}"

            val builder = original.newBuilder()
                .header("X-Trace-ID", traceId)

            val token = sessionManager.cachedToken
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }

            val req = builder.build()
            Log.i(LOG_TAG, "--> [TRACE:$traceId] ${req.method} ${req.url}")

            try {
                val response = chain.proceed(req)
                val durationMs = (System.nanoTime() - startNs) / 1_000_000.0
                val serverTrace = response.header("X-Trace-ID") ?: traceId
                Log.i(
                    LOG_TAG,
                    "<-- [TRACE:$serverTrace] ${req.method} ${req.url} | status=${response.code} | duration=${String.format("%.2f", durationMs)}ms"
                )
                response
            } catch (e: Exception) {
                Log.e(LOG_TAG, "xxx [TRACE:$traceId] Error en ${req.method} ${req.url}: ${e.message}")
                throw e
            }
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(traceAndAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val baseUrl = sessionManager.cachedBaseUrl.ifBlank { BuildConfig.API_BASE_URL }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GuiaDeOficiosApi::class.java)
    }

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        category: String
    ): Pair<MultipartBody.Part, RequestBody>? {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        var fileName = "upload_${System.currentTimeMillis()}.jpg"

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIdx) ?: fileName
            }
        }

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val fileBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, fileBody)
        val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
        return Pair(part, categoryBody)
    }
}
