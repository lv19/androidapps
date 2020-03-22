package lvlambda.twitter

import android.net.Uri
import android.util.Base64
import android.util.Log
import okhttp3.FormBody
import okhttp3.Request
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.absoluteValue
import kotlin.random.Random

class TwitterOAuth {
    companion object {

        fun authorization(request: Request,
                          oauth_consumer_key: String, consumerSecret: String,
                          accessToken: String, secret: String): String {
            val MAC_ALGORITHM = "HMAC-SHA1"
            val oauth_nonce = "${System.nanoTime()}${Random.nextLong().absoluteValue}"
            val oauth_timestamp = "${System.currentTimeMillis()/1000}"
            fun String.encode(): String {
                return URLEncoder.encode(this, "UTF-8")
            }
            fun oauthParams(): MutableMap<String, String?> {
                val params = mutableMapOf<String, String?>()
                params["oauth_consumer_key"] = oauth_consumer_key
                params["oauth_nonce"] = oauth_nonce
                params["oauth_signature"] = null
                params["oauth_signature_method"] = MAC_ALGORITHM
                params["oauth_timestamp"] = oauth_timestamp
                params["oauth_token"] = accessToken
                params["oauth_version"] = "1.0"
                return params
            }
            fun parameterString(): String {
                fun queryParams() : Map<String, String> {
                    val params = mutableMapOf<String, String>()
                    val httpUrl = request.url()
                    for (index in 0 until httpUrl.querySize())
                        params[httpUrl.queryParameterName(index)] =
                            httpUrl.queryParameterValue(index)
                    return params
                }
                fun bodyParams() : Map<String, String> {
                    val params = mutableMapOf<String, String>()
                    val body = request.body()
                    if (body !is FormBody)
                        return params
                    for (index in 0 until body.size())
                        params[body.name(index)] = body.value(index)
                    return params
                }
                val params = mutableMapOf<String, String>()
                params += queryParams()
                params += bodyParams()
                oauthParams().forEach { (name, value) ->
                    value?.let {params[name.encode()] = value.encode()}
                }
                val sortedParams = params.toSortedMap()
                val result = sortedParams
                    .map {(name, value) -> "${name.encode()}=${value.encode()}"}
                    .joinToString(separator = "&")
                Log.d(TAG, "parameterString:$result")
                return result
            }
            fun baseUrl(): String {
                val uri = Uri.parse(request.url().toString())
                return "${uri.scheme}://${uri.host}${uri.path}"
            }
            fun signatureBaseString(): String {
                val result = request.method().toUpperCase() +
                        "&${baseUrl().encode()}" +
                        "&${parameterString().encode()}"
                Log.d(TAG, "signatureBaseString:$result")
                return result
            }
            fun signingKey(): String {
                val result = "${consumerSecret.encode()}&${secret.encode()}"
                Log.d(TAG, "signingKey:$result")
                return result
            }
            fun oauth_signature(): String {
                val signatureBaseBytes = signatureBaseString().toByteArray()
                val signingKeyBytes = signingKey().toByteArray()
                val mac = Mac.getInstance(MAC_ALGORITHM)
                mac.init(SecretKeySpec(signingKeyBytes, MAC_ALGORITHM))
                val signatureBytes = mac.doFinal(signatureBaseBytes)
                val result = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
                Log.d(TAG, "oauth_signature:$result")
                return result
            }
            fun headerString() : String {
                val params = oauthParams()
                params["oauth_signature"] = oauth_signature()
                val result = "OAuth " + params.map { (name, value) ->
                    value?.let { "${name.encode()}=\"${value.encode()}\"" }
                }.joinToString(", ")
                Log.d(TAG, "headerString:$result")
                return result
            }
            return headerString();
/*
      parameterString
       map(queryParams, bodyParams, oAuthSignatureParams)
          .percentEncode(Key and Value)
          .sortByKey()
          .reduce(key1=value1&key2=value2...)
      signatureBaseString
       method.upperCase()&percentEncode(baseUrl)&percentEncode(parameterString)
      signingKey
       percentEncode(consumerSecret)&percentEncode(tokenSecret)
      oauth_signature
       hash_hmac(signatureBaseString, signigKey).base64()
      header Authorization:
       headerString = "OAuth " +
             map(oAuthParams)
             .percentEncode(Key and Value)
             .reduce(key1="value1", key2="value2"...)
*/
/*
      Header example:
      Authorization:
      OAuth oauth_consumer_key="xvz1evFS4wEEPTGEFPHBog",
      oauth_nonce="kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg",
      oauth_signature="tnnArxj06cWHq44gCs1OSKk%2FjLY%3D",
      oauth_signature_method="HMAC-SHA1",
      oauth_timestamp="1318622958",
      oauth_token="370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
      oauth_version="1.0"
*/
        }

        private const val TAG = "TwitterOAuth"
    }

}