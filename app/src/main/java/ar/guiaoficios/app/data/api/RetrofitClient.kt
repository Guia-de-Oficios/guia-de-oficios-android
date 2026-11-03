package ar.guiaoficios.app.data.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import java.util.concurrent.TimeUnit

/**
 * Proveedor de Retrofit2 + OkHttp3 con AuthInterceptor JWT y soporte Multipart real (GDO-20).
 */
object RetrofitClient {

    fun createApi(sessionManager: SessionManager): GuiaDeOficiosApi {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
            val token = sessionManager.cachedToken
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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
