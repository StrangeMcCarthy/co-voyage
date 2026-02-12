package com.covoyage.data.remote.api

import com.covoyage.data.remote.ApiConfig
import com.covoyage.data.remote.ApiResult
import com.covoyage.data.remote.safeApiCall
import com.covoyage.domain.model.InitiatePaymentRequest
import com.covoyage.domain.model.InitiatePaymentResponse
import com.covoyage.domain.model.Payment
import com.covoyage.domain.model.RefundRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class PaymentApiService(private val httpClient: HttpClient) {
    
    suspend fun initiatePayment(request: InitiatePaymentRequest): ApiResult<InitiatePaymentResponse> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.INITIATE_PAYMENT) {
            setBody(request)
        }.body()
    }
    
    suspend fun getPaymentStatus(paymentId: String): ApiResult<Payment> = safeApiCall {
        httpClient.get(ApiConfig.Endpoints.PAYMENT_STATUS.replace("{id}", paymentId)).body()
    }
    
    suspend fun requestRefund(request: RefundRequest): ApiResult<Payment> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.REQUEST_REFUND.replace("{id}", request.paymentId)) {
            setBody(request)
        }.body()
    }
    
    suspend fun getPaymentHistory(): ApiResult<List<Payment>> = safeApiCall {
        httpClient.get("${ApiConfig.Endpoints.PAYMENTS}/history").body()
    }
    
    suspend fun verifyPayment(reference: String): ApiResult<Payment> = safeApiCall {
        httpClient.get("${ApiConfig.Endpoints.PAYMENTS}/verify/$reference").body()
    }
}
